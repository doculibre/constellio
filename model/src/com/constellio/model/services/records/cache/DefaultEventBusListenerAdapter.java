package com.constellio.model.services.records.cache;

import java.util.Collection;
import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class DefaultEventBusListenerAdapter implements RecordsCache {

	protected RecordsCache nestedRecordsCache;

	public DefaultEventBusListenerAdapter(RecordsCache nestedRecordsCache) {
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

	public void insert(List<Record> record) {
		nestedRecordsCache.insert(record);
	}

	public void insertQueryResults(LogicalSearchQuery query,
			List<Record> records) {
		nestedRecordsCache.insertQueryResults(query, records);
	}

	public void insertQueryResultIds(LogicalSearchQuery query,
			List<String> recordIds) {
		nestedRecordsCache.insertQueryResultIds(query, recordIds);
	}

	public List<Record> getAllValues(String schemaType) {
		return nestedRecordsCache.getAllValues(schemaType);
	}

	public List<Record> getAllValuesInUnmodifiableState(String schemaType) {
		return nestedRecordsCache.getAllValuesInUnmodifiableState(schemaType);
	}

	public List<Record> getQueryResults(
			LogicalSearchQuery query) {
		return nestedRecordsCache.getQueryResults(query);
	}

	public List<String> getQueryResultIds(LogicalSearchQuery query) {
		return nestedRecordsCache.getQueryResultIds(query);
	}

	public CacheInsertionStatus insert(Record record) {
		return nestedRecordsCache.insert(record);
	}

	public CacheInsertionStatus forceInsert(Record record) {
		return nestedRecordsCache.forceInsert(record);
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

	public boolean isFullyLoaded(String schemaType) {
		return nestedRecordsCache.isFullyLoaded(schemaType);
	}

	public void markAsFullyLoaded(String schemaType) {
		nestedRecordsCache.markAsFullyLoaded(schemaType);
	}
}
