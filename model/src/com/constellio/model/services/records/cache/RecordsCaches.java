package com.constellio.model.services.records.cache;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface RecordsCaches {

	RecordsCache getCache(String collection);

	CacheInsertionStatus insert(Record record, InsertionReason insertionReason);

	Record getRecordSummary(String id);

	Record getRecord(String id);

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

	default List<CacheInsertionStatus> insert(String collection, List<Record> records,
											  InsertionReason insertionReason) {
		List<CacheInsertionStatus> statuses = new ArrayList<>(records.size());

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
}
