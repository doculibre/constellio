package com.constellio.data.dao.services.factories;

import com.constellio.data.conf.CacheType;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.EventBusSendingServiceType;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.conf.SecondTransactionLogType;
import com.constellio.data.conf.SolrServerType;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.SolrRecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.CachedConfigManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.managers.config.ZooKeeperConfigManager;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.BigVaultSearchDao;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.event.ConstellioEventMapCacheManager;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
import com.constellio.data.dao.services.cache.map.ConstellioMapCacheManager;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCacheManager;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.dao.services.leaderElection.IgniteLeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.LeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.ObservableLeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.StandaloneLeaderElectionManager;
import com.constellio.data.dao.services.leaderElection.ZookeeperLeaderElectionManager;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogRecovery;
import com.constellio.data.dao.services.recovery.TransactionLogXmlRecoveryManager;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.dao.services.sequence.SolrSequencesManager;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.data.dao.services.solr.serverFactories.CloudSolrServerFactory;
import com.constellio.data.dao.services.solr.serverFactories.HttpSolrServerFactory;
import com.constellio.data.dao.services.sql.SqlConnector;
import com.constellio.data.dao.services.sql.SqlRecordDaoFactory;
import com.constellio.data.dao.services.sql.SqlServerConnector;
import com.constellio.data.dao.services.transactionLog.KafkaTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.SqlServerTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.XMLSecondTransactionLogManager;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusManager;
import com.constellio.data.events.EventBusSendingService;
import com.constellio.data.events.SolrEventBusSendingService;
import com.constellio.data.events.StandaloneEventBusSendingService;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.service.background.DataLayerBackgroundThreadsManager;
import com.constellio.data.test.FaultInjectorSolrServerFactory;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.threads.ConstellioJobManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.ignite.Ignite;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.constellio.data.conf.ElectionServiceType.IGNITE;
import static com.constellio.data.conf.ElectionServiceType.ZOOKEEPER;
import static com.constellio.data.events.EventBusEventsExecutionStrategy.ONLY_SENT_REMOTELY;

public class DataLayerFactory extends LayerFactoryImpl {

	private static final String RECORDS_SEQUENCE_TABLE_CONFIG_PATH = "/sequence.properties";
	private static final String SECONDARY_SEQUENCE_TABLE_CONFIG_PATH = "/secondarySequence.properties";
	static final String RECORDS_COLLECTION = "records";
	static final String EVENTS_COLLECTION = "events";
	static final String SEARCH_COLLECTION = "search";
	static final String CONTENTS_COLLECTION = "contents";
	static final String NOTIFICATIONS_COLLECTION = "notifications";

	private final IOServicesFactory ioServicesFactory;
	private final SolrServers solrServers;
	private final SqlConnector sqlConnector;
	private SqlRecordDaoFactory sqlRecordDaoFactory;
	private ConstellioCacheManager localCacheManager;
	private ConstellioCacheManager distributedCacheManager;
	private final ConfigManager configManager;
	private final UniqueIdGenerator idGenerator;
	private final UniqueIdGenerator secondaryIdGenerator;
	private final DataLayerConfiguration dataLayerConfiguration;
	private ContentDao contentDao;
	private final BigVaultLogger bigVaultLogger;
	private final SecondTransactionLogManager secondTransactionLogManager;
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final ConstellioJobManager constellioJobManager;
	private final DataLayerLogger dataLayerLogger;
	private final DataLayerExtensions dataLayerExtensions;
	final TransactionLogRecovery transactionLogXmlRecoveryManager;
	private String constellioVersion;
	private final ConversionManager conversionManager;
	private final EventBusManager eventBusManager;
	private static DataLayerFactory lastCreatedInstance;
	private final ObservableLeaderElectionManager leaderElectionManager;

	private DataLayerBackgroundThreadsManager dataLayerBackgroundThreadsManager;

	private Ignite igniteClient;
	private CuratorFramework curatorFramework;


	public DataLayerFactory(IOServicesFactory ioServicesFactory, DataLayerConfiguration dataLayerConfiguration,
							StatefullServiceDecorator statefullServiceDecorator, String instanceName, short instanceId,
							String warVersion) {

		super(statefullServiceDecorator, instanceName, instanceId);

		this.dataLayerExtensions = new DataLayerExtensions();
		this.dataLayerConfiguration = dataLayerConfiguration;
		// TODO Possibility to configure the logger
		this.bigVaultLogger = BigVaultLogger.disabled();
		this.ioServicesFactory = ioServicesFactory;
		this.solrServers = new SolrServers(newSolrServerFactory(), bigVaultLogger, dataLayerExtensions);
		this.dataLayerLogger = new DataLayerLogger();
		this.sqlConnector = new SqlServerConnector();

		this.backgroundThreadsManager = add(new BackgroundThreadsManager(dataLayerConfiguration, this));

		LeaderElectionManager leaderElectionManager;
		if (dataLayerConfiguration.getElectionServiceType() == ZOOKEEPER) {
			leaderElectionManager = add(new ZookeeperLeaderElectionManager(this));

		} else if (dataLayerConfiguration.getElectionServiceType() == IGNITE) {
			leaderElectionManager = add(new IgniteLeaderElectionManager(this));

		} else {
			leaderElectionManager = add(new StandaloneLeaderElectionManager());
		}
		this.leaderElectionManager = new ObservableLeaderElectionManager(leaderElectionManager);

		EventBusSendingService eventBusSendingService = new StandaloneEventBusSendingService();
		if (EventBusSendingServiceType.SOLR.equals(dataLayerConfiguration.getEventBusSendingServiceType())) {
			SolrEventBusSendingService solrEventBusSendingService = new SolrEventBusSendingService(
					getNotificationsVaultServer().getNestedSolrServer());
			solrEventBusSendingService.setEventLifespan(dataLayerConfiguration.getSolrEventBusSendingServiceTypeEventLifespan());
			solrEventBusSendingService.setPollAndRetrieveFrequency(
					dataLayerConfiguration.getSolrEventBusSendingServiceTypePollAndRetrieveFrequency());
			eventBusSendingService = solrEventBusSendingService;
		}

		this.eventBusManager = add(new EventBusManager(eventBusSendingService, dataLayerExtensions.getSystemWideExtensions()));

		constellioJobManager = add(new ConstellioJobManager(dataLayerConfiguration));


		if (dataLayerConfiguration.getCacheType() == CacheType.MEMORY) {

			if (dataLayerConfiguration.getCacheUrl() != null) {
				ConstellioIgniteCacheManager cacheManager = new ConstellioIgniteCacheManager(dataLayerConfiguration.getCacheUrl(),
						warVersion);
				cacheManager.initialize();
				igniteClient = cacheManager.getClient();
			}

			localCacheManager = new ConstellioMapCacheManager(dataLayerConfiguration);
			distributedCacheManager = new ConstellioEventMapCacheManager(eventBusManager);

		} else if (dataLayerConfiguration.getCacheType() == CacheType.TEST) {
			localCacheManager = new SerializationCheckCacheManager(dataLayerConfiguration);
			distributedCacheManager = new SerializationCheckCacheManager(dataLayerConfiguration);
		} else {
			throw new ImpossibleRuntimeException("Unsupported CacheConfigManager");
		}
		add(localCacheManager);
		add(distributedCacheManager);

		EventBus configManagerEventBus = eventBusManager.createEventBus("configManager", ONLY_SENT_REMOTELY);
		ConfigManager configManagerWithoutCache;
		if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.ZOOKEEPER) {
			configManagerWithoutCache = add(new ZooKeeperConfigManager(dataLayerConfiguration.getSettingsZookeeperAddress(), "/",
					ioServicesFactory.newIOServices(), configManagerEventBus));

		} else if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.FILESYSTEM) {
			configManagerWithoutCache = add(new FileSystemConfigManager(dataLayerConfiguration.getSettingsFileSystemBaseFolder(),
					ioServicesFactory.newIOServices(),
					ioServicesFactory.newHashingService(dataLayerConfiguration.getHashingEncoding()),
					localCacheManager.getCache(FileSystemConfigManager.class.getName()), dataLayerExtensions,
					configManagerEventBus));

		} else {
			throw new ImpossibleRuntimeException("Unsupported ConfigManagerType");
		}

		configManager = new CachedConfigManager(configManagerWithoutCache, distributedCacheManager.getCache("configManager"));

		if (dataLayerConfiguration.getIdGeneratorType() == IdGeneratorType.UUID_V1) {
			this.idGenerator = new UUIDV1Generator();

		} else if (dataLayerConfiguration.getIdGeneratorType() == IdGeneratorType.SEQUENTIAL) {
			this.idGenerator = add(new ZeroPaddedSequentialUniqueIdGenerator(configManager,
					RECORDS_SEQUENCE_TABLE_CONFIG_PATH, dataLayerConfiguration.getSequentialIdReservedBatchSize()));

		} else {
			throw new ImpossibleRuntimeException("Unsupported UniqueIdGenerator");
		}

		if (dataLayerConfiguration.getSecondaryIdGeneratorType() == IdGeneratorType.UUID_V1) {
			this.secondaryIdGenerator = new UUIDV1Generator();

		} else if (dataLayerConfiguration.getSecondaryIdGeneratorType() == IdGeneratorType.SEQUENTIAL) {
			this.secondaryIdGenerator = add(
					new ZeroPaddedSequentialUniqueIdGenerator(configManager, SECONDARY_SEQUENCE_TABLE_CONFIG_PATH));

		} else {
			throw new ImpossibleRuntimeException("Unsupported UniqueIdGenerator");
		}

		if (dataLayerConfiguration.getMicrosoftSqlServerUrl() != null &&
			dataLayerConfiguration.getSecondTransactionLogMode() == SecondTransactionLogType.SQL_SERVER) {
			try {
				this.sqlConnector.setConnection(dataLayerConfiguration);
				this.sqlRecordDaoFactory = new SqlRecordDaoFactory(this.sqlConnector);
			} catch (SQLException sqlException) {
				throw new RuntimeException(sqlException);
			}
		}

		updateContentDao();

		transactionLogXmlRecoveryManager = new TransactionLogXmlRecoveryManager(this);

		if (dataLayerConfiguration.isSecondTransactionLogEnabled()) {
			if (dataLayerConfiguration.getSecondTransactionLogMode() == SecondTransactionLogType.KAFKA) {
				secondTransactionLogManager = add(new KafkaTransactionLogManager(dataLayerConfiguration,
						dataLayerExtensions.getSystemWideExtensions(), newRecordDao(), dataLayerLogger));
			} else if (dataLayerConfiguration.getSecondTransactionLogMode() == SecondTransactionLogType.SQL_SERVER) {

				secondTransactionLogManager = add(new SqlServerTransactionLogManager(dataLayerConfiguration,
						ioServicesFactory.newIOServices(), newRecordDao(), sqlRecordDaoFactory, contentDao, backgroundThreadsManager, dataLayerLogger,
						dataLayerExtensions.getSystemWideExtensions(), transactionLogXmlRecoveryManager, this.leaderElectionManager));
			} else {
				secondTransactionLogManager = add(new XMLSecondTransactionLogManager(dataLayerConfiguration,
						ioServicesFactory.newIOServices(), newRecordDao(), contentDao, backgroundThreadsManager, dataLayerLogger,
						dataLayerExtensions.getSystemWideExtensions(), transactionLogXmlRecoveryManager));
			}
		} else {
			secondTransactionLogManager = null;
		}

		IOServices ioServices = ioServicesFactory.newIOServices();
		conversionManager = add(new ConversionManager(ioServices, dataLayerConfiguration.getConversionProcesses(),
				dataLayerConfiguration.getOnlineConversionUrl(), this.getExtensions().getSystemWideExtensions(), dataLayerConfiguration));
		lastCreatedInstance = this;

		dataLayerBackgroundThreadsManager = new DataLayerBackgroundThreadsManager(this);

		add(dataLayerBackgroundThreadsManager);
	}

	public static DataLayerFactory getLastCreatedInstance() {
		return lastCreatedInstance;
	}

	public void setConstellioVersion(String constellioVersion) {
		this.constellioVersion = constellioVersion;
	}

	public ObservableLeaderElectionManager getLeaderElectionService() {
		return leaderElectionManager;
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

	public RecordDao newSearchDao() {
		return new BigVaultSearchDao(getSearchVaultServer(), newTypesFactory(), null, dataLayerLogger);
	}

	public RecordDao newNotificationsDao() {
		return new BigVaultRecordDao(getNotificationsVaultServer(), newTypesFactory(), null, dataLayerLogger);
	}

	public ConfigManager getConfigManager() {
		return configManager;
	}

	public ConstellioCacheManager getLocalCacheManager() {
		return localCacheManager;
	}

	public ConstellioCacheManager getDistributedCacheManager() {
		return distributedCacheManager;
	}

	public ContentDao getContentsDao() {
		return contentDao;
	}

	public SqlRecordDaoFactory getSqlRecordDao() {
		return sqlRecordDaoFactory;
	}

	public BigVaultServer getRecordsVaultServer() {
		return solrServers.getSolrServer(RECORDS_COLLECTION);
	}

	public BigVaultServer getEventsVaultServer() {
		return solrServers.getSolrServer(EVENTS_COLLECTION);
	}

	public BigVaultServer getSearchVaultServer() {
		return solrServers.getSolrServer(SEARCH_COLLECTION);
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

	public UniqueIdGenerator getSecondaryUniqueIdGenerator() {
		return secondaryIdGenerator;
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

		newRecordDao().removeOldLocks();
	}

	@Override
	public void close() {
		super.close();
		try {
			sqlConnector.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		solrServers.close();
	}

	@Override
	public void close(boolean closeBottomLayers) {
		super.close(closeBottomLayers);
		try {
			sqlConnector.closeConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		solrServers.close();
	}

	public SolrServers getSolrServers() {
		return solrServers;
	}

	private SolrServerFactory newSolrServerFactory() {
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

	public ConstellioJobManager getConstellioJobManager() {
		return constellioJobManager;
	}

	public SecondTransactionLogManager getSecondTransactionLogManager() {
		return secondTransactionLogManager;
	}

	public DataLayerConfiguration getDataLayerConfiguration() {
		return dataLayerConfiguration;
	}

	public String readEncryptionKey()
			throws BigVaultException {
		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "id:the_private_key");
		SolrDocument solrDocument = getRecordsVaultServer().querySingleResult(params);
		return (String) solrDocument.getFieldValue("value_s");
	}

	public void saveEncryptionKey() {
		String solrKeyPart = dataLayerConfiguration.createRandomUniqueKey();

		RecordDao recordDao = newRecordDao();
		Map<String, Object> fields = new HashMap<>();
		fields.put("value_s", solrKeyPart);
		RecordDTO record = new SolrRecordDTO("the_private_key", -1L, fields, RecordDTOMode.FULLY_LOADED);
		try {
			recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(record),
					new ArrayList<RecordDeltaDTO>()));
		} catch (RecordDaoException.OptimisticLocking e) {
			throw new RuntimeException(e);
		}
	}

	public TransactionLogRecovery getTransactionLogXmlRecoveryManager() {
		return this.transactionLogXmlRecoveryManager;
	}

	public SequencesManager getSequencesManager() {
		return new SolrSequencesManager(newRecordDao(), secondTransactionLogManager);
	}

	public void updateContentDao() {
		if (ContentDaoType.FILESYSTEM == dataLayerConfiguration.getContentDaoType()) {
			contentDao = add(new FileSystemContentDao(this, dataLayerConfiguration.getSubvaults()));

		} else {
			throw new ImpossibleRuntimeException("Unsupported ContentDaoType");
		}
	}

	public ConversionManager getConversionManager() {
		return conversionManager;
	}

	public EventBusManager getEventBusManager() {
		return eventBusManager;
	}

	public Ignite getIgniteClient() {
		return igniteClient;
	}

	public CuratorFramework getCuratorFramework() {
		return ZooKeeperConfigManager.getInstance(dataLayerConfiguration.getSettingsZookeeperAddress());
	}

	public boolean isDistributed() {
		return !(leaderElectionManager.getNestedLeaderElectionManager() instanceof StandaloneLeaderElectionManager);
	}

}
