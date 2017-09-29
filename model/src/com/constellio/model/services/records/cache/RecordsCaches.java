package com.constellio.model.services.records.cache;

import java.util.List;

import com.constellio.model.entities.records.Record;

public interface RecordsCaches {

	RecordsCache getCache(String collection);

	boolean isCached(String id);

	void insert(String collection, List<Record> records);

	CacheInsertionStatus insert(Record record);

	CacheInsertionStatus forceInsert(Record record);

	Record getRecord(String id);

	void invalidateAll();

	void invalidate(String collection);

	int getCacheObjectsCount();

	void setEnabled(boolean enabled);

}
