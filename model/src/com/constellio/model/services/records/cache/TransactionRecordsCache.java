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
	public void reloadSchemaType(String recordType, boolean onlyLocally) {
		recordsCache.reloadSchemaType(recordType, onlyLocally);
	}

	@Override
	public void removeFromAllCaches(List<String> recordIds) {
		recordsCache.removeFromAllCaches(recordIds);
	}

	@Override
	public void removeFromAllCaches(String recordId) {
		recordsCache.removeFromAllCaches(recordId);
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
	public void invalidateVolatileReloadPermanent(List<String> schemaTypes, boolean onlyLocally) {
		recordsCache.invalidateVolatileReloadPermanent(schemaTypes, onlyLocally);
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
