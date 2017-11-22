package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;

public class MaxMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public MaxMetadataAggregationHandler() {
		super("max");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		Double max = null;

		for (Object value : params.getValues()) {
			if (value instanceof Number
					&& (max == null || max.doubleValue() < ((Number) value).doubleValue())) {
				max = ((Number) value).doubleValue();
			}
		}

		return max;

	}
}
