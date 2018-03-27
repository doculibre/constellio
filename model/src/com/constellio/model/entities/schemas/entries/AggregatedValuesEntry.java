package com.constellio.model.entities.schemas.entries;

import java.util.List;

public class AggregatedValuesEntry {

	private String recordId;

	private String metadata;

	List<Object> values;

	public AggregatedValuesEntry(String recordId, String metadata, List<Object> values) {
		this.recordId = recordId;
		this.metadata = metadata;
		this.values = values;
	}
}
