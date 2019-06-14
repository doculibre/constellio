package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.Record;

import java.util.Map;

public class ExtraMetadataToGenerateOnReferenceParams {
	private Record record;
	private Map<String, String> valueByMetadata;

	public ExtraMetadataToGenerateOnReferenceParams(Record record, Map<String, String> valueByMetadata) {
		this.record = record;
		this.valueByMetadata = valueByMetadata;
	}

	public Record getRecord() {
		return record;
	}

	public Map<String, String> getValueByMetadata() {
		return valueByMetadata;
	}
}
