package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordModificationEvent implements RecordEvent {

	MetadataList modifiedMetadatas;

	MetadataSchema schema;

	Record record;

	Record originalRecord;

	public RecordModificationEvent(Record record, MetadataList modifiedMetadatas, MetadataSchema schema) {
		this.record = record;
		this.schema = schema;
		this.modifiedMetadatas = modifiedMetadatas;
		this.originalRecord = record.getCopyOfOriginalRecord();
	}

	public <T> T getPreviousValue(String metadataLocalCode) {
		Metadata metadata = modifiedMetadatas.getMetadataWithLocalCode(metadataLocalCode);
		if (metadata == null) {
			throw new RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent.UnModifiedMetadataRuntimeException(metadataLocalCode);
		}

		return originalRecord.get(metadata);
	}

	public Record getRecord() {
		return record;
	}

	public Record getOriginalRecord() {
		return originalRecord;
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

	public MetadataSchema getSchema() {
		return schema;
	}
}
