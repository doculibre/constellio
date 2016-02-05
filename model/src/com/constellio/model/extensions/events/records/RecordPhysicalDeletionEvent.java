package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;

public class RecordPhysicalDeletionEvent implements RecordEvent {

	Record record;

	public RecordPhysicalDeletionEvent(Record record) {
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}

}
