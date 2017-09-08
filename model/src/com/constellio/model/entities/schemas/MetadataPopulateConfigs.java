package com.constellio.model.entities.schemas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.model.services.records.extractions.MetadataPopulator;

public class MetadataPopulateConfigs implements Serializable {
	private final List<MetadataPopulator> metadataPopulators = new ArrayList<>();

	private final List<String> styles = new ArrayList<>();
	private final List<String> properties = new ArrayList<>();
	private final List<RegexConfig> regexes = new ArrayList<>();

	public MetadataPopulateConfigs() {
	}

	public MetadataPopulateConfigs(List<String> styles, List<String> properties, List<RegexConfig> regexes,
			List<MetadataPopulator> metadataPopulators) {
		this.styles.addAll(styles);
		this.properties.addAll(properties);
		this.regexes.addAll(regexes);
		this.metadataPopulators.addAll(metadataPopulators);
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

	public boolean isEmpty() {
		if (styles.isEmpty() && properties.isEmpty() && regexes.isEmpty() && metadataPopulators.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MetadataPopulateConfigs that = (MetadataPopulateConfigs) o;

		if (styles != null ? !styles.equals(that.styles) : that.styles != null)
			return false;
		if (properties != null ? !properties.equals(that.properties) : that.properties != null)
			return false;
		if (metadataPopulators != null ? !metadataPopulators.equals(this.metadataPopulators) : that.metadataPopulators != null)
			return false;
		return !(regexes != null ? !regexes.equals(that.regexes) : that.regexes != null);

	}

	@Override
	public int hashCode() {
		int result = styles != null ? styles.hashCode() : 0;
		result = 31 * result + (properties != null ? properties.hashCode() : 0);
		result = 31 * result + (regexes != null ? regexes.hashCode() : 0);
		result = 31 * result + (metadataPopulators != null ? metadataPopulators.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "MetadataPopulateConfigs [styles=" + styles + ", properties=" + properties + ", regexes=" + regexes + "]";
	}

	public boolean isConfigured() {
		return !styles.isEmpty() || !properties.isEmpty() || !regexes.isEmpty() || !metadataPopulators.isEmpty();
	}
}

