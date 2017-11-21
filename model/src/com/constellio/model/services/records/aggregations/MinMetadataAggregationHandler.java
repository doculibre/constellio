package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.entries.InMemoryAggregatedValuesParams;

public class MinMetadataAggregationHandler extends SolrStatMetadataAggregationHandler {

	public MinMetadataAggregationHandler() {
		super("min");
	}

	@Override
	public Object calculate(InMemoryAggregatedValuesParams params) {
		return 0.0;
	}
}
