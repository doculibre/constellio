package com.constellio.model.services.workflows.execution.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.joda.time.LocalDateTime;

import com.constellio.model.entities.workflows.execution.WorkflowExecutedTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.entities.workflows.trigger.ActionCompletion;
import com.constellio.model.entities.workflows.trigger.Trigger;
import com.constellio.model.entities.workflows.trigger.TriggerType;

public class WorkflowExecutionReader {

	Document document;

	private static final String MARKED_AS_WAITING_FOR_SYSTEM = "markedAsWaitingForSystem";
	private static final String TASK_ID = "taskId";
	private static final String FINISHED_BY = "finishedBy";
	private static final String FINISHED_ON = "finishedOn";
	private static final String WORKFLOW_EXECUTED_TASK = "workflowExecutedTask";
	private static final String VARIABLE_VALUE = "variableValue";
	private static final String VARIABLE_KEY = "variableKey";
	private static final String VARIABLES = "variables";
	private static final String CURRENT_TASK_STARTED_ON = "currentTaskStartedOn";
	private static final String CURRENT_TASK_ID = "currentTaskId";
	private static final String RECORD_ID = "recordId";
	private static final String RECORD_IDS = "recordIds";
	private static final String TRIGGER = "trigger";
	private static final String TRIGGER_TYPE = "triggerType";
	private static final String STARTED_BY = "startedBy";
	private static final String STARTED_ON = "startedOn";
	private static final String ACTION_COMPLETION = "actionCompletion";
	private static final String TRIGGER_METADATA_CODE = "triggerMetadataCode";
	private static final String TRIGGER_SCHEMA_CODE = "triggerSchemaCode";
	private static final String WORKFLOW_DEFINITION_ID = "workflowDefinitionId";

	public WorkflowExecutionReader(Document document) {
		this.document = document;
	}

	public WorkflowExecution read(String collection, String id, Document document) {

		String workflowDefinitionId = document.getRootElement().getChildText(WORKFLOW_DEFINITION_ID);

		LocalDateTime startedOn = LocalDateTime.parse(document.getRootElement().getChildText(STARTED_ON));

		String startedBy = document.getRootElement().getChildText(STARTED_BY);

		String triggerTypeStr = document.getRootElement().getChild(TRIGGER).getChildText(TRIGGER_TYPE);
		TriggerType triggerType = TriggerType.valueOf(triggerTypeStr);
		String triggeredSchemaCode = document.getRootElement().getChild(TRIGGER).getChildText(TRIGGER_SCHEMA_CODE);
		String triggeredMetadataCode = document.getRootElement().getChild(TRIGGER).getChildText(TRIGGER_METADATA_CODE);
		String actionCompletionStr = document.getRootElement().getChild(TRIGGER).getChildText(ACTION_COMPLETION);
		ActionCompletion actionCompletion = ActionCompletion.valueOf(actionCompletionStr);
		if (triggeredMetadataCode.equals("")) {
			triggeredMetadataCode = null;
		}
		Trigger trigger = new Trigger(triggerType, triggeredSchemaCode, triggeredMetadataCode, actionCompletion);

		List<String> recordIds = new ArrayList<>();
		for (Element recordIdElement : document.getRootElement().getChild(RECORD_IDS).getChildren(RECORD_ID)) {
			recordIds.add(recordIdElement.getText());
		}

		String currentTaskId = document.getRootElement().getChildText(CURRENT_TASK_ID);

		LocalDateTime currentTaskStartedOn = LocalDateTime.parse(document.getRootElement().getChildText(CURRENT_TASK_STARTED_ON));

		Map<String, String> variables = new HashMap<>();
		for (Element variableElement : document.getRootElement().getChild(VARIABLES).getChildren()) {
			String variableKey = variableElement.getChildText(VARIABLE_KEY);
			String variableValue = variableElement.getChildText(VARIABLE_VALUE);
			variables.put(variableKey, variableValue);
		}

		List<WorkflowExecutedTask> executedTasks = new ArrayList<>();
		for (Element workflowExecutedTaskElement : document.getRootElement().getChild(WORKFLOW_EXECUTED_TASK).getChildren()) {

			String taskId = workflowExecutedTaskElement.getChildText(TASK_ID);
			LocalDateTime taskStartedOn = LocalDateTime.parse(workflowExecutedTaskElement.getChildText(STARTED_ON));
			LocalDateTime taskFinishedOn = LocalDateTime.parse(workflowExecutedTaskElement.getChildText(FINISHED_ON));
			String taskFinishedBy = workflowExecutedTaskElement.getChildText(FINISHED_BY);
			WorkflowExecutedTask executedTask = new WorkflowExecutedTask(taskId, taskStartedOn, taskFinishedOn, taskFinishedBy);
			executedTasks.add(executedTask);
		}

		boolean markedAsWaitingforSystem = Boolean.valueOf(document.getRootElement().getChildText(MARKED_AS_WAITING_FOR_SYSTEM));

		WorkflowExecution workflowExecution = new WorkflowExecution(id, workflowDefinitionId, startedOn, startedBy, trigger,
				recordIds, currentTaskId, currentTaskStartedOn, variables, executedTasks, collection);

		workflowExecution.setMarkAsWaitingForSystem(markedAsWaitingforSystem);
		return workflowExecution;
	}
}
