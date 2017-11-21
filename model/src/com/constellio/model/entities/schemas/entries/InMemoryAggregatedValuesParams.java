package com.constellio.model.entities.schemas.entries;

import java.util.List;

public class InMemoryAggregatedValuesParams {
	List<Object> values;

	public InMemoryAggregatedValuesParams(List<Object> values) {
		this.values = values;
	}

	public List<Object> getValues() {
		return values;
	}
}
