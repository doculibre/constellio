package com.constellio.model.services.records.aggregations;

import com.constellio.model.entities.records.Record;

public interface ReindexingMetadataAggregationCalculator {

	void onReferencedBy(Record record);

	Object calculateFinalValue();

}
