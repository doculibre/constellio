package com.constellio.model.services.records.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RecordsCacheRequestImpl implements RecordsCache {

	Map<String, Record> cache = new HashMap<>();

	RecordsCache nested;

	public RecordsCacheRequestImpl(RecordsCache nested) {
		this.nested = nested;
	}

	@Override
	public Record get(String id) {
		if (cache.containsKey(id)) {
			return cache.get(id).getCopyOfOriginalRecord();
		}
		System.out.println("get " + id);
		return nested.get(id);
	}

	@Override
	public boolean isCached(String id) {
		return cache.containsKey(id) || nested.isCached(id);
	}

	@Override
	public void insert(List<Record> records) {
		for (Record record : records) {
			insert(record);
		}
	}

	@Override
	public void insertQueryResults(LogicalSearchQuery query, List<Record> records) {
		for (Record record : records) {
			insertInRequestcache(record);
		}
		nested.insertQueryResults(query, records);
	}

	@Override
	public List<Record> getQueryResults(LogicalSearchQuery query) {
		return nested.getQueryResults(query);
	}

	@Override
	public Record insert(Record record) {
		insertInRequestcache(record);
		return nested.insert(record);
	}

	private void insertInRequestcache(Record insertedRecord) {
		if (insertedRecord == null || insertedRecord.isDirty() || !insertedRecord.isSaved()) {
			return;
		}

		if (!insertedRecord.isFullyLoaded()) {
			invalidate(insertedRecord.getId());
			return;
		}

		forceInsertInRequestcache(insertedRecord);
	}

	private void forceInsertInRequestcache(Record record) {
		cache.put(record.getId(), record);
	}

	@Override
	public Record forceInsert(Record record) {
		forceInsertInRequestcache(record);
		return nested.forceInsert(record);
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		for (Map.Entry<String, Record> entry : cache.entrySet()) {
			if (entry.getValue().getTypeCode().equals(recordType)) {
				cache.remove(entry.getKey());
			}
		}
		nested.invalidateRecordsOfType(recordType);
	}

	@Override
	public void invalidate(List<String> recordIds) {
		for (String recordId : recordIds) {
			invalidate(recordId);
		}
	}

	@Override
	public void invalidate(String recordId) {
		cache.remove(recordId);
		nested.invalidate(recordId);
	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {
		nested.configureCache(cacheConfig);
	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return nested.getConfiguredCaches();
	}

	@Override
	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		return nested.getCacheConfigOf(schemaOrTypeCode);
	}

	@Override
	public void invalidateAll() {
		cache.clear();
		nested.invalidateAll();
	}

	@Override
	public Record getByMetadata(Metadata metadata, String value) {
		return nested.getByMetadata(metadata, value);
	}

	@Override
	public void removeCache(String schemaType) {
		invalidateRecordsOfType(schemaType);
		nested.removeCache(schemaType);
	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return nested.isConfigured(type);
	}

	@Override
	public boolean isConfigured(String typeCode) {
		return nested.isConfigured(typeCode);
	}

	@Override
	public int getCacheObjectsCount() {
		return nested.getCacheObjectsCount();
	}
}

