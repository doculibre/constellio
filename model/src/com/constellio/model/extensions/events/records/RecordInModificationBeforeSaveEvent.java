package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

import java.util.List;

public abstract class RecordInModificationBeforeSaveEvent extends BaseConsumableEventImpl implements RecordEvent {

	MetadataList modifiedMetadatas;

	Record record;

	private User transactionUser;

	boolean singleRecordTransaction;

	ValidationErrors validationErrors;

	boolean skipValidationsIfNotEssential;

	public RecordInModificationBeforeSaveEvent(Record record, MetadataList modifiedMetadatas, User transactionUser,
											   boolean singleRecordTransaction, ValidationErrors validationErrors,
											   boolean skipValidationsIfNotEssential) {
		this.record = record;
		this.modifiedMetadatas = modifiedMetadatas;
		this.transactionUser = transactionUser;
		this.singleRecordTransaction = singleRecordTransaction;
		this.validationErrors = validationErrors;
		this.skipValidationsIfNotEssential = skipValidationsIfNotEssential;
	}

	public Record getRecord() {
		return record;
	}

	public MetadataList getModifiedMetadatas() {
		return modifiedMetadatas;
	}

	public boolean isSingleRecordTransaction() {
		return singleRecordTransaction;
	}

	public ValidationErrors getValidationErrors() {
		return validationErrors;
	}

	public <T> T getPreviousValue(String metadataLocalCode) {
		Metadata metadata = modifiedMetadatas.getMetadataWithLocalCode(metadataLocalCode);
		if (metadata == null) {
			throw new UnModifiedMetadataRuntimeException(metadataLocalCode);
		}

		RecordImpl recordImpl = (RecordImpl) record;
		return recordImpl.getCopyOfOriginalRecord().get(metadata);
	}

	public boolean hasModifiedMetadata(String metadataLocalCode) {
		for (String code : modifiedMetadatas.toMetadatasCodesList()) {
			if (code.endsWith(metadataLocalCode)) {
				return true;
			}
		}
		return false;
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

	public static class UnModifiedMetadataRuntimeException extends RuntimeException {
		public UnModifiedMetadataRuntimeException(String metadataLocalCode) {
			super("Metadata '" + metadataLocalCode + "' was not modified");
		}
	}

	public abstract void recalculateRecord(List<String> metadatas);
}
