package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordRestorationEvent implements RecordEvent {

	Record record;

	public RecordRestorationEvent(Record record) {
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}

}
