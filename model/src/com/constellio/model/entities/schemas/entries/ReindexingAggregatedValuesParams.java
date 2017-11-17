package com.constellio.model.entities.schemas.entries;

import java.util.List;

public class ReindexingAggregatedValuesParams {
	List<Object> values;

	public ReindexingAggregatedValuesParams(List<Object> values) {
		this.values = values;
	}

	public List<Object> getValues() {
		return values;
	}
}
