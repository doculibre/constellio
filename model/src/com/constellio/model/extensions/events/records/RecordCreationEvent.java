package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordCreationEvent implements RecordEvent {

	Record record;

	MetadataSchema schema;

	public RecordCreationEvent(Record record, MetadataSchema schema) {
		this.record = record;
		this.schema = schema;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}

	public MetadataSchema getSchema() {
		return schema;
	}
}
