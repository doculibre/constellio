/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.workflows;

import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.WorkflowTask;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.workflows.definitions.AllUsersSelector;
import com.constellio.model.entities.workflows.definitions.BPMNProperty;
import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;
import com.constellio.model.entities.workflows.definitions.WorkflowRouting;
import com.constellio.model.entities.workflows.definitions.WorkflowRoutingDestination;
import com.constellio.model.entities.workflows.definitions.WorkflowServiceTask;
import com.constellio.model.entities.workflows.definitions.WorkflowUserTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.tasks.TaskServices;
import com.constellio.model.services.workflows.WorkflowExecutorRuntimeException.WorkflowExecutorRuntimeException_InvalidTaskId;
import com.constellio.model.services.workflows.WorkflowExecutorRuntimeException.WorkflowExecutorRuntimeException_InvalidTransaction;
import com.constellio.model.services.workflows.config.WorkflowsConfigManager;
import com.constellio.model.services.workflows.execution.WorkflowExecutionService;

public class WorkflowExecutor {

	private final WorkflowExecutionService workflowExecutionService;
	private final WorkflowsConfigManager workflowsConfigManager;
	private final TaskServices taskServices;
	private final CollectionsListManager collectionsListManager;
	private final MetadataSchemasManager metadataSchemasManager;
	private final RecordServices recordServices;
	private final ModelLayerFactory modelLayerFactory;

	public WorkflowExecutor(ModelLayerFactory modelLayerFactory) {
		this.workflowExecutionService = modelLayerFactory.newWorkflowExecutionService();
		this.workflowsConfigManager = modelLayerFactory.getWorkflowsConfigManager();
		this.taskServices = modelLayerFactory.newTaskServices();
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.modelLayerFactory = modelLayerFactory;
	}

	public void handleWaitingWorkflows() {
		for (String collection : collectionsListManager.getCollections()) {
			for (WorkflowExecution workflow : workflowExecutionService.getNextWorkflowWaitingForSystemProcessing(collection)) {
				WorkflowDefinition workflowDefinition = workflowsConfigManager
						.getWorkflowDefinition(collection, workflow.getWorkflowDefinitionId());
				handleWaitingWorkflow(workflow, workflowDefinition);
			}
		}
	}

	public void handleWaitingWorkflow(WorkflowExecution workflowExecution, WorkflowDefinition workflowDefinition) {

		String currentTaskId = workflowExecution.getCurrentTaskId();
		com.constellio.model.entities.workflows.definitions.WorkflowTask task = workflowDefinition.getTask(currentTaskId);
		if (WorkflowRoutingDestination.DESTINATION_END.equals(currentTaskId)) {
			workflowExecutionService.remove(workflowExecution);

		} else if (task != null) {
			if (task instanceof WorkflowUserTask) {
				if (workflowExecution.getVariables().containsKey(TaskServices.TASK_DONE)) {
					workflowExecution.getVariables().remove(TaskServices.TASK_DONE);
					workflowExecutionService.addUpdateWorkflowExecution(workflowExecution);
					workflowExecution.markCurrentTaskAsDoneAndStartNextTask(null, TimeProvider.getLocalDateTime(),
							task.getSingleDestination());
					handleWaitingWorkflow(workflowExecution, workflowDefinition);
				} else {
					WorkflowUserTask userTask = (WorkflowUserTask) task;
					prepareUserTask(userTask, workflowExecution);
					workflowExecutionService.addUpdateWorkflowExecution(workflowExecution);
					workflowExecutionService
							.markAsNotWaitingForSystem(workflowExecution.getCollection(), workflowExecution.getId());
				}
			} else if (task instanceof WorkflowServiceTask) {
				WorkflowServiceTask serviceTask = (WorkflowServiceTask) task;
				serviceTask.getAction().execute(workflowExecution, modelLayerFactory);
				workflowExecution.markCurrentTaskAsDoneAndStartNextTask(null, TimeProvider.getLocalDateTime(),
						task.getSingleDestination());
				handleWaitingWorkflow(workflowExecution, workflowDefinition);
			}
		} else {
			WorkflowRouting routing = workflowDefinition.getRouting(currentTaskId);
			if (routing != null) {
				String destination = routing.getDestination(workflowExecution);
				workflowExecution.markCurrentTaskAsDoneAndStartNextTask(null, TimeProvider.getLocalDateTime(), destination);
				handleWaitingWorkflow(workflowExecution, workflowDefinition);
			} else {
				throw new WorkflowExecutorRuntimeException_InvalidTaskId(currentTaskId);
			}
		}
	}

	public void prepareUserTask(WorkflowUserTask userTask, WorkflowExecution workflowExecution) {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(workflowExecution.getCollection());
		MetadataSchema schema = types.getSchema(userTask.getTaskSchema());

		Record newTaskRecord = recordServices.newRecordWithSchema(schema);
		WorkflowTask task = taskServices.newRelativeTask(newTaskRecord, types);
		fillTaskMetadatas(task, userTask, schema, workflowExecution);

		Transaction transaction = new Transaction(newTaskRecord).setSkippingRequiredValuesValidation(true);
		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new WorkflowExecutorRuntimeException_InvalidTransaction(e);
		}

	}

	public void fillTaskMetadatas(WorkflowTask newTask, WorkflowUserTask userTask, MetadataSchema schema,
			WorkflowExecution workflowExecution) {

		setAssignCandidates(newTask, userTask.getUserSelector(), workflowExecution);
		setDueDate(newTask, userTask.getDueDateInDays());
		newTask.setWorkflowId(workflowExecution.getId());
		newTask.setWorkflowRecordIds(workflowExecution.getRecordIds());
		for (BPMNProperty property : userTask.getFields()) {
			setMetadataBasedOnBPMNFields(newTask, property, schema, workflowExecution);
		}

	}

	public void setAssignCandidates(WorkflowTask newTask, AllUsersSelector userSelector, WorkflowExecution execution) {
		List<String> userIds = new RecordUtils()
				.toWrappedRecordIdsList(userSelector.getCandidateUsers(execution, modelLayerFactory));
		newTask.setAssignCandidates(userIds);
	}

	public void setDueDate(WorkflowTask newTask, int dueDateInDays) {
		newTask.setDueDate(new LocalDateTime().plusDays(dueDateInDays));
	}

	public void setMetadataBasedOnBPMNFields(WorkflowTask newTask, BPMNProperty bpmnField, MetadataSchema schema,
			WorkflowExecution workflowExecution) {
		Metadata propertyMetadata = schema.getMetadata(bpmnField.getFieldId());
		if (bpmnField.getExpressionValue() != null) {
			newTask.set(propertyMetadata.getLocalCode(), bpmnField.getExpressionValue());
		} else if (bpmnField.getVariableCode() != null) {
			newTask.set(propertyMetadata.getLocalCode(), workflowExecution.getVariable(bpmnField.getVariableCode()));
		}
	}
}
