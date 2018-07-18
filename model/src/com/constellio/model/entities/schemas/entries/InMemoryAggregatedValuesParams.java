package com.constellio.model.entities.schemas.entries;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;

public abstract class InMemoryAggregatedValuesParams {
	List<Object> values;

	private Metadata aggregateMetadata;

	private String recordId;

	public InMemoryAggregatedValuesParams(String recordId, Metadata aggregateMetadata, List<Object> values) {
		this.values = values;
		this.aggregateMetadata = aggregateMetadata;
		this.recordId = recordId;
	}

	public <T> List<T> getValues() {
		return (List) values;
	}

	public abstract List<AggregatedValuesEntry> getEntries();

	public abstract int getReferenceCount();

	public Metadata getMetadata() {
		return aggregateMetadata;
	}

	public String getRecordId() {
		return recordId;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return (AggregatedDataEntry) aggregateMetadata.getDataEntry();
	}
}
