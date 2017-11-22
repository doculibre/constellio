package com.constellio.model.entities.schemas.entries;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;

public class InMemoryAggregatedValuesParams {
	List<Object> values;

	private Metadata aggregateMetadata;

	public InMemoryAggregatedValuesParams(Metadata aggregateMetadata, List<Object> values) {
		this.values = values;
		this.aggregateMetadata = aggregateMetadata;
	}

	public <T> List<T> getValues() {
		return (List) values;
	}

	public Metadata getMetadata() {
		return aggregateMetadata;
	}

	public AggregatedDataEntry getAggregatedDataEntry() {
		return (AggregatedDataEntry) aggregateMetadata.getDataEntry();
	}
}
