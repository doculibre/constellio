package com.constellio.app.services.importExport.settings.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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


	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);

	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}
}
