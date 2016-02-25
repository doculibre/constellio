package com.constellio.app.modules.tasks.extensions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstance;
import com.constellio.app.modules.tasks.model.wrappers.WorkflowInstanceStatus;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.WorkflowServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordModificationEvent;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordUtils;

public class WorkflowRecordExtension extends RecordExtension {
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowRecordExtension.class);

	RecordServices recordServices;
	TasksSchemasRecordsServices tasks;
	WorkflowServices workflowServices;

	public WorkflowRecordExtension(String collection, AppLayerFactory appLayerFactory) {
		this.tasks = new TasksSchemasRecordsServices(collection, appLayerFactory);
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.workflowServices = new WorkflowServices(collection, appLayerFactory);
	}

	@Override
	public void recordModified(RecordModificationEvent event) {
		if (event.isSchemaType(Task.SCHEMA_TYPE)) {
			Task task = tasks.wrapTask(event.getRecord());
			if (event.hasModifiedMetadata(tasks.userTask.status().getLocalCode())
					&& advanceWorkflow(task)
					&& task.getWorkflowInstance() != null) {
				LOGGER.info("Moving workflow to next step");
				try {
					taskStatusModified(task);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private boolean advanceWorkflow(Task task) {
		List<String> finishedOrClosedStatuses = getFinishedOrClosedStatuses();
		return finishedOrClosedStatuses.contains(task.getStatus()) && !task.isNextTaskCreated();
	}

	private void taskStatusModified(Task task)
			throws RecordServicesException {

		WorkflowInstance workflowInstance = tasks.getWorkflowInstance(task.getWorkflowInstance());
		String nextModelTaskId = task.getNextTask(task.getDecision());
		if (nextModelTaskId != null) {
			Transaction transaction = new Transaction();

			Task modelTask = tasks.getTask(nextModelTaskId);
			Task instanceTask = workflowServices.createInstanceTask(modelTask, workflowInstance);
			transaction.add(instanceTask);
			transaction.add(task.setNextTaskCreated(true));

			recordServices.execute(transaction);
		} else {
			recordServices.update(workflowInstance.setWorkflowStatus(WorkflowInstanceStatus.FINISHED));
		}
	}

	private List<String> getFinishedOrClosedStatuses() {
		return new RecordUtils().toWrappedRecordIdsList(tasks.getFinishedOrClosedStatuses());
	}
}
