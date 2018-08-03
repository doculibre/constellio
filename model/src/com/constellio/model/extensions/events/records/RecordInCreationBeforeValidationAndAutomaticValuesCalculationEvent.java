package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent implements RecordEvent {

	Record record;
	private User transactionUser;
	ValidationErrors validationErrors;

	public RecordInCreationBeforeValidationAndAutomaticValuesCalculationEvent(Record record, User transactionUser,
																			  ValidationErrors validationErrors) {
		this.record = record;
		this.transactionUser = transactionUser;
		this.validationErrors = validationErrors;
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

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}
}
