package com.constellio.model.services.records.aggregations;

import java.util.List;

public interface ReindexingAggregatedValuesTempStorage {

	void add(String recordIdAggregatingValues, AggregatedMetadataReindexingValueInput entry);

	List<AggregatedMetadataReindexingValueInput> get(String recordIdAggregatingValues);

	void clear();

}
