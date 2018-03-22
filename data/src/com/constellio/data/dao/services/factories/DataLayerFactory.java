package com.constellio.data.dao.services.factories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.constellio.data.service.background.DataLayerBackgroundThreadsManager;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.constellio.data.conf.CacheType;
import com.constellio.data.conf.ConfigManagerType;
import com.constellio.data.conf.ContentDaoType;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.conf.SolrServerType;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordDeltaDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.StatefullServiceDecorator;
import com.constellio.data.dao.managers.config.CachedConfigManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.managers.config.ZooKeeperConfigManager;
import com.constellio.data.dao.services.DataLayerLogger;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.RecordDaoException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultException;
import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ignite.ConstellioIgniteCacheManager;
import com.constellio.data.dao.services.cache.map.ConstellioMapCacheManager;
import com.constellio.data.dao.services.cache.serialization.SerializationCheckCacheManager;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.contents.HadoopContentDao;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.dao.services.ignite.DefaultLeaderElectionServiceImpl;
import com.constellio.data.dao.services.ignite.LeaderElectionService;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.dao.services.sequence.SolrSequencesManager;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.data.dao.services.solr.serverFactories.CloudSolrServerFactory;
import com.constellio.data.dao.services.solr.serverFactories.HttpSolrServerFactory;
import com.constellio.data.dao.services.transactionLog.KafkaTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.dao.services.transactionLog.XMLSecondTransactionLogManager;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.io.ConversionManager;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.test.FaultInjectorSolrServerFactory;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.threads.ConstellioJobManager;
import com.constellio.data.utils.ImpossibleRuntimeException;

public class DataLayerFactory extends LayerFactoryImpl {

	private static final String RECORDS_SEQUENCE_TABLE_CONFIG_PATH = "/sequence.properties";
	private static final String SECONDARY_SEQUENCE_TABLE_CONFIG_PATH = "/secondarySequence.properties";
	static final String RECORDS_COLLECTION = "records";
	static final String EVENTS_COLLECTION = "events";
	static final String CONTENTS_COLLECTION = "contents";
	static final String NOTIFICATIONS_COLLECTION = "notifications";

	private final IOServicesFactory ioServicesFactory;
	private final SolrServers solrServers;
	private ConstellioCacheManager settingsCacheManager;
	private ConstellioCacheManager recordsCacheManager;
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
	final TransactionLogRecoveryManager transactionLogRecoveryManager;
	private String constellioVersion;
	private final ConversionManager conversionManager;
	private static DataLayerFactory lastCreatedInstance;

	private DataLayerBackgroundThreadsManager dataLayerBackgroundThreadsManager;

	public static int countConstructor;

	public static int countInit;

	public DataLayerFactory(IOServicesFactory ioServicesFactory, DataLayerConfiguration dataLayerConfiguration,
			StatefullServiceDecorator statefullServiceDecorator, String instanceName, String warVersion) {

		super(statefullServiceDecorator, instanceName);
		countConstructor++;
		if (countConstructor >= 2) {
			new IllegalStateException("Problemo : DataLayerFactory has been constructed " + countConstructor + " times")
					.printStackTrace();
		}

		this.dataLayerExtensions = new DataLayerExtensions();
		this.dataLayerConfiguration = dataLayerConfiguration;
		// TODO Possibility to configure the logger
		this.bigVaultLogger = BigVaultLogger.disabled();
		this.ioServicesFactory = ioServicesFactory;
		this.solrServers = new SolrServers(newSolrServerFactory(), bigVaultLogger, dataLayerExtensions);
		this.dataLayerLogger = new DataLayerLogger();

		this.backgroundThreadsManager = add(new BackgroundThreadsManager(dataLayerConfiguration, this));

		constellioJobManager = add(new ConstellioJobManager(dataLayerConfiguration));

		if (dataLayerConfiguration.getCacheType() == CacheType.IGNITE) {
			settingsCacheManager = new ConstellioIgniteCacheManager(dataLayerConfiguration.getCacheUrl(), warVersion);
			recordsCacheManager = new ConstellioIgniteCacheManager(dataLayerConfiguration.getCacheUrl(), warVersion);
		} else if (dataLayerConfiguration.getCacheType() == CacheType.MEMORY) {
			settingsCacheManager = new ConstellioMapCacheManager(dataLayerConfiguration);
			recordsCacheManager = new ConstellioMapCacheManager(dataLayerConfiguration);
		} else if (dataLayerConfiguration.getCacheType() == CacheType.TEST) {
			settingsCacheManager = new SerializationCheckCacheManager(dataLayerConfiguration);
			recordsCacheManager = new SerializationCheckCacheManager(dataLayerConfiguration);
		} else {
			throw new ImpossibleRuntimeException("Unsupported CacheConfigManager");
		}
		add(settingsCacheManager);
		add(recordsCacheManager);




		ConfigManager configManagerWithoutCache;
		if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.ZOOKEEPER) {
			configManagerWithoutCache = add(new ZooKeeperConfigManager(dataLayerConfiguration.getSettingsZookeeperAddress(), "/",
					ioServicesFactory.newIOServices()));

		} else if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.FILESYSTEM) {
			configManagerWithoutCache = add(new FileSystemConfigManager(dataLayerConfiguration.getSettingsFileSystemBaseFolder(),
					ioServicesFactory.newIOServices(),
					ioServicesFactory.newHashingService(dataLayerConfiguration.getHashingEncoding()),
					settingsCacheManager.getCache(FileSystemConfigManager.class.getName()), dataLayerExtensions));

		} else {
			throw new ImpossibleRuntimeException("Unsupported ConfigManagerType");
		}
		configManager = new CachedConfigManager(configManagerWithoutCache, settingsCacheManager.getCache("configManager"));

		if (dataLayerConfiguration.getIdGeneratorType() == IdGeneratorType.UUID_V1) {
			this.idGenerator = new UUIDV1Generator();

		} else if (dataLayerConfiguration.getIdGeneratorType() == IdGeneratorType.SEQUENTIAL) {
			this.idGenerator = add(new ZeroPaddedSequentialUniqueIdGenerator(configManager, RECORDS_SEQUENCE_TABLE_CONFIG_PATH));

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

		updateContentDao();

		transactionLogRecoveryManager = new TransactionLogRecoveryManager(this);

		if (dataLayerConfiguration.isSecondTransactionLogEnabled()) {
			if ("kafka".equals(dataLayerConfiguration.getSecondTransactionLogMode())) {
				secondTransactionLogManager = add(new KafkaTransactionLogManager(dataLayerConfiguration,
						dataLayerExtensions.getSystemWideExtensions(), newRecordDao(), dataLayerLogger));
			} else {
				secondTransactionLogManager = add(new XMLSecondTransactionLogManager(dataLayerConfiguration,
						ioServicesFactory.newIOServices(), newRecordDao(), contentDao, backgroundThreadsManager, dataLayerLogger,
						dataLayerExtensions.getSystemWideExtensions(), transactionLogRecoveryManager));
			}
		} else {
			secondTransactionLogManager = null;
		}

		IOServices ioServices = ioServicesFactory.newIOServices();
		conversionManager = add(new ConversionManager(ioServices, dataLayerConfiguration.getConversionProcesses(),
				dataLayerConfiguration.getOnlineConversionUrl(), this.getExtensions().getSystemWideExtensions()));
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

	public LeaderElectionService getLeaderElectionService() {
		return new DefaultLeaderElectionServiceImpl(this);
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

	public ConstellioCacheManager getSettingsCacheManager() {
		return settingsCacheManager;
	}

	public ConstellioCacheManager getRecordsCacheManager() {
		return recordsCacheManager;
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
		countInit++;

		if (countInit >= 2) {
			new IllegalStateException("Problemo : DataLayerFactory has been initialized " + countInit + " times")
					.printStackTrace();
		}
		super.initialize();

		newRecordDao().removeOldLocks();
	}

	@Override
	public void close() {
		super.close();
		solrServers.close();
	}

	@Override
	public void close(boolean closeBottomLayers) {
		super.close(closeBottomLayers);
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
		RecordDTO record = new RecordDTO("the_private_key", -1L, null, fields);
		try {
			recordDao.execute(new TransactionDTO(UUID.randomUUID().toString(), RecordsFlushing.NOW, Arrays.asList(record),
					new ArrayList<RecordDeltaDTO>()));
		} catch (RecordDaoException.OptimisticLocking e) {
			throw new RuntimeException(e);
		}
	}

	public TransactionLogRecoveryManager getTransactionLogRecoveryManager() {
		return this.transactionLogRecoveryManager;
	}

	public SequencesManager getSequencesManager() {
		return new SolrSequencesManager(newRecordDao(), secondTransactionLogManager);
	}

	public void updateContentDao() {
		if (ContentDaoType.FILESYSTEM == dataLayerConfiguration.getContentDaoType()) {
			contentDao = add(new FileSystemContentDao(ioServicesFactory.newIOServices(), dataLayerConfiguration));
		} else if (ContentDaoType.HADOOP == dataLayerConfiguration.getContentDaoType()) {
			String hadoopUrl = dataLayerConfiguration.getContentDaoHadoopUrl();
			String hadoopUser = dataLayerConfiguration.getContentDaoHadoopUser();
			contentDao = new HadoopContentDao(hadoopUrl, hadoopUser);

		} else {
			throw new ImpossibleRuntimeException("Unsupported ContentDaoType");
		}
	}

	public ConversionManager getConversionManager() {
		return conversionManager;
	}

}
