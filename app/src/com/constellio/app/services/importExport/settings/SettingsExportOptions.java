package com.constellio.app.services.importExport.settings;

public class SettingsExportOptions {

	private boolean isOnlyUSR = false;

	private boolean isAlwaysExportingLabels = false;

	private boolean isExportingConfigs = false;

	private boolean isExportingAsCurrentCollection = false;

	private boolean exportingSequences;

	public boolean isOnlyUSR() {
		return isOnlyUSR;
	}

	public SettingsExportOptions setOnlyUSR(boolean onlyUSR) {
		isOnlyUSR = onlyUSR;
		return this;
	}

	public boolean isExportingSequences() {
		return exportingSequences;
	}

	public SettingsExportOptions setExportingSequences(boolean exportingSequences) {
		this.exportingSequences = exportingSequences;
		return this;
	}

	public boolean isAlwaysExportingLabels() {
		return isAlwaysExportingLabels;
	}

	public SettingsExportOptions setAlwaysExportingLabels(boolean alwaysExportingLabels) {
		isAlwaysExportingLabels = alwaysExportingLabels;
		return this;
	}

	public boolean isExportingConfigs() {
		return isExportingConfigs;
	}

	public SettingsExportOptions setExportingConfigs(boolean exportingConfigs) {
		isExportingConfigs = exportingConfigs;
		return this;
	}

	public boolean isExportingAsCurrentCollection() {
		return isExportingAsCurrentCollection;
	}

	public SettingsExportOptions setExportingAsCurrentCollection(boolean exportingAsCurrentCollection) {
		isExportingAsCurrentCollection = exportingAsCurrentCollection;
		return this;
	}
}
