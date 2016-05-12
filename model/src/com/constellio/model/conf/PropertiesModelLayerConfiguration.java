package com.constellio.model.conf;

import java.io.File;
import java.util.Map;

import org.joda.time.Duration;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesConfiguration;
import com.constellio.data.utils.Factory;
import com.constellio.model.services.encrypt.EncryptionServices;

public class PropertiesModelLayerConfiguration extends PropertiesConfiguration implements ModelLayerConfiguration {

	private final DataLayerConfiguration dataLayerConfiguration;
	private final FoldersLocator foldersLocator;
	private boolean batchProcessesEnabled = true;

	public PropertiesModelLayerConfiguration(Map<String, String> configs, DataLayerConfiguration dataLayerConfiguration,
			FoldersLocator foldersLocator, File constellioProperties) {
		super(configs, constellioProperties);
		this.dataLayerConfiguration = dataLayerConfiguration;
		this.foldersLocator = foldersLocator;
	}

	@Override
	public void validate() {

	}

	@Override
	public boolean isDocumentsParsedInForkProcess() {
		return getBoolean("parsing.useForkProcess", false);
	}

	@Override
	public File getTempFolder() {
		return dataLayerConfiguration.getTempFolder();
	}

	@Override
	public String getComputerName() {
		return "mainserver";
	}

	@Override
	public int getBatchProcessesPartSize() {
		//return getRequiredInt("batchProcess.partSize");
		return 500;
	}

	@Override
	public int getNumberOfRecordsPerTask() {
		return 100;
	}

	@Override
	public int getForkParsersPoolSize() {
		return 20;
	}

	@Override
	public File getImportationFolder() {
		return getFile("importationFolder", foldersLocator.getDefaultImportationFolder());
	}

	@Override
	public Duration getDelayBeforeDeletingUnreferencedContents() {
		return Duration.standardMinutes(10);
	}

	@Override
	public Duration getUnreferencedContentsThreadDelayBetweenChecks() {
		return Duration.standardSeconds(30);
	}

	public Duration getTokenRemovalThreadDelayBetweenChecks() {
		return Duration.standardHours(1);
	}

	@Override
	public Duration getTokenDuration() {
		return Duration.standardHours(10);
	}

	@Override
	public int getDelayBeforeSendingNotificationEmailsInMinutes() {
		return 42;
	}

	@Override
	public String getMainDataLanguage() {
		return getString("mainDataLanguage", "fr");
	}

	@Override
	public void setMainDataLanguage(String language) {
		setString("mainDataLanguage", language);

	}

	@Override
	public File getConstellioEncryptionFile() {
		return foldersLocator.getConstellioEncryptionFile();
	}

	@Override
	public void setBatchProcessesEnabled(boolean enabled) {
		this.batchProcessesEnabled = enabled;
	}

	@Override
	public boolean isBatchProcessesThreadEnabled() {
		return batchProcessesEnabled;
	}

	@Override
	public Factory<EncryptionServices> getEncryptionServicesFactory() {
		return new Factory<EncryptionServices>() {
			@Override
			public EncryptionServices get() {
				return new EncryptionServices();
			}
		};
	}

}