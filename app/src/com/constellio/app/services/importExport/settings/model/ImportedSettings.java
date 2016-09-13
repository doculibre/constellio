package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ImportedSettings {

	List<ImportedConfig> configs = new ArrayList<>();

	List<ImportedSequence> sequences = new ArrayList<>();

	List<ImportedCollectionSettings> collectionsSettings = new ArrayList<>();

	public List<ImportedConfig> getConfigs() {
		return configs;
	}

	public ImportedSettings addConfig(ImportedConfig config) {
		configs.add(config);
		return this;
	}

	public ImportedSettings addConfig(String key, String value) {
		configs.add(new ImportedConfig().setKey(key).setValue(value));
		return this;
	}

	public List<ImportedCollectionSettings> getCollectionsSettings() {
		return collectionsSettings;
	}

	public ImportedSettings addCollectionSettings(ImportedCollectionSettings collectionSettings) {
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

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public List<ImportedSequence> getSequences() {
		return Collections.unmodifiableList(sequences);
	}

	public ImportedSettings setImportedSequences(List<ImportedSequence> sequences) {
		this.sequences = sequences;
		return this;
	}

	public ImportedSettings addSequence(ImportedSequence importedSequence) {
		sequences.add(importedSequence);
		return this;
	}
}
