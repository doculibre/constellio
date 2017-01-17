package com.constellio.app.api.extensions.params;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

public class TryRepairAutomaticValueParams {

	Record record;
	Metadata metadata;
	List<String> currentValues;
	List<String> valuesToRemove;

	public TryRepairAutomaticValueParams(Record record, Metadata metadata, List<String> currentValues,
			List<String> valuesToRemove) {
		this.record = record;
		this.metadata = metadata;
		this.currentValues = currentValues;
		this.valuesToRemove = valuesToRemove;
	}

	public Record getRecord() {
		return record;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public List<String> getCurrentValues() {
		return currentValues;
	}

	public List<String> getValuesToRemove() {
		return valuesToRemove;
	}

	public boolean isMetadata(String schemaType, String metadataLocalCode) {
		return isSchemaType(schemaType) && metadata.getLocalCode().equals(metadataLocalCode);
	}

	public boolean isSchemaType(String schemaType) {
		return record.getTypeCode().equals(schemaType);
	}
}
