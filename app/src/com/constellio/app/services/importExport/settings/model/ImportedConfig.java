package com.constellio.app.services.importExport.settings.model;

public class ImportedConfig {

	String key;
	String value;

	public ImportedConfig setKey(String key) {
		this.key = key;
		return this;
	}

	public ImportedConfig setValue(String value) {
		this.value = value;
		return this;
	}

}
