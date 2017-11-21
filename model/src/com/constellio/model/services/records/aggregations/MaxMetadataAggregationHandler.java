package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;

public class MaxMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public MaxMetadataAggregationHandler() {
		super("max");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		return 0.0;
	}
}
