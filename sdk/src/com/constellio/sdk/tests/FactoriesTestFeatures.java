/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.sdk.tests;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.sdk.tests.TestUtils.asList;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.constellio.app.services.extensions.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.services.factories.ModelLayerFactory;

public class FactoriesTestFeatures {

	private boolean dummyPasswords;

	private File initialState;
	private final FileSystemTestFeatures fileSystemTestFeatures;
	private ConstellioFactories factoriesInstance;
	private List<Class<?>> spiedClasses = new ArrayList<>();

	private TestConstellioFactoriesDecorator decorator;

	//	private Map<String, String> sdkProperties;
	private List<DataLayerConfigurationAlteration> dataLayerConfigurationAlterations = new ArrayList<>();
	private List<ModelLayerConfigurationAlteration> modelLayerConfigurationAlterations = new ArrayList<>();
	private List<AppLayerConfigurationAlteration> appLayerConfigurationAlterations = new ArrayList<>();
	private Map<String, String> configs = new HashMap<>();
	private String systemLanguage;

	public FactoriesTestFeatures(FileSystemTestFeatures fileSystemTestFeatures, Map<String, String> sdkProperties) {
		this.fileSystemTestFeatures = fileSystemTestFeatures;
		//		this.sdkProperties = sdkProperties;
	}

	public void afterTest() {

		if (factoriesInstance != null) {

			DataLayerConfiguration conf = factoriesInstance.getDataLayerConfiguration();

			for (BigVaultServer server : factoriesInstance.getDataLayerFactory().getSolrServers().getServers()) {
				deleteServerRecords(server.getName(), server.getNestedSolrServer(), server.getSolrFileSystem(),
						server.getNestedSolrServer());
			}

			if (ContentDaoType.HADOOP == conf.getContentDaoType()) {
				deleteFromHadoop(conf.getContentDaoHadoopUser(), conf.getContentDaoHadoopUrl());

			}

			if (ConfigManagerType.ZOOKEEPER == conf.getSettingsConfigType()) {
				deleteFromZooKeeper(conf.getSettingsZookeeperAddress());
			}
		}

		ConstellioFactories.clear();
		factoriesInstance = null;

	}

	private void deleteFromZooKeeper(String address) {
		try {
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			CuratorFramework client = CuratorFrameworkFactory.newClient(address, retryPolicy);
			client.start();
			client.delete().deletingChildrenIfNeeded().forPath("/constellio");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void deleteFromHadoop(String user, String url) {
		System.setProperty("HADOOP_USER_NAME", user);
		Configuration hadoopConfig = new Configuration();

		if (url == null || user == null) {
			throw new RuntimeException("No config");
		}

		hadoopConfig.set("fs.defaultFS", url);
		hadoopConfig.set("hadoop.job.ugi", user);

		try {
			FileSystem hdfs = FileSystem.get(hadoopConfig);
			for (FileStatus file : hdfs.globStatus(new Path("*"))) {
				hdfs.delete(file.getPath(), true);
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void deleteServerRecords(String serverName, SolrClient solrServer, AtomicFileSystem configManager,
			SolrClient adminServer) {

		BigVaultServer vaultServer = new BigVaultServer(serverName, solrServer, configManager, adminServer,
				BigVaultLogger.disabled());

		ModifiableSolrParams allRecordsSolrParams = new ModifiableSolrParams();
		allRecordsSolrParams.set("q", "*:*");

		try {
			vaultServer
					.addAll(new BigVaultServerTransaction(NOW).addDeletedQuery("*:*"));
		} catch (BigVaultException e) {
			throw new RuntimeException("Cannot deleteLogically by query *:*");
		}
		try {

			if (!vaultServer.query(allRecordsSolrParams).getResults().isEmpty()) {
				throw new RuntimeException("Invalid solr core initial state ");
			}

		} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
			throw new RuntimeException(couldNotExecuteQuery);
		}
	}

	public synchronized ConstellioFactories getConstellioFactories() {

		if (decorator == null) {

			StringBuilder setupPropertiesContent = new StringBuilder();
			setupPropertiesContent.append("admin.servicekey=adminkey\n");
			setupPropertiesContent.append("admin.password=password\n");
			File setupProperties = fileSystemTestFeatures.newTempFileWithContent(setupPropertiesContent.toString());

			decorator = new TestConstellioFactoriesDecorator() {

				@Override
				public DataLayerFactory decorateDataLayerFactory(DataLayerFactory dataLayerFactory) {
					if (spiedClasses.isEmpty()) {
						return dataLayerFactory;
					} else {
						return spy(dataLayerFactory);
					}
				}

				@Override
				public ModelLayerFactory decorateModelServicesFactory(ModelLayerFactory modelLayerFactory) {
					if (spiedClasses.isEmpty()) {
						return modelLayerFactory;
					} else {
						return spy(modelLayerFactory);
					}
				}

				@Override
				public AppLayerFactory decorateAppServicesFactory(AppLayerFactory appLayerFactory) {
					return spy(appLayerFactory);
				}

				@Override
				public StatefullServiceDecorator getStatefullServiceDecorator() {
					return new StatefullServiceDecorator() {
						@Override
						public <T> T decorate(T service) {

							if (ConstellioPluginManager.class.isAssignableFrom(service.getClass())) {
								return spy(service);
							} else {

								for (Class<?> spiedClass : spiedClasses) {

									if (spiedClass.isAssignableFrom(service.getClass())) {
										return spy(service);
									}
								}
							}
							return service;
						}
					};

				}
			};

			File configManagerFolder = fileSystemTestFeatures.newTempFolderWithName("configManagerFolder");
			File contentFolder = fileSystemTestFeatures.newTempFolderWithName("contentFolder");

			decorator.setDataLayerConfigurationAlterations(dataLayerConfigurationAlterations);
			decorator.setModelLayerConfigurationAlterations(modelLayerConfigurationAlterations);
			decorator.setAppLayerConfigurationAlterations(appLayerConfigurationAlterations);
			decorator.setSetupProperties(setupProperties);
			decorator.setImportationFolder(fileSystemTestFeatures.newTempFolderWithName("importationFolder"));
			decorator.setConfigManagerFolder(configManagerFolder);
			decorator.setAppTempFolder(fileSystemTestFeatures.newTempFolderWithName("appTempFolder"));
			decorator.setContentFolder(contentFolder);
			decorator.setSystemLanguage(systemLanguage);

			if (initialState != null) {
				File tempFolder = fileSystemTestFeatures.newTempFolder();
				try {
					SaveStateFeature.loadStateFrom(initialState, tempFolder, configManagerFolder, contentFolder, dummyPasswords);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		File propertyFile = new SDKFoldersLocator().getSDKProperties();

		if (!configs.isEmpty()) {

			File tempPropertyFile = fileSystemTestFeatures.newTempFileInNewTempFolder("temp-sdk.properties");
			StringBuilder basePropertyContent = new StringBuilder();
			try {
				basePropertyContent.append(FileUtils.readFileToString(propertyFile) + "\n");

				for (Map.Entry<String, String> entry : configs.entrySet()) {
					basePropertyContent.append(entry.getKey() + "=" + entry.getValue() + "\n");
				}

				FileUtils.writeStringToFile(tempPropertyFile, basePropertyContent.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			propertyFile = tempPropertyFile;
		}

		return factoriesInstance = ConstellioFactories.getInstance(propertyFile, decorator);
	}

	public void configure(DataLayerConfigurationAlteration dataLayerConfigurationAlteration) {
		dataLayerConfigurationAlterations.add(dataLayerConfigurationAlteration);
	}

	public void configure(ModelLayerConfigurationAlteration modelLayerConfigurationAlteration) {
		modelLayerConfigurationAlterations.add(modelLayerConfigurationAlteration);
	}

	public void configure(AppLayerConfigurationAlteration appLayerConfigurationAlteration) {
		appLayerConfigurationAlterations.add(appLayerConfigurationAlteration);
	}

	public void load() {
		getConstellioFactories();
	}

	public DataLayerFactory newDaosFactory() {
		return getConstellioFactories().getDataLayerFactory();
	}

	public IOServicesFactory newIOServicesFactory() {
		return getConstellioFactories().getIoServicesFactory();
	}

	public ModelLayerFactory newModelServicesFactory() {
		return getConstellioFactories().getModelLayerFactory();
	}

	public AppLayerFactory newAppServicesFactory() {
		return getConstellioFactories().getAppLayerFactory();
	}

	public FoldersLocator getFoldersLocator() {
		return getConstellioFactories().getFoldersLocator();
	}

	public void withSpiedServices(Class<?>[] classes) {
		spiedClasses.addAll(asList(classes));
	}

	public void givenConstellioProperties(Map<String, String> configs) {
		this.configs.putAll(configs);

	}

	public FactoriesTestFeatures givenSystemInState(File state) {
		this.initialState = state;
		return this;
	}

	public FactoriesTestFeatures withPasswordsReset() {
		this.dummyPasswords = true;
		return this;
	}

	public void setSystemLanguage(String languageCode) {
		this.systemLanguage = languageCode;
	}
}
