package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent implements RecordEvent {

	Record record;
	private User transactionUser;

	public RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent(Record record, User transactionUser) {
		this.record = record;
		this.transactionUser = transactionUser;
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

	public User getTransactionUser() {
		return transactionUser;
	}
}
