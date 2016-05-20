package com.constellio.app.ui.pages.search.batchProcessing.entities;

import com.constellio.model.entities.schemas.Metadata;

public class BatchProcessRecordFieldModification {

	private final String valueBefore;
	private final String valueAfter;
	private final Metadata metadata;

	public BatchProcessRecordFieldModification(String valueBefore, String valueAfter,
			Metadata metadata) {
		this.valueBefore = valueBefore;
		this.valueAfter = valueAfter;
		this.metadata = metadata;
	}

	public String getValueBefore() {
		return valueBefore;
	}

	public String getValueAfter() {
		return valueAfter;
	}

	public Metadata getMetadata() {
		return metadata;
	}
}
