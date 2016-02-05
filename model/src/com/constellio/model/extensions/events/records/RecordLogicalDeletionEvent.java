package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordLogicalDeletionEvent implements RecordEvent {

	Record record;

	public RecordLogicalDeletionEvent(Record record) {
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

}
