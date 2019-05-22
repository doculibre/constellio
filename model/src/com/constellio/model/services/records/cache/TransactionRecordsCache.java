package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.Collection;
import java.util.List;

public class TransactionRecordsCache implements RecordsCache {

	private RecordsCache recordsCache;

	private Transaction transaction;

	public TransactionRecordsCache(RecordsCache recordsCache, Transaction transaction) {
		this.recordsCache = recordsCache;
		this.transaction = transaction;
	}

	@Override
	public Record get(String id) {
		Record recordInTransaction = transaction.getRecord(id);
		return recordInTransaction != null ? recordInTransaction : recordsCache.get(id);
	}

	@Override
	public Record getSummary(String id) {
		Record recordInTransaction = transaction.getRecord(id);
		return recordInTransaction != null ? recordInTransaction : recordsCache.getSummary(id);
	}


	@Override
	public Record getByMetadata(
			Metadata metadata, String value) {

		for (Record record : transaction.getRecords()) {
			if (record.getTypeCode().equals(metadata.getSchemaTypeCode()) && value.equals(record.get(metadata))) {
				return record;
			}
		}

		return recordsCache.getByMetadata(metadata, value);
	}

	@Override
	public Record getSummaryByMetadata(
			Metadata metadata, String value) {

		for (Record record : transaction.getRecords()) {
			if (record.getTypeCode().equals(metadata.getSchemaTypeCode()) && value.equals(record.get(metadata))) {
				return record;
			}
		}

		return recordsCache.getSummaryByMetadata(metadata, value);
	}


	@Override
	public boolean isCached(String id) {
		Record recordInTransaction = transaction.getRecord(id);
		return recordInTransaction != null || recordsCache.isCached(id);
	}

	@Override
	public List<CacheInsertionStatus> insert(List<Record> record,
											 InsertionReason insertionReason) {
		return recordsCache.insert(record, insertionReason);
	}

	@Override
	public List<Record> getAllValues(String schemaType) {
		return recordsCache.getAllValues(schemaType);
	}

	@Override
	public List<Record> getAllValuesInUnmodifiableState(
			String schemaType) {
		return recordsCache.getAllValuesInUnmodifiableState(schemaType);
	}

	@Override
	public CacheInsertionStatus insert(Record record,
									   InsertionReason insertionReason) {
		return recordsCache.insert(record, insertionReason);
	}

	@Override
	public void invalidateRecordsOfType(String recordType) {
		recordsCache.invalidateRecordsOfType(recordType);
	}

	@Override
	public void invalidate(List<String> recordIds) {
		recordsCache.invalidate(recordIds);
	}

	@Override
	public void invalidate(String recordId) {
		recordsCache.invalidate(recordId);
	}

	@Override
	public void configureCache(CacheConfig cacheConfig) {
		recordsCache.configureCache(cacheConfig);
	}

	@Override
	public Collection<CacheConfig> getConfiguredCaches() {
		return recordsCache.getConfiguredCaches();
	}

	@Override
	public CacheConfig getCacheConfigOf(String schemaOrTypeCode) {
		return recordsCache.getCacheConfigOf(schemaOrTypeCode);
	}

	@Override
	public void invalidateAll() {
		recordsCache.invalidateAll();
	}


	@Override
	public void removeCache(String schemaType) {
		recordsCache.removeCache(schemaType);
	}

	@Override
	public boolean isConfigured(MetadataSchemaType type) {
		return recordsCache.isConfigured(type);
	}

	@Override
	public boolean isConfigured(String typeCode) {
		return recordsCache.isConfigured(typeCode);
	}

	@Override
	public boolean isEmpty() {
		return recordsCache.isEmpty();
	}

}
