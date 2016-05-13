package com.constellio.app.ui.pages.search.batchProcessing.entities;

import java.util.Collections;
import java.util.List;

public class BatchProcessResults {

	List<BatchProcessRecordModifications> recordModifications;

	public BatchProcessResults(
			List<BatchProcessRecordModifications> recordModifications) {
		this.recordModifications = Collections.unmodifiableList(recordModifications);
	}

	public List<BatchProcessRecordModifications> getRecordModifications() {
		return recordModifications;
	}

	public BatchProcessRecordModifications getRecordModifications(String id) {
		for (BatchProcessRecordModifications recordModification : recordModifications) {
			if (recordModification.getRecordId().equals(id)) {
				return recordModification;
			}
		}
		return null;
	}
}
