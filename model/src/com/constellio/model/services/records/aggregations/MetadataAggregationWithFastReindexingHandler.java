package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.records.Record;

public interface MetadataAggregationWithFastReindexingHandler extends MetadataAggregationHandler {

	ReindexingMetadataAggregationCalculator newReindexingMetadataAggregationCalculator(Record record);

}
