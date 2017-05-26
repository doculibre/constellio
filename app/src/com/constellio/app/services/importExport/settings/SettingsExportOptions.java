package com.constellio.app.services.importExport.settings;

public class SettingsExportOptions {

	private boolean isOnlyUSR = false;

	private boolean isAlwaysExportingLabels = false;

	private boolean isExportingConfigs = false;

	public boolean isOnlyUSR() {
		return isOnlyUSR;
	}

	public SettingsExportOptions setOnlyUSR(boolean onlyUSR) {
		isOnlyUSR = onlyUSR;
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
}
