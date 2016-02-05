package com.constellio.app.extensions.records.params;

import com.constellio.model.entities.records.Record;

public class GetIconPathParams {
	private final Record record;
	private final boolean expanded;

	public GetIconPathParams(Record record, boolean expanded) {
		this.record = record;
		this.expanded = expanded;
	}

	public Record getRecord() {
		return record;
	}

	public boolean isExpanded() {
		return expanded;
	}
}
