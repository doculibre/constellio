package com.constellio.data.dao.services.factories;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

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
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.contents.HadoopContentDao;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.idGenerator.ZeroPaddedSequentialUniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.recovery.TransactionLogRecoveryManager;
import com.constellio.data.dao.services.sequence.SequencesManager;
import com.constellio.data.dao.services.sequence.SolrSequencesManager;
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

	private static final String RECORDS_SEQUENCE_TABLE_CONFIG_PATH = "/sequence.properties";
	private static final String SECONDARY_SEQUENCE_TABLE_CONFIG_PATH = "/secondarySequence.properties";
	static final String RECORDS_COLLECTION = "records";
	static final String EVENTS_COLLECTION = "events";
	static final String CONTENTS_COLLECTION = "contents";
	static final String NOTIFICATIONS_COLLECTION = "notifications";

	private final IOServicesFactory ioServicesFactory;
	private final SolrServers solrServers;
	private final ConfigManager configManager;
	private final UniqueIdGenerator idGenerator;
	private final UniqueIdGenerator secondaryIdGenerator;
	private final DataLayerConfiguration dataLayerConfiguration;
	private final ContentDao contentDao;
	private final BigVaultLogger bigVaultLogger;
	private final SecondTransactionLogManager secondTransactionLogManager;
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final DataLayerLogger dataLayerLogger;
	private final DataLayerExtensions dataLayerExtensions;
	final TransactionLogRecoveryManager transactionLogRecoveryManager;

	public DataLayerFactory(IOServicesFactory ioServicesFactory, DataLayerConfiguration dataLayerConfiguration,
			StatefullServiceDecorator statefullServiceDecorator) {

		super(statefullServiceDecorator);
		this.dataLayerExtensions = new DataLayerExtensions();
		this.dataLayerConfiguration = dataLayerConfiguration;
		// TODO Possibility to configure the logger
		this.bigVaultLogger = BigVaultLogger.disabled();
		this.ioServicesFactory = ioServicesFactory;
		this.solrServers = new SolrServers(newSolrServerFactory(), bigVaultLogger, dataLayerExtensions);
		this.dataLayerLogger = new DataLayerLogger();

		this.backgroundThreadsManager = add(new BackgroundThreadsManager(dataLayerConfiguration));

		if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.ZOOKEEPER) {
			this.configManager = add(new ZooKeeperConfigManager(dataLayerConfiguration.getSettingsZookeeperAddress(),
					ioServicesFactory.newIOServices()));

		} else if (dataLayerConfiguration.getSettingsConfigType() == ConfigManagerType.FILESYSTEM) {
			this.configManager = add(new FileSystemConfigManager(dataLayerConfiguration.getSettingsFileSystemBaseFolder(),
					ioServicesFactory.newIOServices(),
					ioServicesFactory.newHashingService(dataLayerConfiguration.getHashingEncoding())));

		} else {
			throw new ImpossibleRuntimeException("Unsupported ConfigManagerType");
		}

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
			this.secondaryIdGenerator = add(new ZeroPaddedSequentialUniqueIdGenerator(configManager,
					SECONDARY_SEQUENCE_TABLE_CONFIG_PATH));

		} else {
			throw new ImpossibleRuntimeException("Unsupported UniqueIdGenerator");
		}

		if (ContentDaoType.FILESYSTEM == dataLayerConfiguration.getContentDaoType()) {
			File rootFolder = dataLayerConfiguration.getContentDaoFileSystemFolder();
			contentDao = add(new FileSystemContentDao(rootFolder, ioServicesFactory.newIOServices(), dataLayerConfiguration));

		} else if (ContentDaoType.HADOOP == dataLayerConfiguration.getContentDaoType()) {
			String hadoopUrl = dataLayerConfiguration.getContentDaoHadoopUrl();
			String hadoopUser = dataLayerConfiguration.getContentDaoHadoopUser();
			contentDao = new HadoopContentDao(hadoopUrl, hadoopUser);

		} else {
			throw new ImpossibleRuntimeException("Unsupported ContentDaoType");
		}

		transactionLogRecoveryManager = new TransactionLogRecoveryManager(this);

		if (dataLayerConfiguration.isSecondTransactionLogEnabled()) {
			secondTransactionLogManager = add(new XMLSecondTransactionLogManager(dataLayerConfiguration,
					ioServicesFactory.newIOServices(), newRecordDao(), contentDao, backgroundThreadsManager, dataLayerLogger,
					dataLayerExtensions.getSystemWideExtensions(), transactionLogRecoveryManager));
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
}
