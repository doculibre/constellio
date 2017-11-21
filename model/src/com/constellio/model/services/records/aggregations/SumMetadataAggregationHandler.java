package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;

public class SumMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public SumMetadataAggregationHandler() {
		super("sum");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {

		double sum = 0.0;

		for (Object value : params.getValues()) {
			if (value instanceof Number) {
				sum += ((Number) value).doubleValue();
			}
		}

		return sum;
	}
}
