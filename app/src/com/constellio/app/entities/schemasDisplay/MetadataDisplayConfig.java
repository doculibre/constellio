package com.constellio.app.entities.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;

import java.io.Serializable;

public class MetadataDisplayConfig implements Serializable {

	private final String metadataCode;

	private final String collection;

	private final boolean visibleInAdvancedSearch;

	private final MetadataInputType inputType;

	private final MetadataDisplayType displayType;

	private final String metadataGroupCode;

	private final boolean highlight;

	private final String helpMessage;

	public MetadataDisplayConfig(String collection, String metadataCode, boolean visibleInAdvancedSearch,
								 MetadataInputType inputType, boolean highlight, String metadataGroupCode,
								 MetadataDisplayType displayType, String helpMessage) {
		this.collection = collection;
		this.metadataCode = metadataCode;
		this.visibleInAdvancedSearch = visibleInAdvancedSearch;
		this.inputType = inputType;
		this.highlight = highlight;
		this.metadataGroupCode = metadataGroupCode;
		this.displayType = displayType;
		this.helpMessage = (helpMessage == null ? "" : helpMessage);
	}

	public boolean isVisibleInAdvancedSearch() {
		return visibleInAdvancedSearch;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public MetadataInputType getInputType() {
		return inputType;
	}

	public MetadataDisplayType getDisplayType() {
		return displayType;
	}

	public String getMetadataCode() {
		return metadataCode;
	}

	public String getCollection() {
		return collection;
	}

	public String getMetadataGroupCode() {
		return metadataGroupCode;
	}

	public String getHelpMessage() {
		return helpMessage;
	}

	public MetadataDisplayConfig withVisibleInAdvancedSearchStatus(boolean visibleInAdvancedSearch) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	public MetadataDisplayConfig withHighlightStatus(boolean highlight) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	public MetadataDisplayConfig withInputType(MetadataInputType inputType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	public MetadataDisplayConfig withDisplayType(MetadataDisplayType displayType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	public MetadataDisplayConfig withMetadataGroup(String metadataGroupCode) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	public static MetadataDisplayConfig inheriting(String metadataCode, MetadataDisplayConfig inheritance) {
		return new MetadataDisplayConfig(inheritance.collection, metadataCode, inheritance.visibleInAdvancedSearch,
				inheritance.inputType, inheritance.highlight, inheritance.metadataGroupCode, inheritance.displayType, inheritance.helpMessage);
	}

	public MetadataDisplayConfig withCode(String code) {
		return new MetadataDisplayConfig(collection, code, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	public MetadataDisplayConfig withHelpMessage(String helpMessage) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType, helpMessage);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MetadataDisplayConfig)) {
			return false;
		}

		MetadataDisplayConfig that = (MetadataDisplayConfig) o;

		if (visibleInAdvancedSearch != that.visibleInAdvancedSearch) {
			return false;
		}
		if (highlight != that.highlight) {
			return false;
		}
		if (metadataCode != null ? !metadataCode.equals(that.metadataCode) : that.metadataCode != null) {
			return false;
		}
		if (collection != null ? !collection.equals(that.collection) : that.collection != null) {
			return false;
		}
		if (inputType != that.inputType) {
			return false;
		}
		if (displayType != that.displayType) {
			return false;
		}
		if (helpMessage != that.helpMessage) {
			return false;
		}

		return metadataGroupCode != null ? metadataGroupCode.equals(that.metadataGroupCode) : that.metadataGroupCode == null;
	}

	@Override
	public int hashCode() {
		int result = metadataCode != null ? metadataCode.hashCode() : 0;
		result = 31 * result + (collection != null ? collection.hashCode() : 0);
		result = 31 * result + (visibleInAdvancedSearch ? 1 : 0);
		result = 31 * result + (inputType != null ? inputType.hashCode() : 0);
		result = 31 * result + (displayType != null ? displayType.hashCode() : 0);
		result = 31 * result + (metadataGroupCode != null ? metadataGroupCode.hashCode() : 0);
		result = 31 * result + (highlight ? 1 : 0);
		result = 31 * result + (helpMessage != null ? helpMessage.hashCode() : 0);
		return result;
	}
}
