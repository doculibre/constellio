package com.constellio.data.conf;

import java.io.File;
import java.io.Serializable;

import org.joda.time.Duration;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogReplayFilter;

public interface DataLayerConfiguration extends Serializable {

	void validate();

	SolrServerType getRecordsDaoSolrServerType();

	String getRecordsDaoHttpSolrServerUrl();

	String getRecordsDaoCloudSolrServerZKHost();

	boolean isRecordsDaoHttpSolrServerFaultInjectionEnabled();

	ContentDaoType getContentDaoType();

	String getContentDaoHadoopUrl();

	String getContentDaoHadoopUser();

	File getContentDaoFileSystemFolder();

	DigitSeparatorMode getContentDaoFileSystemDigitsSeparatorMode();

	void setContentDaoFileSystemDigitsSeparatorMode(DigitSeparatorMode mode);

	File getTempFolder();

	ConfigManagerType getSettingsConfigType();

	CacheType getSettingsCacheType();

	String getSettingsCacheUrl();

	CacheType getRecordsCacheType();

	String getRecordsCacheUrl();

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
}
