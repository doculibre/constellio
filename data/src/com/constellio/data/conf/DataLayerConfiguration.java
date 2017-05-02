package com.constellio.data.conf;

import java.io.File;
import java.util.List;

import org.joda.time.Duration;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;

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

    List<String> getContentDaoReplicatedVaultMountPoints();

    void setContentDaoReplicatedVaultMountPoints(List<String> replicatedVaultMountPoints);

    File getTempFolder();

	ConfigManagerType getSettingsConfigType();

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
	
	String getSecondTransactionLogClass();
	
	String getKafkaTopic();
}
