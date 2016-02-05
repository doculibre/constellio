package com.constellio.app.ui.pages.management.extractors.entities;

import java.io.Serializable;

import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;

public class RegexConfigVO implements Serializable {

	private String inputMetadata;
	private String regex;
	private String value;
	private RegexConfigType regexConfigType;

	public RegexConfigVO() {
	}

	public RegexConfigVO(String inputMetadata, String regex, String value, RegexConfigType regexConfigType) {
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

	public RegexConfigType getRegexConfigType() {
		return regexConfigType;
	}

	public void setRegexConfigType(RegexConfigType regexConfigType) {
		this.regexConfigType = regexConfigType;
	}

}
