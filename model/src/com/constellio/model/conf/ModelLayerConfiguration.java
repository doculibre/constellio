package com.constellio.model.conf;

import java.io.File;

import org.joda.time.Duration;

public interface ModelLayerConfiguration {

	void validate();

	boolean isDocumentsParsedInForkProcess();

	File getTempFolder();

	String getComputerName();

	boolean isBatchProcessesThreadEnabled();

	void setBatchProcessesEnabled(boolean enabled);

	int getBatchProcessesPartSize();

	int getNumberOfRecordsPerTask();

	int getForkParsersPoolSize();

	File getImportationFolder();

	Duration getDelayBeforeDeletingUnreferencedContents();

	Duration getUnreferencedContentsThreadDelayBetweenChecks();

	Duration getTokenRemovalThreadDelayBetweenChecks();

	Duration getTokenDuration();

	int getDelayBeforeSendingNotificationEmailsInMinutes();

	String getMainDataLanguage();

	void setMainDataLanguage(String language);

	File getConstellioEncryptionFile();

}
