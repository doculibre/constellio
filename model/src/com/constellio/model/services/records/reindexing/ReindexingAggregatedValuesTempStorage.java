package com.constellio.model.services.records.reindexing;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.entries.AggregatedValuesEntry;

public interface ReindexingAggregatedValuesTempStorage {

	void addOrReplace(String recordIdAggregatingValues, String recordId, Metadata inputMetadata, List<Object> values);

	List<Object> getAllValues(String recordIdAggregatingValues, Metadata inputMetadata);

	void clear();

	List<AggregatedValuesEntry> getAllEntriesWithValues(String recordIdAggregatingValues);

	void incrementReferenceCount(String recordIdAggregatingValues);

	int getReferenceCount(String recordIdAggregatingValues);
}
