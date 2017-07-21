package com.constellio.model.services.records.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class RecordsCacheRequestImpl implements RecordsCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordsCacheRequestImpl.class);

	Map<String, Record> cache = new HashMap<>();

	RecordsCache nested;

	private String cacheId;

	private boolean disconnected;

	public RecordsCacheRequestImpl(String cacheId, RecordsCache nested) {
		this.nested = nested;
		this.cacheId = cacheId;
	}

	@Override
	public Record get(String id) {
		Record recordFromRequestCache = null;
		if (cache.containsKey(id)) {
			recordFromRequestCache = cache.get(id).getCopyOfOriginalRecord();
		}

		if (!Toggle.TEST_REQUEST_CACHE.isEnabled() && recordFromRequestCache != null) {
			return recordFromRequestCache;
		}

		Record record = nested.get(id);
		if (Toggle.TEST_REQUEST_CACHE.isEnabled()) {
			if (record != null && recordFromRequestCache != null && record.getVersion() != recordFromRequestCache.getVersion()) {
				throw new RuntimeException("Version mismatch with record " + record.getIdTitle() + " in request cache " + cacheId
						+ ". Request version '" + recordFromRequestCache.getVersion() + "' doesn't match global cache version"
						+ "'" + record.getVersion() + "'");
			}
			if (recordFromRequestCache != null) {
				return recordFromRequestCache;
			}
		}

		if (record != null) {
			insertInRequestcache(record);
		}
		return record;
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
		if (Toggle.LOG_REQUEST_CACHE.isEnabled()) {
			if (!record.getSchemaCode().startsWith("event")) {
				LOGGER.info("inserting in request cache " + record.getIdTitle() + " with version " + record.getVersion()
						+ " in cache " + cacheId);
				((RecordsCacheImpl) nested).doNotLog.add(record.getId() + "_" + record.getVersion());
			}
		}
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
		if (!disconnected) {
			cache.put(record.getId(), record);
		}
	}

	@Override
	public Record forceInsert(Record record) {
		forceInsertInRequestcache(record);
		return nested.forceInsert(record);
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		for (Map.Entry<String, Record> entry : new ArrayList<>(cache.entrySet())) {
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

		Record recordFromRequestCache = null;
		String metadataTypeCode = SchemaUtils.getSchemaTypeCode(metadata.getSchemaCode());
		for (Record cachedRecord : cache.values()) {
			if (metadataTypeCode.equals(cachedRecord.getTypeCode())) {
				Object recordValue = cachedRecord.get(metadata);
				if (recordValue != null && recordValue.equals(value)) {
					recordFromRequestCache = cachedRecord;
				}
			}
		}

		if (!Toggle.TEST_REQUEST_CACHE.isEnabled()) {
			if (recordFromRequestCache != null) {
				return recordFromRequestCache;
			}
		}

		Record record = nested.getByMetadata(metadata, value);
		if (Toggle.TEST_REQUEST_CACHE.isEnabled()) {
			if (record != null && recordFromRequestCache != null && record.getVersion() != recordFromRequestCache.getVersion()) {
				throw new RuntimeException("Request cache : Version mismatch with record " + record.getIdTitle());
			}

			if (recordFromRequestCache != null) {
				return recordFromRequestCache;
			}
		}

		if (record != null && !disconnected) {
			cache.put(record.getId(), record);
		}

		return record;
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

	@Override
	public int getCacheObjectsCount(String typeCode) {
		return this.cache.size();
	}

	@Override
	public long getCacheObjectsSize(String typeCode) {
		return 0;
	}

	public void disconnect() {
		disconnected = true;
		cache.clear();
	}
}

