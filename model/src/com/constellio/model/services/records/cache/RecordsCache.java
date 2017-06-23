package com.constellio.model.services.records.cache;

import java.util.Collection;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public interface RecordsCache {

	Record get(String id);

	boolean isCached(String id);

	void insert(List<Record> record);

	void insertQueryResults(LogicalSearchQuery query, List<Record> records);

	List<Record> getQueryResults(LogicalSearchQuery query);

	Record insert(Record record);

	Record forceInsert(Record record);

	void invalidateRecordsOfType(String recordType);

	void invalidate(List<String> recordIds);

	void invalidate(String recordId);

	void configureCache(CacheConfig cacheConfig);

	Collection<CacheConfig> getConfiguredCaches();

	CacheConfig getCacheConfigOf(String schemaOrTypeCode);

	void invalidateAll();

	Record getByMetadata(Metadata metadata, String value);

	void removeCache(String schemaType);

	boolean isConfigured(MetadataSchemaType type);

	boolean isConfigured(String typeCode);

	int getCacheObjectsCount();

	int getCacheObjectsCount(String typeCode);

	long getCacheObjectsSize(String typeCode);
}
