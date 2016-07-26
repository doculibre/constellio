package com.constellio.data.conf;

import static com.constellio.data.conf.SolrServerType.HTTP;

import java.io.File;
import java.util.Map;
import java.util.Random;

import org.apache.solr.common.SolrInputDocument;
import org.joda.time.Duration;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;

public class PropertiesDataLayerConfiguration extends PropertiesConfiguration implements DataLayerConfiguration {

	public static final String ZKHOST = "dao.records.cloud.zkHost";

	public static final String RECORD_TYPE = "dao.records.type";

	private File defaultTempFolder;

	private File defaultFileSystemBaseFolder;

	private boolean backgroundThreadsEnable = true;

	private Boolean secondTransactionLogEnabled;

	public PropertiesDataLayerConfiguration(Map<String, String> configs, File defaultTempFolder,
			File defaultFileSystemBaseFolder, File constellioProperties) {
		super(configs, constellioProperties);
		this.defaultTempFolder = defaultTempFolder;
		this.defaultFileSystemBaseFolder = defaultFileSystemBaseFolder;
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
		if (secondTransactionLogEnabled != null) {
			return secondTransactionLogEnabled;
		}
		return getBoolean("secondTransactionLog.enabled", false);
	}

	@Override
	public File getSecondTransactionLogBaseFolder() {
		return getRequiredFile("secondTransactionLog.folder");
	}

	public ConfigManagerType getSettingsConfigType() {
		return (ConfigManagerType) getRequiredEnum("dao.settings.type", ConfigManagerType.class);
	}

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
		return Duration.standardMinutes(15);
	}

	@Override
	public int getSecondTransactionLogBackupCount() {
		return 2;
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
	public void setSecondTransactionLogFolderEnabled(boolean enable) {
		this.secondTransactionLogEnabled = enable;
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
		if (secondTransactionLogEnabled != null) {
			return secondTransactionLogEnabled;
		}
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

}