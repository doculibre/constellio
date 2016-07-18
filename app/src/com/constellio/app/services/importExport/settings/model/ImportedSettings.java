package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

public class ImportedSettings {

	List<ImportedConfig> configs = new ArrayList<>();

	List<ImportedCollectionSettings> collectionsSettings = new ArrayList<>();

	public List<ImportedConfig> getConfigs() {
		return configs;
	}

	public ImportedSettings addConfig(ImportedConfig config) {
		configs.add(config);
		return this;
	}

	public List<ImportedCollectionSettings> getCollectionsConfigs() {
		return collectionsSettings;
	}

	public ImportedSettings addCollectionsConfigs(ImportedCollectionSettings collectionSettings) {
		collectionsSettings.add(collectionSettings);
		return this;
	}

	public ImportedSettings setConfigs(List<ImportedConfig> configs) {
		this.configs = configs;
		return this;
	}


	public ImportedSettings setCollectionsSettings(List<ImportedCollectionSettings> collectionsSettings) {
		this.collectionsSettings = collectionsSettings;
		return this;
	}
}
