package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordSetCategoryEvent implements RecordEvent {

	String category;

	Record record;

	public RecordSetCategoryEvent(Record record, String category) {
		this.record = record;
		this.category = category;
	}

	public Record getRecord() {
		return record;
	}

	public String getCategory() {
		return category;
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}
}
