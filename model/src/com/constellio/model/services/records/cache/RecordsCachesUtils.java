package com.constellio.model.services.records.cache;

import com.constellio.model.entities.records.Record;

public class RecordsCachesUtils {

	public static CacheInsertionStatus evaluateCacheInsert(Record insertedRecord) {

		if (insertedRecord.isDirty()) {
			return CacheInsertionStatus.REFUSED_DIRTY;
		}

		if (!insertedRecord.isSaved()) {
			return CacheInsertionStatus.REFUSED_UNSAVED;
		}

		return CacheInsertionStatus.ACCEPTED;
	}

}
