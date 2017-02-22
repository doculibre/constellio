package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordCreationEvent implements RecordEvent {

	Record record;

	boolean singleRecordTransaction;

	ValidationErrors validationErrors;

	public RecordCreationEvent(Record record, boolean singleRecordTransaction, ValidationErrors errors) {
		this.record = record;
		this.singleRecordTransaction = singleRecordTransaction;
	}

	public boolean isSingleRecordTransaction() {
		return singleRecordTransaction;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
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
}
