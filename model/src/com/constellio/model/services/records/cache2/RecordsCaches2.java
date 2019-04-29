package com.constellio.model.services.records.cache2;

import com.constellio.data.dao.services.cache.InsertionReason;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.records.cache.CacheInsertionStatus;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.stream.Stream;

public class RecordsCaches2 {

	MemoryEfficientRecordsCachesDataStore dataStore = new MemoryEfficientRecordsCachesDataStore();


	void initialize() {

	}

	CacheInsertionStatus insert(Record record, InsertionReason insertionReason) {
		return null;
	}

	Record get(String id) {
		return null;
	}

	Stream<Record> streamQueryResults(LogicalSearchQuery query) {
		return null;
	}

	Stream<Record> streamQueryResults(LogicalSearchCondition condition) {
		return null;
	}

	Stream<Record> streamReferencing(String id) {
		return null;
	}

}
