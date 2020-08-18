package com.constellio.app.conf;

import com.constellio.data.conf.PropertiesConfiguration;
import com.constellio.data.utils.Factory;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Language;
import com.constellio.model.services.encrypt.EncryptionServices;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertiesAppLayerConfiguration extends PropertiesConfiguration implements AppLayerConfiguration {

	private FoldersLocator foldersLocator;

	private final ModelLayerConfiguration modelLayerConfiguration;

	public PropertiesAppLayerConfiguration(Map<String, String> configs, ModelLayerConfiguration modelLayerConfiguration,
										   FoldersLocator foldersLocator, File constellioProperties) {
		super(configs, constellioProperties);
		this.modelLayerConfiguration = modelLayerConfiguration;
		this.foldersLocator = foldersLocator;
	}

	public static class InMemoryAppLayerConfiguration extends PropertiesAppLayerConfiguration {

		private Factory<EncryptionServices> encryptionServicesFactory;

		public InMemoryAppLayerConfiguration(PropertiesAppLayerConfiguration nested) {
			super(new HashMap<String, String>(nested.configs), nested.modelLayerConfiguration, nested.foldersLocator,
					new File(""));
		}

		@Override
		public void writeProperty(String key, String value) {
			configs.put(key, value);
		}

		public void setSetupProperties(File value) {
			setFile("setupProperties.file", value);
		}

		public void setPluginsFolder(File value) {
			setFile("plugins.folder", value);
		}

		public void setPluginsManagementOnStartupFile(File value) {
			setFile("pluginsToMoveOnStartup.file", value);
		}

		public void setFastMigrationsEnabled(boolean value) {
			setBoolean("fastMigrations.enabled", value);
		}

		public void setEnabledPrototypeLanguages(String value) {
			setString("enabledPrototypeLanguages", value);
		}

	}

	public static PropertiesAppLayerConfiguration newVolatileConfiguration(PropertiesAppLayerConfiguration config) {
		return new PropertiesAppLayerConfiguration(new HashMap<>(config.configs), config.modelLayerConfiguration,
				config.foldersLocator, config.propertyFile) {
			@Override
			public void writeProperty(String key, String value) {
				configs.put(key, value);
			}
		};
	}

	@Override
	public void validate() {

	}

	@Override
	public File getTempFolder() {
		return modelLayerConfiguration.getTempFolder();
	}

	@Override
	public File getPluginsFolder() {
		return getFile("plugins.folder", new FoldersLocator().getPluginsJarsFolder());
	}

	@Override
	public File getPluginsManagementOnStartupFile() {
		return getFile("pluginsToMoveOnStartup.file", new FoldersLocator().getPluginsToMoveOnStartupFile());
	}

	@Override
	public File getSetupProperties() {
		return getFile("setupProperties.file", foldersLocator.getConstellioSetupProperties());
	}

	@Override
	public boolean isFastMigrationsEnabled() {
		return getBoolean("fastMigrations.enabled", true);
	}

	@Override
	public List<Language> getSupportedLanguages() {
		String enabledPrototypeLanguages = getString("enabledPrototypeLanguages", "");

		List<Language> languages = new ArrayList<>();
		languages.add(Language.French);
		languages.add(Language.English);

		if (enabledPrototypeLanguages.contains("ar")) {
			languages.add(Language.Arabic);
		}
		return Collections.unmodifiableList(languages);
	}

	@Override
	public List<String> getSupportedLanguageCodes() {
		List<String> languageCodes = new ArrayList<>();

		for (Language language : getSupportedLanguages()) {
			languageCodes.add(language.getCode());
		}

		return Collections.unmodifiableList(languageCodes);
	}
}
