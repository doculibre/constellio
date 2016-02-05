package com.constellio.model.services.tasks;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.Map;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.ApprovalTask;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.workflows.execution.WorkflowExecutionService;

public class TaskServices {

	public static final String TASK_DONE = "currentUserTaskDone";
	public static final String DONE = "done";
	RecordServices recordServices;

	WorkflowExecutionService workflowExecutionService;

	SearchServices searchServices;

	MetadataSchemasManager metadataSchemasManager;

	public TaskServices(RecordServices recordServices, SearchServices searchServices,
			WorkflowExecutionService workflowExecutionService, MetadataSchemasManager metadataSchemasManager) {
		this.recordServices = recordServices;
		this.searchServices = searchServices;
		this.workflowExecutionService = workflowExecutionService;
		this.metadataSchemasManager = metadataSchemasManager;
	}

	public WorkflowTask getCurrentWorkflowManualTask(WorkflowExecution execution) {

		String collection = execution.getCollection();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchemaType metadataSchemaType = metadataSchemaTypes.getSchemaType(WorkflowTask.SCHEMA_TYPE);
		Metadata workflowIdMetadata = metadataSchemaType.getDefaultSchema().getMetadata(WorkflowTask.WORKFLOW_ID);
		LogicalSearchCondition condition = newCondition(execution, metadataSchemaType, workflowIdMetadata);
		Record record = searchServices.searchSingleResult(condition);
		if (record == null) {
			return null;
		}
		return newRelativeTask(record, metadataSchemaTypes);
	}

	public WorkflowTask newRelativeTask(Record record, MetadataSchemaTypes metadataSchemaTypes) {
		if (record.getSchemaCode().equals(ApprovalTask.SCHEMA_CODE)) {
			return newAprovalTask(record, metadataSchemaTypes);
		} else {
			return newTask(record, metadataSchemaTypes);
		}
	}

	public void finish(WorkflowTask task, User user) {
		task.set(WorkflowTask.FINISHED_BY, user.getId());
		task.set(WorkflowTask.FINISHED_ON, TimeProvider.getLocalDateTime());
		try {
			recordServices.update(task);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		String workflowId = task.getWorkflowId();
		if (workflowId != null) {
			WorkflowExecution execution = workflowExecutionService.getWorkflow(task.getSchema().getCollection(), workflowId);
			for (Map.Entry<String, String> variable : execution.getVariables().entrySet()) {
				if (task.getSchema().hasMetadataWithCode(variable.getKey())) {
					execution.setVariable(variable.getKey(), (String) task.get(variable.getKey()));
				}
			}
			execution.setVariable(TASK_DONE, DONE);
			workflowExecutionService.addUpdateWorkflowExecution(execution);
			workflowExecutionService.markAsWaitingForSystem(task.getSchema().getCollection(), workflowId);
		}
	}

	LogicalSearchCondition newCondition(WorkflowExecution execution, MetadataSchemaType metadataSchemaType,
			Metadata workflowIdMetadata) {
		LogicalSearchCondition condition = from(metadataSchemaType).where(workflowIdMetadata).isEqualTo(execution.getId());
		return condition;
	}

	WorkflowTask newTask(Record record, MetadataSchemaTypes metadataSchemaTypes) {
		return new WorkflowTask(record, metadataSchemaTypes);
	}

	ApprovalTask newAprovalTask(Record record, MetadataSchemaTypes metadataSchemaTypes) {
		return new ApprovalTask(record, metadataSchemaTypes);
	}
}
