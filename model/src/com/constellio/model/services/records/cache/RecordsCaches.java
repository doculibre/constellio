package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.cache.hooks.RecordsCachesHook;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface RecordsCaches {

	RecordsCache getCache(String collection);

	CacheInsertionResponse insert(Record record, InsertionReason insertionReason);

	default Record getRecordSummary(String id) {
		return getRecordSummary(id, null, null);
	}

	default Record getRecordSummary(String id, String optionnalCollection) {
		return getRecordSummary(id, optionnalCollection, null);
	}

	Record getRecordSummary(String id, String optionnalCollection, String optionnalSchemaType);

	default Record getRecord(String id) {
		return getRecord(id, null, null);
	}

	default Record getRecord(String id, String optionnalCollection) {
		return getRecord(id, optionnalCollection, null);
	}

	Record getRecord(String id, String optionnalCollection, String optionnalSchemaType);

	default void invalidateVolatile() {
		invalidateVolatile(MassiveCacheInvalidationReason.KEEP_INTEGRITY);
	}

	void invalidateVolatile(MassiveCacheInvalidationReason reason);

	//void reload(List<String> schemaTypes);

	default void removeRecordsOfCollection(String collection) {
		removeRecordsOfCollection(collection, false);
	}

	void removeRecordsOfCollection(String collection, boolean onlyLocally);

	default boolean isCached(String id) {
		return getRecord(id) != null;
	}

	default List<CacheInsertionResponse> insert(String collection, List<Record> records,
												InsertionReason insertionReason) {
		List<CacheInsertionResponse> statuses = new ArrayList<>(records.size());

		for (Record record : records) {
			statuses.add(insert(record, insertionReason));
		}

		return statuses;
	}

	Stream<Record> stream(MetadataSchemaType type);

	Stream<Record> stream(String collection);

	default Stream<Record> stream() {
		throw new UnsupportedOperationException("Unsupported");
	}

	boolean isInitialized();

	default void reloadAllSchemaTypes(String collection) {
		throw new UnsupportedOperationException("Unsupported");
	}

	default void register(RecordsCachesHook hook) {
		throw new UnsupportedOperationException("Unsupported");
	}

	default RecordsCachesHook getHook(MetadataSchemaType schemaType) {
		throw new UnsupportedOperationException("Unsupported");
	}
}
