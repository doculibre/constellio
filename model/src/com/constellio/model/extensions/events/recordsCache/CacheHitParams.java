package com.constellio.model.extensions.events.recordsCache;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public class CacheHitParams {

	Metadata metadata;

	Record record;

	long duration;

	String value;

	public CacheHitParams(Metadata metadata, String value, Record record, long duration) {
		this.metadata = metadata;
		this.record = record;
		this.duration = duration;
		this.value = value;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public String getValue() {
		return value;
	}

	public Record getRecord() {
		return record;
	}

	public long getDuration() {
		return duration;
	}
}
