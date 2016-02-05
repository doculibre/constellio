package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ApprovalTask extends WorkflowTask {

	public static final String SCHEMA_LOCAL_CODE = "approval";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;

	public static final String DECISION = "decision";
	public static final String DECISION_APPROVED = "approved";
	public static final String DECISION_REFUSED = "refused";

	public ApprovalTask(Record record, MetadataSchemaTypes types) {
		super(record, types, "task_approval");
	}

	public void approve() {
		set(DECISION, DECISION_APPROVED);
	}

	public void refuse() {
		set(DECISION, DECISION_REFUSED);
	}

	public String getDecision() {
		return get(DECISION);
	}
}
