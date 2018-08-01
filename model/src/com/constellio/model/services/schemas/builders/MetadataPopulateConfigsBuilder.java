package com.constellio.model.services.schemas.builders;

import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.services.records.extractions.MetadataPopulator;

import java.util.ArrayList;
import java.util.List;

public class MetadataPopulateConfigsBuilder {

	private List<String> styles = new ArrayList<>();
	private List<String> properties = new ArrayList<>();
	private List<RegexConfig> regexes = new ArrayList<>();
	private List<MetadataPopulator> metadataPopulators = new ArrayList<>();
	private Boolean isAddOnly = null;

	public static MetadataPopulateConfigsBuilder modify(MetadataPopulateConfigs populateConfigs) {
		MetadataPopulateConfigsBuilder builder = new MetadataPopulateConfigsBuilder();
		builder.styles.addAll(populateConfigs.getStyles());
		builder.properties.addAll(populateConfigs.getProperties());
		builder.regexes.addAll(populateConfigs.getRegexes());
		builder.metadataPopulators.addAll(populateConfigs.getMetadataPopulators());
		return builder;
	}

	public static MetadataPopulateConfigsBuilder modify(MetadataPopulateConfigsBuilder populateConfigsBuilder) {
		MetadataPopulateConfigsBuilder builder = new MetadataPopulateConfigsBuilder();
		builder.styles.addAll(populateConfigsBuilder.getStyles());
		builder.properties.addAll(populateConfigsBuilder.getProperties());
		builder.regexes.addAll(populateConfigsBuilder.getRegexes());
		builder.metadataPopulators.addAll(populateConfigsBuilder.getMetadataPopulators());
		return builder;
	}


	public static MetadataPopulateConfigsBuilder create() {
		return new MetadataPopulateConfigsBuilder();
	}

	public List<String> getStyles() {
		return styles;
	}

	public List<String> getProperties() {
		return properties;
	}

	public List<RegexConfig> getRegexes() {
		return regexes;
	}

	public List<MetadataPopulator> getMetadataPopulators() {
		return metadataPopulators;
	}

	public MetadataPopulateConfigsBuilder setStyles(List<String> styles) {
		List<String> newStyles = new ArrayList<>();
		for (String style : styles) {
			if (!newStyles.contains(style)) {
				newStyles.add(style);
			}
		}
		this.styles = newStyles;
		return this;
	}

	public MetadataPopulateConfigsBuilder setProperties(List<String> properties) {
		List<String> newProperties = new ArrayList<>();
		for (String property : properties) {
			if (!newProperties.contains(property)) {
				newProperties.add(property);
			}
		}
		this.properties = newProperties;
		return this;
	}

	public void setRegexes(List<RegexConfig> regexes) {
		this.regexes = regexes;
	}

	public void setMetadataPopulators(List<MetadataPopulator> metadataPopulators) {
		this.metadataPopulators = metadataPopulators;
	}

	public MetadataPopulateConfigsBuilder addStyle(String style) {
		if (!styles.contains(style)) {
			styles.add(style);
		}
		return this;
	}

	public MetadataPopulateConfigsBuilder removeStyle(String style) {
		styles.remove(style);
		return this;
	}

	public MetadataPopulateConfigsBuilder addProperty(String property) {
		if (!properties.contains(property)) {
			properties.add(property);
		}
		return this;
	}

	public MetadataPopulateConfigsBuilder removeProperty(String property) {
		properties.remove(property);
		return this;
	}

	public MetadataPopulateConfigsBuilder addRegex(RegexConfig regexConfig) {
		regexes.add(regexConfig);
		return this;
	}

	public boolean isEmpty() {
		if (styles.isEmpty() && properties.isEmpty() && regexes.isEmpty() && metadataPopulators.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public MetadataPopulateConfigsBuilder setAddOnly(Boolean addOnly) {
		isAddOnly = addOnly;
		return this;
	}

	public MetadataPopulateConfigs build() {
		return new MetadataPopulateConfigs(styles, properties, regexes, metadataPopulators, isAddOnly);
	}

	@Override
	public String toString() {
		return "MetadataPopulateConfigsBuilder [styles=" + styles + ", properties=" + properties
			   + ", regexes=" + regexes + "metadata_populator=" + metadataPopulators + "]";
	}

}
