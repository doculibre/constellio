package com.constellio.model.services.schemas.builders;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.schemas.MetadataPopulateConfigs;
import com.constellio.model.entities.schemas.RegexConfig;

public class MetadataPopulateConfigsBuilder {

	private List<String> styles = new ArrayList<>();
	private List<String> properties = new ArrayList<>();
	private List<RegexConfig> regexes = new ArrayList<>();

	public static MetadataPopulateConfigsBuilder modify(MetadataPopulateConfigs populateConfigs) {
		MetadataPopulateConfigsBuilder builder = new MetadataPopulateConfigsBuilder();
		builder.styles.addAll(populateConfigs.getStyles());
		builder.properties.addAll(populateConfigs.getProperties());
		builder.regexes.addAll(populateConfigs.getRegexes());
		return builder;
	}

	public static MetadataPopulateConfigsBuilder modify(MetadataPopulateConfigsBuilder populateConfigsBuilder) {
		MetadataPopulateConfigsBuilder builder = new MetadataPopulateConfigsBuilder();
		builder.styles.addAll(populateConfigsBuilder.getStyles());
		builder.properties.addAll(populateConfigsBuilder.getProperties());
		builder.regexes.addAll(populateConfigsBuilder.getRegexes());
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
		if (styles.isEmpty() && properties.isEmpty() && regexes.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public MetadataPopulateConfigs build() {
		return new MetadataPopulateConfigs(styles, properties, regexes);
	}

	@Override
	public String toString() {
		return "MetadataPopulateConfigsBuilder [styles=" + styles + ", properties=" + properties + ", regexes=" + regexes + "]";
	}

}
