package com.constellio.data.conf;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;
import org.joda.time.Duration;

import java.io.File;

public interface DataLayerConfiguration {

	void validate();

	SolrServerType getRecordsDaoSolrServerType();

	String getRecordsDaoHttpSolrServerUrl();

	String getRecordsDaoCloudSolrServerZKHost();

	boolean isRecordsDaoHttpSolrServerFaultInjectionEnabled();

	ContentDaoType getContentDaoType();

	String getContentDaoHadoopUrl();

	String getContentDaoHadoopUser();

	File getContentDaoFileSystemFolder();

	void setContentDaoFileSystemFolder(File contentsFolder);

	DigitSeparatorMode getContentDaoFileSystemDigitsSeparatorMode();

	void setContentDaoFileSystemDigitsSeparatorMode(DigitSeparatorMode mode);

	String getContentDaoReplicatedVaultMountPoint();

	void setContentDaoReplicatedVaultMountPoint(String replicatedVaultMountPoint);

	File getTempFolder();

	ConfigManagerType getSettingsConfigType();

	CacheType getCacheType();

	String getCacheUrl();

	String getSettingsZookeeperAddress();

	File getSettingsFileSystemBaseFolder();

	IdGeneratorType getIdGeneratorType();

	IdGeneratorType getSecondaryIdGeneratorType();

	boolean isSecondTransactionLogEnabled();

	boolean isWriteZZRecords();

	HashingEncoding getHashingEncoding();

	File getSecondTransactionLogBaseFolder();

	int getBackgroudThreadsPoolSize();

	Duration getSecondTransactionLogMergeFrequency();

	int getSecondTransactionLogBackupCount();

	boolean isBackgroundThreadsEnabled();

	void setBackgroundThreadsEnabled(boolean backgroundThreadsEnabled);

	SecondTransactionLogReplayFilter getSecondTransactionLogReplayFilter();

	void setWriteZZRecords(boolean enable);

	boolean isLocalHttpSolrServer();

	boolean isInRollbackTestMode();

	String createRandomUniqueKey();

	void setHashingEncoding(HashingEncoding encoding);

	String getKafkaServers();

	String getSecondTransactionLogMode();

	String getKafkaTopic();

	long getReplayTransactionStartVersion();

	int getConversionProcesses();

	String getOnlineConversionUrl();

	EventBusSendingServiceType getEventBusSendingServiceType();

	ElectionServiceType getElectionServiceType();

	boolean isSystemDistributed();

	Duration getSolrEventBusSendingServiceTypeEventLifespan();

	Duration getSolrEventBusSendingServiceTypePollAndRetrieveFrequency();

	boolean areTiffFilesConvertedForPreview();

}
