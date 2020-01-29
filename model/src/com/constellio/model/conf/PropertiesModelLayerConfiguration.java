package com.constellio.model.conf;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.PropertiesConfiguration;
import com.constellio.data.utils.Factory;
import com.constellio.model.services.encrypt.EncryptionServices;
import org.joda.time.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

	public static class InMemoryModelLayerConfiguration extends PropertiesModelLayerConfiguration {

		private Factory<EncryptionServices> encryptionServicesFactory;

		public InMemoryModelLayerConfiguration(PropertiesModelLayerConfiguration nested) {
			super(new HashMap<String, String>(nested.configs), nested.dataLayerConfiguration, nested.foldersLocator,
					new File(""));
		}

		@Override
		public void writeProperty(String key, String value) {
			configs.put(key, value);
		}

		public void setConstellioEncryptionFile(File file) {
			setFile("encryption.file", file);
		}

		public void setImportationFolder(File importationFolder) {
			setFile("importationFolder", importationFolder);
		}

		public void setContentImportThreadFolder(File file) {
			setFile("content.import.thread.folder", file);
		}

		public void setDeleteUnusedContentEnabled(boolean value) {
			setBoolean("content.delete.unused.enabled", value);
		}

		public void setTokenDuration(Duration duration) {
			setDuration("token.duration", duration);
		}

		public void setTokenRemovalThreadDelayBetweenChecks(Duration duration) {
			setDuration("tokenRemovalThread.delayBetweenChecks", duration);
		}

		public void setEncryptionServicesFactory(Factory<EncryptionServices> encryptionServicesFactory) {
			this.encryptionServicesFactory = encryptionServicesFactory;
		}

		@Override
		public Factory<EncryptionServices> getEncryptionServicesFactory() {
			return encryptionServicesFactory == null ? super.getEncryptionServicesFactory() : encryptionServicesFactory;
		}

		public void setUnreferencedContentsThreadDelayBetweenChecks(Duration duration) {
			setDuration("unreferencedContentsThread.delayBetweenChecks", duration);
		}

		public void setDelayBeforeDeletingUnreferencedContents(Duration duration) {
			setDuration("unreferencedContentsThread.delayBeforeDeleting", duration);
		}

		public void setRecordsVolatileCacheMemorySize(String volatileCacheSizeInMemory) {
			setString("dao.cache.volatileCacheSizeInMemory", volatileCacheSizeInMemory);
		}
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
		return getDuration("unreferencedContentsThread.delayBeforeDeleting", Duration.standardHours(36));
	}

	@Override
	public Duration getParsingContentsThreadDelayBetweenChecks() {
		return getDuration("unreferencedContentsThread.delayBetweenChecks", Duration.standardSeconds(30));
	}

	@Override
	public Duration getGeneratePreviewsThreadDelayBetweenChecks() {
		return getDuration("generatePreviewsThread.delayBetweenChecks", Duration.standardSeconds(30));
	}

	public Duration getTokenRemovalThreadDelayBetweenChecks() {
		return getDuration("tokenRemovalThread.delayBetweenChecks", Duration.standardHours(1));
	}

	@Override
	public Duration getTokenDuration() {
		return getDuration("token.duration", Duration.standardHours(10));
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
	public boolean isPreviousPrivateKeyLost() {
		return getBoolean("previousPrivateKeyLost", false);
	}

	@Override
	public boolean isDeleteUnusedContentEnabled() {
		return getBoolean("content.delete.unused.enabled", true);
	}

	@Override
	public File getContentImportThreadFolder() {
		return getFile("content.import.thread.folder", null);
	}

	@Override
	public int getReindexingQueryBatchSize() {
		return getInt("reindexing.queryBatchSize", 5000);
	}

	@Override
	public boolean isReindexingEvents() {
		return getBoolean("reindexing.events", true);
	}

	@Override
	public int getReindexingThreadBatchSize() {
		return getInt("reindexing.threadBatchSize", 250);
	}

	@Override
	public void setMainDataLanguage(String language) {
		setString("mainDataLanguage", language);

	}

	@Override
	public File getConstellioEncryptionFile() {
		return getFile("encryption.file", foldersLocator.getConstellioEncryptionFile());
	}

	@Override
	public DataLayerConfiguration getDataLayerConfiguration() {
		return dataLayerConfiguration;
	}

	@Override
	public void setBatchProcessesEnabled(boolean enabled) {
		this.batchProcessesEnabled = enabled;
	}

	@Override
	public boolean isBatchProcessesThreadEnabled() {
		return batchProcessesEnabled;
	}


	public long getRecordsVolatileCacheMemorySize() {
		return getBytesSize("dao.cache.volatileCacheSizeInMemory", "250M");
	}

	@Override
	public boolean isForcingCacheLoadingFromSolr() {
		return getBoolean("dao.cache.forcingCacheLoadingFromSolr", false);
	}

	@Override
	public boolean isSummaryCacheEnabled() {
		return getBoolean("caches.summary.enabled", true);
	}


	@Override
	public Factory<EncryptionServices> getEncryptionServicesFactory() {
		return new Factory<EncryptionServices>() {
			@Override
			public EncryptionServices get() {
				return new EncryptionServices(isPreviousPrivateKeyLost());
			}
		};
	}

}