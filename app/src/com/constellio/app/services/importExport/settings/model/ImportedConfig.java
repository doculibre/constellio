package com.constellio.app.services.importExport.settings.model;

public class ImportedConfig {

	String key;
	String value;

	public String getKey(){
		return key;
	}

	public ImportedConfig setKey(String key) {
		this.key = key;
		return this;
	}

	public ImportedConfig setValue(String value) {
		this.value = value;
		return this;
	}

	public String getValue() {
		return value;
	}

}
