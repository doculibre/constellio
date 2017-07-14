package com.constellio.model.extensions.events.recordsCache;

import com.constellio.model.entities.records.Record;

public class CachePutParams {

	Record record;

	long duration;

	public CachePutParams(Record record, long duration) {
		this.record = record;
		this.duration = duration;
	}

	public Record getRecord() {
		return record;
	}

	public long getDuration() {
		return duration;
	}
}
