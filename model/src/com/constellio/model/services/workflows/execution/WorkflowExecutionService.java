package com.constellio.model.services.workflows.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.workflows.execution.WorkflowExecutedTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggeredWorkflowDefinition;

public class WorkflowExecutionService {

	private static final String START = "start";

	WorkflowExecutionIndexManager workflowExecutionIndexManager;

	ConfigManager configManager;
	DataLayerFactory dataLayerFactory;

	public WorkflowExecutionService(WorkflowExecutionIndexManager workflowExecutionIndexManager,
			DataLayerFactory dataLayerFactory) {
		this.workflowExecutionIndexManager = workflowExecutionIndexManager;
		this.dataLayerFactory = dataLayerFactory;
		this.configManager = dataLayerFactory.getConfigManager();
	}

	public WorkflowExecution startWorkflow(TriggeredWorkflowDefinition triggeredWorkflowDefinition,
			List<Record> records, User user) {

		String collection = triggeredWorkflowDefinition.getWorkflowDefinition().getCollection();
		String id = dataLayerFactory.getUniqueIdGenerator().next();
		String workflowDefinitionId = triggeredWorkflowDefinition.getWorkflowDefinition().getConfigId();
		String currentTaskId = START;
		Trigger trigger = triggeredWorkflowDefinition.getTrigger();
		Map<String, String> variables = triggeredWorkflowDefinition.getWorkflowDefinition().getVariables();
		List<WorkflowExecutedTask> executedTasks = new ArrayList<>();
		List<String> recordIds = new ArrayList<>();
		for (Record record : records) {
			recordIds.add(record.getId());
		}
		WorkflowExecution workflowExecution = new WorkflowExecution(id, workflowDefinitionId, new LocalDateTime(),
				user.getUsername(), trigger, recordIds, currentTaskId, new LocalDateTime(), variables, executedTasks,
				collection);

		addUpdateWorkflowExecution(workflowExecution);

		markAsWaitingForSystem(collection, id);
		workflowExecution = getWorkflow(collection, id);

		return workflowExecution;
	}

	public void addUpdateWorkflowExecution(WorkflowExecution workflowExecution) {
		workflowExecutionIndexManager.addUpdate(workflowExecution);
	}

	public void remove(WorkflowExecution workflowExecution) {
		workflowExecutionIndexManager.remove(workflowExecution);
	}

	public void markAsWaitingForSystem(String collection, String id) {
		workflowExecutionIndexManager.markAsWaitingForSystem(collection, id);
	}

	public void markAsNotWaitingForSystem(String collection, String id) {
		workflowExecutionIndexManager.markAsNotWaitingForSystem(collection, id);
	}

	public WorkflowExecution getWorkflow(String collection, String id) {
		return workflowExecutionIndexManager.getWorkflow(collection, id);
	}

	public List<WorkflowExecution> getNextWorkflowWaitingForSystemProcessing(String collection) {
		return workflowExecutionIndexManager.getNextWorkflowIdsWaitingForSystemProcessing(collection);
	}
}
