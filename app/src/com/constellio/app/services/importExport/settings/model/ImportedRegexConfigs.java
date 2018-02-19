package com.constellio.app.services.importExport.settings.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportedRegexConfigs {

	private String inputMetadata;
	private String regex;
	private String value;
	private String regexConfigType;

	public ImportedRegexConfigs() {
	}

	public ImportedRegexConfigs(String inputMetadata, String regex, String value, String regexConfigType) {
		this.inputMetadata = inputMetadata;
		this.regex = regex;
		this.value = value;
		this.regexConfigType = regexConfigType;
	}

	public String getInputMetadata() {
		return inputMetadata;
	}

	public void setInputMetadata(String inputMetadata) {
		this.inputMetadata = inputMetadata;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getRegexConfigType() {
		return regexConfigType;
	}

	public void setRegexConfigType(String regexConfigType) {
		this.regexConfigType = regexConfigType;
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
