package com.constellio.model.services.records.reindexing;

import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;

import java.util.List;

public interface ReindexingAggregatedValuesTempStorage {

	void addOrReplace(String recordIdAggregatingValues, String recordId, String metadataLocalCode, List<Object> values);

	List<Object> getAllValues(String recordIdAggregatingValues, String inputMetadataLocalCode);

	void clear();

	List<AggregatedValuesEntry> getAllEntriesWithValues(String recordIdAggregatingValues);

	void incrementReferenceCount(String recordIdAggregatingValues, String aggregatedMetadataLocalCode);

	int getReferenceCount(String recordIdAggregatingValues, String aggregatedMetadataLocalCode);

	void populateCacheConsumptionInfos(SystemReindexingConsumptionInfos infos);

}
