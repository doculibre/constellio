package com.constellio.model.services.records.aggregations;

public class AggregatedMetadataReindexingValueInput {

	String metadata;

	Object value;

	public AggregatedMetadataReindexingValueInput(String metadata, Object value) {
		this.metadata = metadata;
		this.value = value;
	}

	public String getMetadata() {
		return metadata;
	}

	public Object getValue() {
		return value;
	}
}
