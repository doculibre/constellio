package com.constellio.model.extensions.events.records;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.MetadataList;
import com.constellio.model.services.schemas.SchemaUtils;

public class RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent implements RecordEvent {

	MetadataList modifiedMetadatas;

	Record record;

	public RecordInModificationBeforeValidationAndAutomaticValuesCalculationEvent(Record record,
			MetadataList modifiedMetadatas) {
		this.record = record;
		this.modifiedMetadatas = modifiedMetadatas;
	}

	public Record getRecord() {
		return record;
	}

	public MetadataList getModifiedMetadatas() {
		return modifiedMetadatas;
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

	public static class UnModifiedMetadataRuntimeException extends RuntimeException {
		public UnModifiedMetadataRuntimeException(String metadataLocalCode) {
			super("Metadata '" + metadataLocalCode + "' was not modified");
		}
	}
}
