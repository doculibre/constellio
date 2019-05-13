package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.Collection;
import java.util.List;

public class DefaultRecordsCacheAdapter implements RecordsCache {

	protected RecordsCache nestedRecordsCache;

	public DefaultRecordsCacheAdapter(RecordsCache nestedRecordsCache) {
		this.nestedRecordsCache = nestedRecordsCache;
	}

	public Record get(String id) {
		return nestedRecordsCache.get(id);
	}

	public Record getSummary(String id) {
		return nestedRecordsCache.getSummary(id);
	}

	public boolean isCached(String id) {
		return nestedRecordsCache.isCached(id);
	}

	public List<CacheInsertionStatus> insert(List<Record> record, InsertionReason reason) {
		return nestedRecordsCache.insert(record, reason);
	}

	public List<Record> getAllValues(String schemaType) {
		return nestedRecordsCache.getAllValues(schemaType);
	}

	public List<Record> getAllValuesInUnmodifiableState(String schemaType) {
		return nestedRecordsCache.getAllValuesInUnmodifiableState(schemaType);
	}

	public CacheInsertionStatus insert(Record record, InsertionReason reason) {
		return nestedRecordsCache.insert(record, reason);
	}

	public CacheInsertionStatus forceInsert(Record record, InsertionReason reason) {
		return nestedRecordsCache.forceInsert(record, reason);
	}

	public void invalidateRecordsOfType(String recordType) {
		nestedRecordsCache.invalidateRecordsOfType(recordType);
	}

	public void invalidate(List<String> recordIds) {
		nestedRecordsCache.invalidate(recordIds);
	}

	public void invalidate(String recordId) {
		nestedRecordsCache.invalidate(recordId);
	}

	public void configureCache(CacheConfig cacheConfig) {
		nestedRecordsCache.configureCache(cacheConfig);
	}

	public Collection<CacheConfig> getConfiguredCaches() {
		return nestedRecordsCache.getConfiguredCaches();
	}

	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		return nestedRecordsCache.getCacheConfigOf(schemaOrTypeCode);
	}

	public void invalidateAll() {
		nestedRecordsCache.invalidateAll();
	}

	public Record getByMetadata(Metadata metadata,
								String value) {
		return nestedRecordsCache.getByMetadata(metadata, value);
	}

	public Record getSummaryByMetadata(Metadata metadata,
									   String value) {
		return nestedRecordsCache.getSummaryByMetadata(metadata, value);
	}

	public void removeCache(String schemaType) {
		nestedRecordsCache.removeCache(schemaType);
	}

	public boolean isConfigured(MetadataSchemaType type) {
		return nestedRecordsCache.isConfigured(type);
	}

	public boolean isConfigured(String typeCode) {
		return nestedRecordsCache.isConfigured(typeCode);
	}

	public int getCacheObjectsCount() {
		return nestedRecordsCache.getCacheObjectsCount();
	}

	public int getCacheObjectsCount(String typeCode) {
		return nestedRecordsCache.getCacheObjectsCount(typeCode);
	}

	public long getCacheObjectsSize(String typeCode) {
		return nestedRecordsCache.getCacheObjectsSize(typeCode);
	}

	public boolean isEmpty() {
		return nestedRecordsCache.isEmpty();
	}

}
