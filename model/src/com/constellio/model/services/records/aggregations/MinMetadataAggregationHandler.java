package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;

public class MinMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public MinMetadataAggregationHandler() {
		super("min");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		Double min = null;

		for (Object value : params.getValues()) {
			if (value instanceof Number
					&& (min == null || min.doubleValue() > ((Number) value).doubleValue())) {
				min = ((Number) value).doubleValue();
			}
		}

		return min;

	}
}
