package com.constellio.app.entities.schemasDisplay;

import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;

public class MetadataDisplayConfig {

	private final String metadataCode;

	private final String collection;

	private final boolean visibleInAdvancedSearch;

	private final MetadataInputType inputType;

	private final String metadataGroup;

	private final boolean highlight;

	public MetadataDisplayConfig(String collection, String metadataCode, boolean visibleInAdvancedSearch,
			MetadataInputType inputType, boolean highlight, String metadataGroup) {
		this.collection = collection;
		this.metadataCode = metadataCode;
		this.visibleInAdvancedSearch = visibleInAdvancedSearch;
		this.inputType = inputType;
		this.highlight = highlight;
		this.metadataGroup = metadataGroup;
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

	public String getMetadataCode() {
		return metadataCode;
	}

	public String getCollection() {
		return collection;
	}

	public String getMetadataGroup() {
		return metadataGroup;
	}

	public MetadataDisplayConfig withVisibleInAdvancedSearchStatus(boolean visibleInAdvancedSearch) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight, metadataGroup);
	}

	public MetadataDisplayConfig withHighlightStatus(boolean highlight) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight, metadataGroup);
	}

	public MetadataDisplayConfig withInputType(MetadataInputType inputType) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight, metadataGroup);
	}

	public MetadataDisplayConfig withMetadataGroup(String metadataGroup) {
		return new MetadataDisplayConfig(collection, metadataCode, visibleInAdvancedSearch, inputType, highlight, metadataGroup);
	}

	public static MetadataDisplayConfig inheriting(String metadataCode, MetadataDisplayConfig inheritance) {
		return new MetadataDisplayConfig(inheritance.collection, metadataCode, inheritance.visibleInAdvancedSearch,
				inheritance.inputType, inheritance.highlight, inheritance.metadataGroup);
	}
}
