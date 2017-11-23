package com.constellio.model.services.records.reindexing;

import java.util.List;

import com.constellio.model.entities.schemas.Metadata;

public interface ReindexingAggregatedValuesTempStorage {

	void addOrReplace(String recordIdAggregatingValues, String recordId, Metadata inputMetadata, List<Object> values);

	List<Object> getAllValues(String recordIdAggregatingValues, Metadata inputMetadata);

	void clear();

}
