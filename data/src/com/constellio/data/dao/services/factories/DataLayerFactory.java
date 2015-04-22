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
package com.constellio.data.dao.services.factories;

import java.io.File;

import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.conf.SolrServerType;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.managers.config.ZooKeeperConfigManager;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.contents.HadoopContentDao;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.data.dao.services.solr.serverFactories.CloudSolrServerFactory;
import com.constellio.data.dao.services.solr.serverFactories.HttpSolrServerFactory;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.XMLSecondTransactionLogManager;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.test.FaultInjectorSolrServerFactory;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class DataLayerFactory extends LayerFactory {

	static final String RECORDS_COLLECTION = "records";
	static final String EVENTS_COLLECTION = "events";
	static final String CONTENTS_COLLECTION = "contents";
	static final String NOTIFICATIONS_COLLECTION = "notifications";

	private final IOServicesFactory ioServicesFactory;
	private final SolrServerFactory solrServerFactory;
	private final SolrServers solrServers;
	private final ConfigManager configManager;
	private final UniqueIdGenerator idGenerator;
	private final DataLayerConfiguration dataLayerConfiguration;
	private final ContentDao contentDao;
	private final BigVaultLogger bigVaultLogger;
	private final SecondTransactionLogManager secondTransactionLogManager;
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final DataLayerLogger dataLayerLogger;
	private final DataLayerExtensions dataLayerExtensions;

	public DataLayerFactory(IOServicesFactory ioServicesFactory, DataLayerConfiguration dataLayerConfiguration,
			StatefullServiceDecorator statefullServiceDecorator) {

		super(statefullServiceDecorator);
		this.dataLayerExtensions = new DataLayerExtensions();
		this.dataLayerConfiguration = dataLayerConfiguration;
		// TODO Possibility to configure the logger
		this.bigVaultLogger = BigVaultLogger.disabled();
		this.ioServicesFactory = ioServicesFactory;
		this.solrServerFactory = newSolrServerFactory();
		this.solrServers = new SolrServers(solrServerFactory, bigVaultLogger);
		this.dataLayerLogger = new DataLayerLogger();

		this.backgroundThreadsManager = add(new BackgroundThreadsManager(dataLayerConfiguration.getBackgroudThreadsPoolSize()));

		if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.ZOOKEEPER) {
			this.configManager = add(new ZooKeeperConfigManager(dataLayerConfiguration.getSettingsZookeeperAddress(),
					ioServicesFactory.newIOServices()));

		} else if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.FILESYSTEM) {
			this.configManager = add(new FileSystemConfigManager(dataLayerConfiguration.getSettingsFileSystemBaseFolder(),
					ioServicesFactory.newIOServices(), ioServicesFactory.newHashingService()));

		} else {
			throw new ImpossibleRuntimeException("Unsupported ConfigManagerType");
		}

		if (dataLayerConfiguration.getIdGeneratorType() == IdGeneratorType.UUID_V1) {
			this.idGenerator = new UUIDV1Generator();

		} else if (dataLayerConfiguration.getIdGeneratorType() == IdGeneratorType.SEQUENTIAL) {
			this.idGenerator = add(new ZeroPaddedSequentialUniqueIdGenerator(configManager));

		} else {
			throw new ImpossibleRuntimeException("Unsupported UniqueIdGenerator");
		}

		if (ContentDaoType.FILESYSTEM == dataLayerConfiguration.getContentDaoType()) {
			File rootFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
			contentDao = add(new FileSystemContentDao(rootFolder, ioServicesFactory.newIOServices()));

		} else if (ContentDaoType.HADOOP == dataLayerConfiguration.getContentDaoType()) {
			String hadoopUrl = dataLayerConfiguration.getContentDaoHadoopUrl();
			String hadoopUser = dataLayerConfiguration.getContentDaoHadoopUser();
			contentDao = new HadoopContentDao(hadoopUrl, hadoopUser);

		} else {
			throw new ImpossibleRuntimeException("Unsupported ContentDaoType");
		}

		if (dataLayerConfiguration.isSecondTransactionLogEnabled()) {
			secondTransactionLogManager = add(
					new XMLSecondTransactionLogManager(dataLayerConfiguration, ioServicesFactory.newIOServices(),
							newRecordDao(), contentDao, backgroundThreadsManager));
		} else {
			secondTransactionLogManager = null;
		}
	}

	public DataLayerExtensions getExtensions() {
		return dataLayerExtensions;
	}

	public RecordDao newRecordDao() {
		return new BigVaultRecordDao(getRecordsVaultServer(), newTypesFactory(), secondTransactionLogManager, dataLayerLogger);
	}

	public RecordDao newEventsDao() {
		return new BigVaultRecordDao(getEventsVaultServer(), newTypesFactory(), null, dataLayerLogger);
	}

	public RecordDao newNotificationsDao() {
		return new BigVaultRecordDao(getNotificationsVaultServer(), newTypesFactory(), null, dataLayerLogger);
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public ContentDao getContentsDao() {
		return contentDao;
	}

	public BigVaultServer getRecordsVaultServer() {
		return solrServers.getSolrServer(RECORDS_COLLECTION);
	}

	public BigVaultServer getEventsVaultServer() {
		return solrServers.getSolrServer(EVENTS_COLLECTION);
	}

	public BigVaultServer getContentsVaultServer() {
		return solrServers.getSolrServer(CONTENTS_COLLECTION);
	}

	public BigVaultServer getNotificationsVaultServer() {
		return solrServers.getSolrServer(NOTIFICATIONS_COLLECTION);
	}

	public DataStoreTypesFactory newTypesFactory() {
		return new SolrDataStoreTypesFactory();
	}

	public UniqueIdGenerator getUniqueIdGenerator() {
		return idGenerator;
	}

	public DataLayerLogger getDataLayerLogger() {
		return dataLayerLogger;
	}

	public IOServicesFactory getIOServicesFactory() {
		return ioServicesFactory;
	}

	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	public void close() {
		super.close();
		solrServerFactory.clear();
		solrServers.close();
	}

	public SolrServers getSolrServers() {
		return solrServers;
	}

	SolrServerFactory newSolrServerFactory() {
		SolrServerType solrServerType = dataLayerConfiguration.getRecordsDaoSolrServerType();

		if (SolrServerType.HTTP == solrServerType) {
			return newHttpSolrServerFactory();

		} else if (SolrServerType.CLOUD == solrServerType) {
			return newSolrCloudServerFactory();

		} else {
			throw new ImpossibleRuntimeException("Unsupported solr server type");
		}
	}

	private SolrServerFactory newHttpSolrServerFactory() {
		String httpSolrUrl = dataLayerConfiguration.getRecordsDaoHttpSolrServerUrl();
		SolrServerFactory solrServerFactory = new HttpSolrServerFactory(httpSolrUrl, ioServicesFactory);
		if (dataLayerConfiguration.isRecordsDaoHttpSolrServerFaultInjectionEnabled()) {
			solrServerFactory = new FaultInjectorSolrServerFactory(solrServerFactory);
		}
		return solrServerFactory;
	}

	private SolrServerFactory newSolrCloudServerFactory() {
		String zkHost = dataLayerConfiguration.getRecordsDaoCloudSolrServerZKHost();
		return new CloudSolrServerFactory(zkHost);
	}

	public BackgroundThreadsManager getBackgroundThreadsManager() {
		return backgroundThreadsManager;
	}

	public SecondTransactionLogManager getSecondTransactionLogManager() {
		return secondTransactionLogManager;
	}

	public SolrServerFactory getSolrServerFactory() {
		return solrServerFactory;
	}

	public DataLayerConfiguration getDataLayerConfiguration() {
		return dataLayerConfiguration;
	}
}
