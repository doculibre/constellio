package com.constellio.data.dao.managers.config.values;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class TextConfiguration {
	private final String version;
	private final String text;
	
	public TextConfiguration(String version, String text) {
		this.version = version;
		this.text = text;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}
}
