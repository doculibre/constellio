package com.constellio.sdk.tests;

import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException.CouldNotExecuteQuery;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.leaderElection.LeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.StandaloneLeaderElectionManager;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.extensions.TransactionLogExtension;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.utils.Factory;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.FakeEncryptionServices;
import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static com.constellio.sdk.tests.SDKConstellioFactoriesInstanceProvider.DEFAULT_NAME;
import static com.constellio.sdk.tests.SaveStateFeature.loadStateFrom;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.spy;

public class FactoriesTestFeatures {

	private boolean fakeEncryptionServices;
	private boolean useSDKPluginFolder;
	//private boolean instanciated = false;
	private boolean backgroundThreadsEnabled = false;
	private boolean checkRollback;
	private List<String> loggingOfRecords = new ArrayList<>();
	private boolean dummyPasswords;

	private File initialState;
	private final FileSystemTestFeatures fileSystemTestFeatures;
	//private ConstellioFactories factoriesInstance;
	private List<Class<?>> spiedClasses = new ArrayList<>();

	private Map<String, TestConstellioFactoriesDecorator> decorators = new HashMap<>();

	//	private Map<String, String> sdkProperties;
	private List<DataLayerConfigurationAlteration> dataLayerConfigurationAlterations = new ArrayList<>();
	private List<ModelLayerConfigurationAlteration> modelLayerConfigurationAlterations = new ArrayList<>();
	private List<AppLayerConfigurationAlteration> appLayerConfigurationAlterations = new ArrayList<>();
	private Map<String, String> configs = new HashMap<>();
	private List<String> instanceNames = new ArrayList<>();
	private String systemLanguage;

	public FactoriesTestFeatures(FileSystemTestFeatures fileSystemTestFeatures, Map<String, String> sdkProperties,
								 boolean checkRollback) {
		this.fileSystemTestFeatures = fileSystemTestFeatures;
		this.checkRollback = checkRollback;
		//		this.sdkProperties = sdkProperties;
		ConstellioFactories.instanceProvider = new SDKConstellioFactoriesInstanceProvider();
	}

	public List<Runnable> afterTest(boolean restarting) {

		List<Runnable> runtimes = new ArrayList<>();


		if (ConstellioFactories.instanceProvider.isInitialized()) {
			if (!restarting) {
				runtimes.addAll(clear());
			}
		}

		ConstellioFactories.clear();

		return runtimes;

	}

	public List<Runnable> clear() {
		List<Runnable> runnables = new ArrayList<>();
		SDKConstellioFactoriesInstanceProvider instanceProvider = (SDKConstellioFactoriesInstanceProvider) ConstellioFactories.instanceProvider;

		for (ConstellioFactories factoriesInstance : instanceProvider.instances.values()) {

			final File licenseFile = factoriesInstance.getFoldersLocator().getLicenseFile();
			runnables.add(new Runnable() {
				@Override
				public void run() {
					if (licenseFile.exists()) {
						licenseFile.delete();
					}
				}
			});

			final DataLayerConfiguration conf = factoriesInstance.getDataLayerConfiguration();
			for (final BigVaultServer server : factoriesInstance.getDataLayerFactory().getSolrServers().getServers()) {
				//
				runnables.add(new Runnable() {
					@Override
					public void run() {
						deleteServerRecords(server);
						server.getSolrServerFactory().clear();
						CloseableUtils.closeQuietly(server.getNestedSolrServer());
					}
				});
			}

			if (ConfigManagerType.ZOOKEEPER == conf.getSettingsConfigType()) {

				runnables.add(new Runnable() {
					@Override
					public void run() {
						deleteFromZooKeeper(conf.getSettingsZookeeperAddress());
					}
				});
			}
		}

		deleteFromCaches();

		runnables.add(new Runnable() {
			@Override
			public void run() {
				i18n.clearBundles();
			}
		});

		return runnables;
	}

	private void deleteFromCaches() {
		try {
			ConstellioCacheManager settingsCacheManager = getConstellioFactories().getDataLayerFactory()
					.getLocalCacheManager();
			if (settingsCacheManager != null) {
				for (String cacheName : settingsCacheManager.getCacheNames()) {
					ConstellioCache cache = settingsCacheManager.getCache(cacheName);
					cache.clear();
				}
			}
			ConstellioCacheManager recordsCacheManager = getConstellioFactories().getDataLayerFactory().getDistributedCacheManager();
			if (recordsCacheManager != null) {
				for (String cacheName : recordsCacheManager.getCacheNames()) {
					ConstellioCache cache = recordsCacheManager.getCache(cacheName);
					cache.clear();
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void deleteFromZooKeeper(String address) {
		CuratorFramework client = null;
		try {
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
			client = CuratorFrameworkFactory.newClient(address, retryPolicy);
			client.start();
			client.delete().deletingChildrenIfNeeded().forPath("/constellio");
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			CloseableUtils.closeQuietly(client);
		}
	}

	private void deleteServerRecords(BigVaultServer server) {

		BigVaultServer vaultServer = server.clone();
		vaultServer.unregisterAllListeners();
		vaultServer.disableLogger();
		vaultServer.setExtensions(new DataLayerSystemExtensions());

		clearSolrData(vaultServer, 0);
	}

	private void clearSolrData(BigVaultServer vaultServer, int attempt) {
		try {
			vaultServer.softCommit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		}
		ModifiableSolrParams allRecordsSolrParams = new ModifiableSolrParams();
		allRecordsSolrParams.set("q", "*:*");

		try {
			vaultServer
					.addAll(new BigVaultServerTransaction(NOW).addDeletedQuery("*:*"));

			if (!vaultServer.query(allRecordsSolrParams).getResults().isEmpty()) {
				if (attempt < 10) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
					clearSolrData(vaultServer, attempt + 1);
				} else {
					throw new RuntimeException("Invalid solr core initial state ");
				}
			}
		} catch (CouldNotExecuteQuery couldNotExecuteQuery) {
			throw new RuntimeException(couldNotExecuteQuery);
		} catch (BigVaultException e) {
			throw new RuntimeException("Cannot deleteLogically by query *:*");
		} finally {
			vaultServer.getSolrServerFactory().clear();
			CloseableUtils.closeQuietly(vaultServer.getNestedSolrServer());
		}
	}


	public void restart() {
		SDKConstellioFactoriesInstanceProvider instanceProvider = (SDKConstellioFactoriesInstanceProvider) ConstellioFactories.instanceProvider;
		instanceProvider.clear();
		getConstellioFactories();
	}

	public synchronized ConstellioFactories getConstellioFactories() {
		return getConstellioFactories(DEFAULT_NAME);
	}

	public synchronized ConstellioFactories getConstellioFactories(final String name) {
		TestConstellioFactoriesDecorator decorator = decorators.get(name);
		if (decorator == null) {

			StringBuilder setupPropertiesContent = new StringBuilder();
			setupPropertiesContent.append("admin.servicekey=adminkey\n");
			setupPropertiesContent.append("admin.password=password\n");
			File setupProperties = fileSystemTestFeatures.newTempFileWithContent(setupPropertiesContent.toString());

			decorator = new TestConstellioFactoriesDecorator(backgroundThreadsEnabled, true, checkRollback) {

				@Override
				public DataLayerFactory decorateDataLayerFactory(DataLayerFactory dataLayerFactory) {

					if (!loggingOfRecords.isEmpty()) {
						dataLayerFactory.getDataLayerLogger().setMonitoredIds(loggingOfRecords);
					}

					dataLayerFactory.getExtensions().getSystemWideExtensions().getTransactionLogExtensions()
							.add(new TransactionLogExtension() {
								@Override
								public ExtensionBooleanResult isDocumentFieldLoggedInTransactionLog(String field,
																									String schema,
																									String collection) {
									return ExtensionBooleanResult.FORCE_TRUE;
								}
							});

					if (spiedClasses.isEmpty()) {
						return dataLayerFactory;
					} else {
						return spy(dataLayerFactory);
					}
				}

				@Override
				public ModelLayerFactory decorateModelServicesFactory(final ModelLayerFactory modelLayerFactory) {

					if (spiedClasses.isEmpty()) {
						return modelLayerFactory;
					} else {
						return spy(modelLayerFactory);
					}
				}

				@Override
				public AppLayerFactory decorateAppServicesFactory(final AppLayerFactory appLayerFactory) {

					if (dummyPasswords) {
						appLayerFactory.add(new StatefulService() {
							@Override
							public void initialize() {
								try {
									ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
									List<UserCredential> users = modelLayerFactory.newUserServices().getAllUserCredentials();
									StringBuilder passwordFileContent = new StringBuilder();
									for (UserCredential user : users) {
										passwordFileContent.append(user.getUsername() + "=W6ph5Mm5Pz8GgiULbPgzG37mj9g\\=\n");
									}
									File settingsFolder = modelLayerFactory.getDataLayerFactory().getDataLayerConfiguration()
											.getSettingsFileSystemBaseFolder();
									File authenticationFile = new File(settingsFolder, "authentification.properties");
									try {
										FileUtils.write(authenticationFile, passwordFileContent.toString());
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								} catch (Exception e) {
									//e.printStackTrace();
								}
							}

							@Override
							public void close() {

							}
						});
					}

					if (spiedClasses.isEmpty()) {
						return appLayerFactory;
					} else {
						return spy(appLayerFactory);
					}
				}

				@Override
				public StatefullServiceDecorator getStatefullServiceDecorator() {
					return new StatefullServiceDecorator() {
						@Override
						public <T> T decorate(T service) {

							if (service != null && ConstellioPluginManager.class.isAssignableFrom(service.getClass())) {
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

						@Override
						public <T> void afterInitialize(T service) {
							if (service instanceof ConfigManager && importedSettings != null) {
								((ConfigManager) service).importFrom(importedSettings);
							}
						}
					};

				}
			};

			if (fakeEncryptionServices) {
				modelLayerConfigurationAlterations.add(new ModelLayerConfigurationAlteration() {
					@Override
					public void alter(InMemoryModelLayerConfiguration configuration) {
						Factory<EncryptionServices> encryptionServicesFactory = new Factory<EncryptionServices>() {
							@Override
							public EncryptionServices get() {
								return new FakeEncryptionServices();
							}
						};
						configuration.setEncryptionServicesFactory(encryptionServicesFactory);
					}
				});
			}

			File configManagerFolder = fileSystemTestFeatures.newTempFolderWithName("configManagerFolder");
			File contentFolder = fileSystemTestFeatures.newTempFolderWithName("contentFolder");
			File pluginsFolder;
			if (useSDKPluginFolder) {
				pluginsFolder = new SDKFoldersLocator().getPluginsJarsFolder();
			} else {
				pluginsFolder = fileSystemTestFeatures.newTempFolderWithName("plugins");
			}
			File tlogWorkFolder = fileSystemTestFeatures.newTempFolderWithName("tlogWorkFolder");

			decorator.setDataLayerConfigurationAlterations(dataLayerConfigurationAlterations);
			decorator.setModelLayerConfigurationAlterations(modelLayerConfigurationAlterations);
			decorator.setAppLayerConfigurationAlterations(appLayerConfigurationAlterations);
			decorator.setSetupProperties(setupProperties);
			decorator.setImportationFolder(fileSystemTestFeatures.newTempFolderWithName("importationFolder"));
			decorator.setConfigManagerFolder(configManagerFolder);
			decorator.setAppTempFolder(fileSystemTestFeatures.newTempFolderWithName("appTempFolder"));
			decorator.setContentFolder(contentFolder);
			decorator.setPluginsFolder(pluginsFolder)
					.setPluginsToMoveOnStartupFile(fileSystemTestFeatures.newTempFileWithContent(""));
			decorator.setTransactionLogWorkFolder(tlogWorkFolder);
			decorator.setSystemLanguage(systemLanguage);

			if (initialState != null) {
				if (!ConstellioTest.isCurrentPreservingState()) {

					File tempFolder = fileSystemTestFeatures.newTempFolder();
					try {
						File tempUnzipSettingsFolder = loadStateFrom(initialState, tempFolder, configManagerFolder, contentFolder,
								pluginsFolder, tlogWorkFolder, dummyPasswords);
						decorator.importSettings(tempUnzipSettingsFolder);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}

				}
			}
			decorators.put(name, decorator);
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

		final File finalPropertyFile = propertyFile;
		final TestConstellioFactoriesDecorator finalDecorator = decorator;
		SDKConstellioFactoriesInstanceProvider instanceProvider = (SDKConstellioFactoriesInstanceProvider) ConstellioFactories.instanceProvider;

		ConstellioFactories constellioFactories = instanceProvider.getInstance(new Factory<ConstellioFactories>() {
			@Override
			public ConstellioFactories get() {

				short instanceId = 0;
				if (name != null) {
					if (!instanceNames.contains(name)) {
						instanceNames.add(name);
					}
					instanceId = (short) instanceNames.indexOf(name);
				}

				ConstellioFactories instance = ConstellioFactories.buildFor(finalPropertyFile, finalDecorator, name, instanceId);
				LeaderElectionManager electionService = instance.getDataLayerFactory().getLeaderElectionService().getNestedLeaderElectionManager();
				if (electionService instanceof StandaloneLeaderElectionManager) {
					((StandaloneLeaderElectionManager) electionService).setLeader(DEFAULT_NAME.equals(name));
				}
				return instance;
			}
		}, name);

		return constellioFactories;

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

	public DataLayerFactory newDaosFactory(String name) {
		return getConstellioFactories(name).getDataLayerFactory();
	}

	public IOServicesFactory newIOServicesFactory(String name) {
		return getConstellioFactories(name).getIoServicesFactory();
	}

	public ModelLayerFactory newModelServicesFactory(String name) {
		return getConstellioFactories(name).getModelLayerFactory();
	}

	public AppLayerFactory newAppServicesFactory(String name) {
		return getConstellioFactories(name).getAppLayerFactory();
	}

	public FoldersLocator getFoldersLocator(String name) {
		return getConstellioFactories(name).getFoldersLocator();
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

	public FactoriesTestFeatures withLoggingOfRecords(String... loggingOfRecordsArray) {
		loggingOfRecords.addAll(asList(loggingOfRecordsArray));
		return this;
	}

	public boolean isInitialized() {
		return ConstellioFactories.instanceProvider.isInitialized();
	}

	public void givenBackgroundThreadsEnabled() {
		backgroundThreadsEnabled = true;
	}

	public FactoriesTestFeatures withoutCheckForRollback() {
		checkRollback = false;
		return this;
	}

	public FactoriesTestFeatures withFakeEncryptionServices() {
		fakeEncryptionServices = true;
		return this;
	}

	public FactoriesTestFeatures withSDKPluginFolder() {
		useSDKPluginFolder = true;
		return this;
	}

	public boolean isCheckRollback() {
		return checkRollback;
	}

}
