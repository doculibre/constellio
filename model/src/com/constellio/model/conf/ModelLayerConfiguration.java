package com.constellio.model.conf;

import java.io.File;

import org.joda.time.Duration;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.utils.Factory;
import com.constellio.model.services.encrypt.EncryptionServices;

public interface ModelLayerConfiguration {

	void validate();

	boolean isDocumentsParsedInForkProcess();

	File getTempFolder();

	String getComputerName();

	boolean isBatchProcessesThreadEnabled();

	Factory<EncryptionServices> getEncryptionServicesFactory();

	void setBatchProcessesEnabled(boolean enabled);

	int getBatchProcessesPartSize();

	int getNumberOfRecordsPerTask();

	int getForkParsersPoolSize();

	File getImportationFolder();

	Duration getDelayBeforeDeletingUnreferencedContents();

	Duration getUnreferencedContentsThreadDelayBetweenChecks();

	Duration getGeneratePreviewsThreadDelayBetweenChecks();

	Duration getTokenRemovalThreadDelayBetweenChecks();

	Duration getTokenDuration();

	int getDelayBeforeSendingNotificationEmailsInMinutes();

	String getMainDataLanguage();

	void setMainDataLanguage(String language);

	File getConstellioEncryptionFile();

	DataLayerConfiguration getDataLayerConfiguration();

	boolean isPreviousPrivateKeyLost();

	boolean isDeleteUnusedContentEnabled();

	File getContentImportThreadFolder();

	boolean isReindexingEvents();

	int getReindexingQueryBatchSize();

	int getReindexingThreadBatchSize();

}
