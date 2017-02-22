package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class TransactionExecutionBeforeSaveEvent {

	Transaction transaction;

	ValidationErrors validationErrors;

	public TransactionExecutionBeforeSaveEvent(Transaction transaction, ValidationErrors validationErrors) {
		this.transaction = transaction;
		this.validationErrors = validationErrors;
	}

	public boolean isOnlySchemaType(String schemaType) {
		for (Record record : transaction.getRecords()) {
			if (!record.getTypeCode().equals(schemaType)) {
				return false;
			}
		}
		return !transaction.getRecords().isEmpty();
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}
}
