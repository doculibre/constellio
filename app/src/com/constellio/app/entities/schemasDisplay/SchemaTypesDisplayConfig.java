package com.constellio.app.entities.schemasDisplay;

import java.util.Collections;
import java.util.List;

public class SchemaTypesDisplayConfig {

	final String collection;

	final List<String> facetMetadataCodes;

	public SchemaTypesDisplayConfig(String collection, List<String> facetMetadataCodes) {
		this.collection = collection;
		this.facetMetadataCodes = Collections.unmodifiableList(facetMetadataCodes);
	}

	public SchemaTypesDisplayConfig(String collection) {
		this.collection = collection;
		this.facetMetadataCodes = Collections.emptyList();
	}

	public List<String> getFacetMetadataCodes() {
		return facetMetadataCodes;
	}

	public String getCollection() {
		return collection;
	}

	public SchemaTypesDisplayConfig withFacetMetadataCodes(List<String> facetMetadataCodes) {
		return new SchemaTypesDisplayConfig(collection, facetMetadataCodes);
	}
}

