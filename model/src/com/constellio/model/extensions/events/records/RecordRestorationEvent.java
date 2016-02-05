package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;

public class RecordRestorationEvent implements RecordEvent {

	Record record;

	public RecordRestorationEvent(Record record) {
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}

}
