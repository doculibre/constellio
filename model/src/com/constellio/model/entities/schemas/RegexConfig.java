package com.constellio.model.entities.schemas;

import java.io.Serializable;
import java.util.regex.Pattern;

public class RegexConfig implements Serializable {

	public enum RegexConfigType {
		SUBSTITUTION, TRANSFORMATION
	}

	private String inputMetadata;
	private Pattern regex;
	private String value;
	private RegexConfigType regexConfigType;

	public RegexConfig() {
	}

	public RegexConfig(String inputMetadata, Pattern regex, String value, RegexConfigType regexConfigType) {
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

	public Pattern getRegex() {
		return regex;
	}

	public void setRegex(Pattern regex) {
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
