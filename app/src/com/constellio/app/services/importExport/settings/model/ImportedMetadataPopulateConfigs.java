package com.constellio.app.services.importExport.settings.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ImportedMetadataPopulateConfigs {

	//	private List<String> styles = new ArrayList<>();
	//	private List<String> properties = new ArrayList<>();
	private List<ImportedRegexConfigs> regexes = new ArrayList<>();

	public ImportedMetadataPopulateConfigs() {
	}
	//
	//	public ImportedMetadataPopulateConfigs setStyles(List<String> styles) {
	//		this.styles = styles;
	//		return this;
	//	}
	//
	//	public ImportedMetadataPopulateConfigs setProperties(List<String> properties) {
	//		this.properties = properties;
	//		return this;
	//	}

	public ImportedMetadataPopulateConfigs setRegexes(
			List<ImportedRegexConfigs> regexes) {
		this.regexes = regexes;
		return this;
	}

	//	public List<String> getStyles() {
	//		return styles;
	//	}
	//
	//	public List<String> getProperties() {
	//		return properties;
	//	}

	public List<ImportedRegexConfigs> getRegexes() {
		return regexes;
	}

	public ImportedMetadataPopulateConfigs addRegexConfigs(ImportedRegexConfigs configs) {
		regexes.add(configs);
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

}
