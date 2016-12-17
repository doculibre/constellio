package com.constellio.app.extensions.sequence;

import com.constellio.model.entities.records.Record;

public class AvailableSequenceForRecordParams {

	Record record;

	public AvailableSequenceForRecordParams(Record record) {
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isSchemaType(String schemaType) {
		return record.getSchemaCode().startsWith(schemaType + "_");
	}
}
