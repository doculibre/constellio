package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.schemas.entries.ReindexingAggregatedValuesParams;

public interface MetadataAggregationWithFastReindexingHandler extends MetadataAggregationHandler {

	Object calculate(ReindexingAggregatedValuesParams params);
}
