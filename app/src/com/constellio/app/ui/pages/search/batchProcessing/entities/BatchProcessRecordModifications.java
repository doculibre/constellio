package com.constellio.app.ui.pages.search.batchProcessing.entities;

import java.util.Collections;
import java.util.List;

public class BatchProcessRecordModifications {

	private final List<BatchProcessRecordFieldModification> fieldsModifications;

	private final List<BatchProcessPossibleImpact> impacts;

	public BatchProcessRecordModifications(
			List<BatchProcessPossibleImpact> impacts,
			List<BatchProcessRecordFieldModification> fieldsModifications) {
		this.impacts = Collections.unmodifiableList(impacts);
		this.fieldsModifications = Collections.unmodifiableList(fieldsModifications);
	}

	public List<BatchProcessRecordFieldModification> getFieldsModifications() {
		return fieldsModifications;
	}

	public List<BatchProcessPossibleImpact> getImpacts() {
		return impacts;
	}
}
