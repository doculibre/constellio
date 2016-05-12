package com.constellio.app.ui.pages.search.batchProcessing.entities;

import java.util.Collections;
import java.util.List;

public class BatchProcessRecordModifications {

	private final String recordId;

	private final String recordTitle;

	private final List<BatchProcessRecordFieldModification> fieldsModifications;

	private final List<BatchProcessPossibleImpact> impacts;

	public BatchProcessRecordModifications(
			String recordId,
			String recordTitle,
			List<BatchProcessPossibleImpact> impacts,
			List<BatchProcessRecordFieldModification> fieldsModifications) {
		this.recordId = recordId;
		this.recordTitle = recordTitle;
		this.impacts = Collections.unmodifiableList(impacts);
		this.fieldsModifications = Collections.unmodifiableList(fieldsModifications);
	}

	public String getRecordId() {
		return recordId;
	}

	public String getRecordTitle() {
		return recordTitle;
	}

	public List<BatchProcessRecordFieldModification> getFieldsModifications() {
		return fieldsModifications;
	}

	public List<BatchProcessPossibleImpact> getImpacts() {
		return impacts;
	}
}
