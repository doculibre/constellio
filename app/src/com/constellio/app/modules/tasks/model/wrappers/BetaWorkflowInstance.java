package com.constellio.app.modules.tasks.model.wrappers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.structures.MapStringListStringStructure;

public class BetaWorkflowInstance extends RecordWrapper {

	public static final String SCHEMA_TYPE = "workflowInstance";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";

	public static final String WORKFLOW = "workflow";
	public static final String STARTED_BY = "startedBy";
	public static final String STARTED_ON = "startedOn";
	public static final String STATUS = "status";
	public static final String EXTRA_FIELDS = "extraFields";

	public BetaWorkflowInstance(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	public Map<String, List<String>> getExtraFields() {
		MapStringListStringStructure structure = get(EXTRA_FIELDS);
		if (structure == null) {
			return Collections.emptyMap();
		} else {
			return structure;
		}
	}

	public BetaWorkflowInstance setExtraFields(Map<String, List<String>> extraFields) {
		set(EXTRA_FIELDS, extraFields == null ? null : new MapStringListStringStructure(extraFields));
		return this;
	}

	public BetaWorkflowInstance setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getWorkflow() {
		return get(WORKFLOW);
	}

	public BetaWorkflowInstance setWorkflow(String workflowId) {
		set(WORKFLOW, workflowId);
		return this;
	}

	public BetaWorkflowInstance setWorkflow(Record workflow) {
		set(WORKFLOW, workflow);
		return this;
	}

	public BetaWorkflowInstance setWorkflow(BetaWorkflow workflow) {
		set(WORKFLOW, workflow);
		return this;
	}

	public WorkflowInstanceStatus getWorkflowStatus() {
		return get(STATUS);
	}

	public BetaWorkflowInstance setWorkflowStatus(WorkflowInstanceStatus status) {
		set(STATUS, status);
		return this;
	}

	public LocalDateTime getStartedOn() {
		return get(STARTED_ON);
	}

	public BetaWorkflowInstance setStartedOn(LocalDateTime startedOn) {
		set(STARTED_ON, startedOn);
		return this;
	}

	public String getStartedBy() {
		return get(STARTED_BY);
	}

	public BetaWorkflowInstance setStartedBy(String startedBy) {
		set(STARTED_BY, startedBy);
		return this;
	}

	public BetaWorkflowInstance setStartedBy(Record startedBy) {
		set(STARTED_BY, startedBy);
		return this;
	}

	public BetaWorkflowInstance setStartedBy(User startedBy) {
		set(STARTED_BY, startedBy);
		return this;
	}
}
