package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.List;
import java.util.stream.Collectors;

public class RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent implements RecordEvent {

	List<Metadata> modifiedMetadatas;

	Record record;

	ValidationErrors validationErrors;

	User transactionUser;

	boolean isOnlyBeingPrepared;

	public RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent(Record record,
																				  List<Metadata> modifiedMetadatas,
																				  ValidationErrors validationErrors,
																				  User transactionUser,
																				  boolean isOnlyBeingPrepared) {
		this.record = record;
		this.modifiedMetadatas = modifiedMetadatas;
		this.validationErrors = validationErrors;
		this.isOnlyBeingPrepared = isOnlyBeingPrepared;
		this.transactionUser = transactionUser;
	}

	public User getTransactionUser() {
		return transactionUser;
	}

	public Record getRecord() {
		return record;
	}

	public List<Metadata> getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public boolean isOnlyBeingPrepared() {
		return isOnlyBeingPrepared;
	}

	public void setOnlyBeingPrepared(boolean onlyBeingPrepared) {
		isOnlyBeingPrepared = onlyBeingPrepared;
	}

	public boolean hasModifiedMetadata(String metadataLocalCode) {
		for (String code : modifiedMetadatas.stream().map(Metadata::getCode).collect(Collectors.toList())) {
			if (code.endsWith(metadataLocalCode)) {
				return true;
			}
		}
		return false;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public boolean isSchemaType(String schemaType) {
		return schemaType.equals(getSchemaTypeCode());
	}

	public String getSchemaTypeCode() {
		return new SchemaUtils().getSchemaTypeCode(record.getSchemaCode());
	}

	public static class UnModifiedMetadataRuntimeException extends RuntimeException {
		public UnModifiedMetadataRuntimeException(String metadataLocalCode) {
			super("Metadata '" + metadataLocalCode + "' was not modified");
		}
	}
}
