package com.constellio.model.entities.records.wrappers;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class WorkflowTask extends RecordWrapper {

	public static final String SCHEMA_TYPE = "task";

	public static final String ASSIGNED_TO = "assignedTo";
	public static final String ASSIGNED_ON = "assignedOn";
	public static final String ASSIGN_CANDIDATES = "assignCandidates";
	public static final String FINISHED_BY = "finishedBy";
	public static final String FINISHED_ON = "finishedOn";
	public static final String WORKFLOW_ID = "workflowIdentifier";
	public static final String WORKFLOW_RECORD_IDS = "workflowRecordIdentifiers";
	public static final String DUE_DATE = "dueDate";

	public WorkflowTask(Record record, MetadataSchemaTypes types) {
		super(record, types, "task_");
	}

	public WorkflowTask(Record record, MetadataSchemaTypes types, String typeRequirement) {
		super(record, types, typeRequirement);
	}

	public String getAssignedTo() {
		return get(ASSIGNED_TO);
	}

	public void setAssignedTo(String userId) {
		set(ASSIGNED_TO, userId);
	}

	public LocalDateTime getAssignedOn() {
		return get(ASSIGNED_ON);
	}

	public void setAssignedOn(LocalDateTime assignedOn) {
		set(ASSIGNED_ON, assignedOn);
	}

	public List<String> getAssignCandidates() {
		return get(ASSIGN_CANDIDATES);
	}

	public void setAssignCandidates(List<String> candidates) {
		set(ASSIGN_CANDIDATES, candidates);
	}

	public String getFinishedBy() {
		return get(FINISHED_BY);
	}

	public void setFinishedBy(String finishedBy) {
		set(FINISHED_BY, finishedBy);
	}

	public LocalDateTime getFinishedOn() {
		return get(FINISHED_ON);
	}

	public void setFinishedOn(LocalDateTime finishedOn) {
		set(FINISHED_ON, finishedOn);
	}

	public String getWorkflowId() {
		return get(WORKFLOW_ID);
	}

	public void setWorkflowId(String workflowId) {
		set(WORKFLOW_ID, workflowId);
	}

	public List<String> getWorkflowRecordIds() {
		return get(WORKFLOW_RECORD_IDS);
	}

	public void setWorkflowRecordIds(List<String> workflowRecordIds) {
		set(WORKFLOW_RECORD_IDS, workflowRecordIds);
	}

	public LocalDateTime getDueDate() {
		return get(DUE_DATE);
	}

	public void setDueDate(LocalDateTime dueDate) {
		set(DUE_DATE, dueDate);
	}
}
