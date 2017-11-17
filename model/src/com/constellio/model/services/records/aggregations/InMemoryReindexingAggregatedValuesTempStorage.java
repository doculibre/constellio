package com.constellio.model.services.records.aggregations;

import java.util.List;

import com.constellio.data.utils.KeyListMap;

public class InMemoryReindexingAggregatedValuesTempStorage implements ReindexingAggregatedValuesTempStorage {

	KeyListMap<String, AggregatedMetadataReindexingValueInput> entries = new KeyListMap<>();

	@Override
	public void add(String recordIdAggregatingValues, AggregatedMetadataReindexingValueInput entry) {
		entries.add(recordIdAggregatingValues, entry);
	}

	@Override
	public List<AggregatedMetadataReindexingValueInput> get(String recordIdAggregatingValues) {
		return entries.get(recordIdAggregatingValues);
	}

	@Override
	public void clear() {
		entries.clear();
	}
}
