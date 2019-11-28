package com.constellio.data.conf;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;
import org.apache.solr.common.SolrInputDocument;
import org.joda.time.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.constellio.data.conf.SolrServerType.HTTP;

public class PropertiesDataLayerConfiguration extends PropertiesConfiguration implements DataLayerConfiguration {

	public static final String ZKHOST = "dao.records.cloud.zkHost";
	public static final String NUMBER_OF_LIVE_NODE = "dao.records.cloud.numberOfLiveNode";


	public static final String RECORD_TYPE = "dao.records.type";

	private File defaultTempFolder;

	private File defaultFileSystemBaseFolder;

	private boolean backgroundThreadsEnable = true;

	public PropertiesDataLayerConfiguration(Map<String, String> configs, File defaultTempFolder,
											File defaultFileSystemBaseFolder, File constellioProperties) {
		super(configs, constellioProperties);
		this.defaultTempFolder = defaultTempFolder;
		this.defaultFileSystemBaseFolder = defaultFileSystemBaseFolder;
	}

	public static class InMemoryDataLayerConfiguration extends PropertiesDataLayerConfiguration {

		private SecondTransactionLogReplayFilter filter;
		private IdGeneratorType idGeneratorType;
		private String uniqueKeyToBeCreated;

		public InMemoryDataLayerConfiguration(PropertiesDataLayerConfiguration nested) {
			super(new HashMap<String, String>(nested.configs), nested.defaultTempFolder, nested.defaultFileSystemBaseFolder,
					new File(""));
		}

		@Override
		public void writeProperty(String key, String value) {
			configs.put(key, value);
		}

		public void setSettingsFileSystemBaseFolder(File value) {
			setFile("dao.settings.filesystem.baseFolder", value);
		}

		public void setTempFolder(File value) {
			setFile("tempFolder", value);
		}

		public void setContentDaoFileSystemFolder(File value) {
			setFile("dao.contents.filesystem.folder", value);
		}

		public void setInRollbackTestMode(boolean value) {
			setBoolean("secondTransactionLog.checkRollback", value);
		}

		public void setSecondTransactionLogBaseFolder(File value) {
			setFile("secondTransactionLog.folder", value);
		}

		public void setSecondTransactionLogMode(SecondTransactionLogType value) {
			setEnum("secondTransactionLog.mode", value);
		}

		public void setReplayTransactionStartVersion(long value) {
			setLong("secondTransactionLog.version", value);
		}

		public void setSecondTransactionLogEnabled(boolean value) {
			setBoolean("secondTransactionLog.enabled", value);
		}

		public void setKafkaServers(String value) {
			setString("kafka.servers", value);
		}

		public void setKafkaTopic(String value) {
			setString("kafka.topic", value);
		}

		public void setSecondTransactionLogReplayFilter(SecondTransactionLogReplayFilter filter) {
			this.filter = filter;
		}

		public void setEventLifespan(Duration duration) {
			setDuration("eventBus.solr.eventLifespan", duration);
		}

		public void setSolrEventBusRetrieveAndSendFrequency(Duration duration) {
			setDuration("eventBus.solr.retrieveAndSendFrequency", duration);
		}

		public SecondTransactionLogReplayFilter getSecondTransactionLogReplayFilter() {
			return filter == null ? super.getSecondTransactionLogReplayFilter() : filter;
		}

		public void setSecondTransactionLogBackupCount(int value) {
			setInt("secondTransactionLog.backupCount", value);
		}

		public void setSecondTransactionLogMergeFrequency(Duration duration) {
			setDuration("secondTransactionLog.mergeFrequency", duration);
		}

		public void setSecondaryIdGeneratorType(IdGeneratorType idGeneratorType) {
			this.idGeneratorType = idGeneratorType;
		}

		public void setSettingsConfigType(ConfigManagerType configManagerType) {
			setEnum("dao.settings.type", configManagerType);
		}

		@Override
		public IdGeneratorType getSecondaryIdGeneratorType() {
			return idGeneratorType == null ? super.getSecondaryIdGeneratorType() : idGeneratorType;
		}

		public void setUniqueKeyToBeCreated(String uniqueKeyToBeCreated) {
			this.uniqueKeyToBeCreated = uniqueKeyToBeCreated;
		}

		@Override
		public String createRandomUniqueKey() {
			return this.uniqueKeyToBeCreated == null ? super.createRandomUniqueKey() : uniqueKeyToBeCreated;
		}

		public void setRecordsDaoHttpSolrServerUrl(String url) {
			setString("dao.records.http.url", url);
		}

		public void setContentDaoType(ContentDaoType contentDaoType) {
			setEnum("dao.contents.type", contentDaoType);
		}

		public void setRecordsDaoSolrServerType(SolrServerType solrServerType) {
			setEnum(RECORD_TYPE, solrServerType);
		}

		public void setMicrosoftSqlServerUrl(String value) {
			setString("sql.server.url", value);
		}

		public void setMicrosoftSqlServerDatabase(String value) {
			setString("sql.server.database", value);
		}

		public void setMicrosoftSqlServeruser(String value) {
			setString("sql.server.user", value);
		}

		public void setMicrosoftSqlServerpassword(String value) {
			setString("sql.server.password", value);
		}

		public void setMicrosoftSqlServerencrypt(boolean value) {
			setBoolean("sql.server.encrypt", value);
		}

		public void setMicrosoftSqlServertrustServerCertificate(boolean value) {
			setBoolean("sql.server.trustServerCertificate", value);
		}

		public void setMicrosoftSqlServerloginTimeout(int value) {
			setInt("sql.server.loginTimeout", value);
		}

	}

	public SolrServerType getRecordsDaoSolrServerType() {
		return (SolrServerType) getRequiredEnum(RECORD_TYPE, SolrServerType.class);
	}

	public String getRecordsDaoHttpSolrServerUrl() {
		return getRequiredString("dao.records.http.url");
	}

	public String getRecordsDaoCloudSolrServerZKHost() {
		return getRequiredString(ZKHOST);
	}

	public boolean isRecordsDaoHttpSolrServerFaultInjectionEnabled() {
		return getBoolean("dao.records.http.faultInjection", false);
	}

	public ContentDaoType getContentDaoType() {
		return (ContentDaoType) getRequiredEnum("dao.contents.type", ContentDaoType.class);
	}

	public String getContentDaoHadoopUrl() {
		return getRequiredString("dao.contents.server.address");
	}

	public String getContentDaoHadoopUser() {
		return getRequiredString("dao.contents.server.user");
	}

	public File getContentDaoFileSystemFolder() {
		return getRequiredFile("dao.contents.filesystem.folder");
	}

	public void setContentDaoFileSystemFolder(File contentsFolder) {
		setFile("dao.contents.filesystem.folder", contentsFolder);
	}

	public String getContentDaoReplicatedVaultMountPoint() {
		return getString("dao.contents.filesystem.replicatedVaultMountPoint", null);
	}

	public void setContentDaoReplicatedVaultMountPoint(String replicatedVaultMountPoint) {
		setString("dao.contents.filesystem.replicatedVaultMountPoint", replicatedVaultMountPoint);
	}

	@Override
	public DigitSeparatorMode getContentDaoFileSystemDigitsSeparatorMode() {
		return (DigitSeparatorMode) getEnum("dao.contents.filesystem.separatormode", DigitSeparatorMode.TWO_DIGITS);
	}

	@Override
	public void setContentDaoFileSystemDigitsSeparatorMode(DigitSeparatorMode mode) {
		setString("dao.contents.filesystem.separatormode", mode == null ? null : mode.name());
	}

	public String getSettingsZookeeperAddress() {
		return getRequiredString("dao.settings.server.address");
	}

	public File getTempFolder() {
		return getFile("tempFolder", defaultTempFolder);
	}

	public IdGeneratorType getIdGeneratorType() {
		return (IdGeneratorType) getEnum("idGenerator.type", IdGeneratorType.SEQUENTIAL);
	}

	@Override
	public IdGeneratorType getSecondaryIdGeneratorType() {
		return (IdGeneratorType) getEnum("secondaryIdGenerator.type", IdGeneratorType.UUID_V1);
	}

	@Override
	public boolean isSecondTransactionLogEnabled() {
		return getBoolean("secondTransactionLog.enabled", false);
	}

	@Override
	public File getSecondTransactionLogBaseFolder() {
		return getRequiredFile("secondTransactionLog.folder");
	}

	@Override
	public SecondTransactionLogType getSecondTransactionLogMode() {
		return (SecondTransactionLogType) getEnum("secondTransactionLog.mode", SecondTransactionLogType.XML);
	}

	@Override
	public long getReplayTransactionStartVersion() {
		return getLong("secondTransactionLog.version", 0);
	}

	public ConfigManagerType getSettingsConfigType() {
		return (ConfigManagerType) getRequiredEnum("dao.settings.type", ConfigManagerType.class);
	}

	@Override
	public CacheType getCacheType() {
		return (CacheType) getEnum("dao.cache", CacheType.MEMORY);
	}

	@Override
	public String getCacheUrl() {
		return getString("dao.cache.url", null);
	}

	@Override
	public File getSettingsFileSystemBaseFolder() {
		return getFile("dao.settings.filesystem.baseFolder", defaultFileSystemBaseFolder);
	}

	@Override
	public void validate() {

	}

	@Override
	public int getBackgroudThreadsPoolSize() {
		return 2;
	}

	@Override
	public Duration getSecondTransactionLogMergeFrequency() {
		return getDuration("secondTransactionLog.mergeFrequency", Duration.standardMinutes(15));
	}

	@Override
	public int getSecondTransactionLogBackupCount() {
		return getInt("secondTransactionLog.backupCount", 1);
	}

	@Override
	public boolean isBackgroundThreadsEnabled() {
		return backgroundThreadsEnable;
	}

	@Override
	public void setBackgroundThreadsEnabled(boolean backgroundThreadsEnabled) {
		this.backgroundThreadsEnable = backgroundThreadsEnabled;
	}

	@Override
	public SecondTransactionLogReplayFilter getSecondTransactionLogReplayFilter() {
		return new SecondTransactionLogReplayFilter() {

			@Override
			public boolean isReplayingAdd(String id, String schema, SolrInputDocument solrInputDocument) {
				return true;
			}

			@Override
			public boolean isReplayingUpdate(String id, SolrInputDocument solrInputDocument) {
				return true;
			}
		};
	}

	@Override
	public boolean isWriteZZRecords() {
		return getBoolean("writeZZRecords", false);
	}

	@Override
	public HashingEncoding getHashingEncoding() {
		return (HashingEncoding) getEnum("hashing.encoding", HashingEncoding.BASE64);
	}

	@Override
	public void setWriteZZRecords(boolean enable) {
		setBoolean("writeZZRecords", enable);
	}

	@Override
	public boolean isLocalHttpSolrServer() {
		return getRecordsDaoSolrServerType().equals(HTTP) &&
			   (getRecordsDaoHttpSolrServerUrl().contains("localhost") || getRecordsDaoHttpSolrServerUrl()
					   .contains("127.0.0.1"));
	}

	@Override
	public boolean isInRollbackTestMode() {
		return getBoolean("secondTransactionLog.checkRollback", false);
	}

	@Override
	public String createRandomUniqueKey() {
		Random random = new Random();
		return random.nextInt(1000) + "-" + random.nextInt(1000) + "-" + random.nextInt(1000);
	}

	@Override
	public void setHashingEncoding(HashingEncoding encoding) {
		setString("hashing.encoding", encoding == null ? null : encoding.name());
	}

	@Override
	public String getKafkaServers() {
		return getString("kafka.servers", null);
	}

	@Override
	public String getKafkaTopic() {
		return getString("kafka.topic", null);
	}

	@Override
	public int getConversionProcesses() {
		return getInt("conversion.processes", 1);
	}

	@Override
	public String getOnlineConversionUrl() {
		return getString("conversion.url", null);
	}

	@Override
	public EventBusSendingServiceType getEventBusSendingServiceType() {
		return (EventBusSendingServiceType) getEnum("eventBus.type", EventBusSendingServiceType.STANDALONE);
	}

	@Override
	public Duration getSolrEventBusSendingServiceTypeEventLifespan() {
		return getDuration("eventBus.solr.eventLifespan", Duration.standardMinutes(10));
	}

	@Override
	public Duration getSolrEventBusSendingServiceTypePollAndRetrieveFrequency() {
		return getDuration("eventBus.solr.retrieveAndSendFrequency", Duration.standardSeconds(1));
	}

	@Override
	public String getMicrosoftSqlServerUrl() {
		return getString("sql.server.url", null);
	}

	@Override
	public String getMicrosoftSqlServerDatabase() {
		return getString("sql.server.database", null);
	}

	@Override
	public String getMicrosoftSqlServeruser() {
		return getString("sql.server.user", null);
	}

	@Override
	public String getMicrosoftSqlServerpassword() {
		return getString("sql.server.password", null);
	}

	@Override
	public boolean getMicrosoftSqlServerencrypt() {
		return getBoolean("sql.server.encrypt", false);
	}

	@Override
	public boolean getMicrosoftSqlServertrustServerCertificate() {
		return getBoolean("sql.server.trustServerCertificate", false);
	}

	@Override
	public int getMicrosoftSqlServerloginTimeout() {
		return getInt("sql.server.loginTimeout", 0);
	}

	@Override
	public ElectionServiceType getElectionServiceType() {
		return (ElectionServiceType) getEnum("leaderElectionMethod.type", ElectionServiceType.STANDALONE);
	}

	@Override
	public boolean isSystemDistributed() {
		return getElectionServiceType() != ElectionServiceType.STANDALONE;
	}
}