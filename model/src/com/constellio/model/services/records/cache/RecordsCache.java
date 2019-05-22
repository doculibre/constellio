package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface RecordsCache {

	Record get(String id);

	Record getSummary(String id);

	List<Record> getAllValues(String schemaType);

	List<Record> getAllValuesInUnmodifiableState(String schemaType);

	CacheInsertionStatus insert(Record record, InsertionReason insertionReason);

	void invalidateRecordsOfType(String recordType);

	void invalidate(List<String> recordIds);

	void invalidate(String recordId);

	void configureCache(CacheConfig cacheConfig);

	Collection<CacheConfig> getConfiguredCaches();

	CacheConfig getCacheConfigOf(String schemaOrTypeCode);

	void invalidateAll();

	Record getByMetadata(Metadata metadata, String value);

	Record getSummaryByMetadata(Metadata metadata, String value);

	void removeCache(String schemaType);

	boolean isConfigured(MetadataSchemaType type);

	boolean isConfigured(String typeCode);

	boolean isEmpty();

	@Deprecated
	default boolean isCached(String id) {
		return getSummary(id) != null;
	}

	default List<CacheInsertionStatus> insert(List<Record> records, InsertionReason insertionReason) {
		List<CacheInsertionStatus> statuses = new ArrayList<>(records.size());

		for (Record record : records) {
			statuses.add(insert(record, insertionReason));
		}

		return statuses;
	}
}
