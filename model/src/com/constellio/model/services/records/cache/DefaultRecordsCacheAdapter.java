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

	public List<CacheInsertionResponse> insert(List<Record> record, InsertionReason reason) {
		return nestedRecordsCache.insert(record, reason);
	}

	public List<Record> getAllValues(String schemaType) {
		return nestedRecordsCache.getAllValues(schemaType);
	}

	public List<Record> getAllValuesInUnmodifiableState(String schemaType) {
		return nestedRecordsCache.getAllValuesInUnmodifiableState(schemaType);
	}

	public CacheInsertionResponse insert(Record record, InsertionReason reason) {
		return nestedRecordsCache.insert(record, reason);
	}

	public void reloadSchemaType(String recordType, boolean onlyLocally, boolean forceVolatileCacheClear) {
		nestedRecordsCache.reloadSchemaType(recordType, onlyLocally);
	}

	public void removeFromAllCaches(List<String> recordIds) {
		nestedRecordsCache.removeFromAllCaches(recordIds);
	}

	public void removeFromAllCaches(String recordId) {
		nestedRecordsCache.removeFromAllCaches(recordId);
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

	public void invalidateVolatileReloadPermanent(List<String> schemaTypes, boolean onlyLocally) {
		nestedRecordsCache.invalidateVolatileReloadPermanent(schemaTypes, onlyLocally);
	}

	public Record getByMetadata(Metadata metadata,
								String value) {
		return nestedRecordsCache.getByMetadata(metadata, value);
	}

	public Record getSummaryByMetadata(Metadata metadata,
									   String value) {
		return nestedRecordsCache.getSummaryByMetadata(metadata, value);
	}

	public boolean isConfigured(MetadataSchemaType type) {
		return nestedRecordsCache.isConfigured(type);
	}

	public boolean isConfigured(String typeCode) {
		return nestedRecordsCache.isConfigured(typeCode);
	}


	public boolean isEmpty() {
		return nestedRecordsCache.isEmpty();
	}

}
