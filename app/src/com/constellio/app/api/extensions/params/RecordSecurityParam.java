package com.constellio.app.api.extensions.params;

import com.constellio.model.entities.records.Record;

public class RecordSecurityParam {
	private Record record;

	public RecordSecurityParam(Record record) {
		this.record = record;
	}

	public Record getRecord() {
		return record;
	}
}
