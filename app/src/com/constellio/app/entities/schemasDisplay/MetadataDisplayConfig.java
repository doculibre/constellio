package com.constellio.app.entities.schemasDisplay;

import java.io.Serializable;

import com.constellio.app.entities.schemasDisplay.enums.MetadataDisplayType;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;

public class MetadataDisplayConfig implements Serializable {

	private final String metadataCode;

	private final String collection;

	private final boolean visibleInAdvancedSearch;

	private final MetadataInputType inputType;

	private final MetadataDisplayType displayType;

	private final String metadataGroupCode;

	private final boolean highlight;

	public MetadataDisplayConfig(String collection, String metadataCode, boolean visibleInAdvancedSearch,
								 MetadataInputType inputType, boolean highlight, String metadataGroupCode,
								 MetadataDisplayType displayType) {
		this.collection = collection;
		this.metadataCode = metadataCode;
		this.visibleInAdvancedSearch = visibleInAdvancedSearch;
		this.inputType = inputType;
		this.highlight = highlight;
		this.metadataGroupCode = metadataGroupCode;
		this.displayType = displayType;
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

	public MetadataDisplayConfig withVisibleInAdvancedSearchStatus(boolean visibleInAdvancedSearch) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType);
	}

	public MetadataDisplayConfig withHighlightStatus(boolean highlight) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType);
	}

	public MetadataDisplayConfig withInputType(MetadataInputType inputType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType);
	}

	public MetadataDisplayConfig withDisplayType(MetadataDisplayType displayType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType);
	}

	public MetadataDisplayConfig withMetadataGroup(String metadataGroupCode) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType);
	}

	public static MetadataDisplayConfig inheriting(String metadataCode, MetadataDisplayConfig inheritance) {
		return new MetadataDisplayConfig(inheritance.collection, metadataCode, inheritance.visibleInAdvancedSearch,
				inheritance.inputType, inheritance.highlight, inheritance.metadataGroupCode, inheritance.displayType);
	}

	public MetadataDisplayConfig withCode(String code) {
		return new MetadataDisplayConfig(collection, code, visibleInAdvancedSearch, inputType, highlight,
				metadataGroupCode, displayType);
	}
}
