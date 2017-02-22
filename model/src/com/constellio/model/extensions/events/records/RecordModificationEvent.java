package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordModificationEvent implements RecordEvent {

	MetadataList modifiedMetadatas;

	Record record;

	boolean singleRecordTransaction;

	ValidationErrors errors;

	public RecordModificationEvent(Record record, MetadataList modifiedMetadatas, boolean singleRecordTransaction,
			ValidationErrors errors) {
		this.record = record;
		this.modifiedMetadatas = modifiedMetadatas;
		this.singleRecordTransaction = singleRecordTransaction;
	}

	public boolean isSingleRecordTransaction() {
		return singleRecordTransaction;
	}

	public ValidationErrors getValidationErrors() {
		return errors;
	}

	public Record getRecord() {
		return record;
	}

	public MetadataList getModifiedMetadatas() {
		return modifiedMetadatas;
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

}
