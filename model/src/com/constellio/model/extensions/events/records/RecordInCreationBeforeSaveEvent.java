package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordInCreationBeforeSaveEvent implements RecordEvent {

	Record record;
	private User transactionUser;

	boolean singleRecordTransaction;

	ValidationErrors validationErrors;

	public RecordInCreationBeforeSaveEvent(Record record, User transactionUser, boolean singleRecordTransaction,
			ValidationErrors validationErrors) {
		this.record = record;
		this.transactionUser = transactionUser;
		this.singleRecordTransaction = singleRecordTransaction;
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

	public boolean isSingleRecordTransaction() {
		return singleRecordTransaction;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public boolean isNewRecordImport() {
		Record record = getRecord();
		return !record.isSaved() && record.get(Schemas.LEGACY_ID) != null;
	}
}
