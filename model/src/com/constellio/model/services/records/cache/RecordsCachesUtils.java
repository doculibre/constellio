package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;

public class RecordsCachesUtils {

	public static CacheInsertionStatus evaluateCacheInsert(Record insertedRecord, CacheConfig cacheConfig) {

		if (insertedRecord.isDirty()) {
			return CacheInsertionStatus.REFUSED_DIRTY;
		}

		if (!insertedRecord.isSaved()) {
			return CacheInsertionStatus.REFUSED_UNSAVED;
		}

		if (!insertedRecord.isFullyLoaded()) {
			return CacheInsertionStatus.REFUSED_NOT_FULLY_LOADED;
		}

		return CacheInsertionStatus.ACCEPTED;
	}

	public static Record prepareRecordForCacheInsert(Record insertedRecord, CacheConfig cacheConfig) {

		if (cacheConfig == null) {
			return insertedRecord;
		} else if (cacheConfig.getPersistedMetadatas().isEmpty()) {
			return insertedRecord.getCopyOfOriginalRecord();
		} else {
			return insertedRecord.getCopyOfOriginalRecordKeepingOnly(cacheConfig.getPersistedMetadatas());
		}
	}

}
