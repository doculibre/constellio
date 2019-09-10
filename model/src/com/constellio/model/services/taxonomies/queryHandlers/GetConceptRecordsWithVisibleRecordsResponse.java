package com.constellio.model.services.taxonomies.queryHandlers;

import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

import java.util.ArrayList;
import java.util.List;

public class GetConceptRecordsWithVisibleRecordsResponse {
	private List<TaxonomySearchRecord> records = new ArrayList<>();
	private boolean finishedIteratingOverRecords;
	private int continueAtPosition;

	public GetConceptRecordsWithVisibleRecordsResponse setRecords(
			List<TaxonomySearchRecord> records) {
		this.records = records;
		return this;
	}

	public GetConceptRecordsWithVisibleRecordsResponse setFinishedIteratingOverRecords(
			boolean finishedIteratingOverRecords) {
		this.finishedIteratingOverRecords = finishedIteratingOverRecords;
		return this;
	}

	public GetConceptRecordsWithVisibleRecordsResponse setContinueAtPosition(int continueAtPosition) {
		this.continueAtPosition = continueAtPosition;
		return this;
	}

	public List<TaxonomySearchRecord> getRecords() {
		return records;
	}

	public boolean isFinishedIteratingOverRecords() {
		return finishedIteratingOverRecords;
	}

	public int getContinueAtPosition() {
		return continueAtPosition;
	}
}
