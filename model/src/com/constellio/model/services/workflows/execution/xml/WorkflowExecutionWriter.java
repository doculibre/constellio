package com.constellio.model.services.workflows.execution.xml;

import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import com.constellio.model.entities.workflows.execution.WorkflowExecutedTask;
import com.constellio.model.entities.workflows.execution.WorkflowExecution;
import com.constellio.model.entities.workflows.trigger.Trigger;

public class WorkflowExecutionWriter {

	private static final String STARTED_BY = "startedBy";
	private static final String STARTED_ON = "startedOn";
	private static final String WORKFLOW_DEFINITION_ID = "workflowDefinitionId";
	private static final String CURRENT_TASK_ID = "currentTaskId";
	private static final String WORKFLOW_EXCETUTION = "workflowExcetution";
	private static final String ACTION_COMPLETION = "actionCompletion";
	private static final String TRIGGER_METADATA_CODE = "triggerMetadataCode";
	private static final String TRIGGER_SCHEMA_CODE = "triggerSchemaCode";
	private static final String TRIGGER_TYPE = "triggerType";
	private static final String TRIGGER = "trigger";
	Document document;

	public WorkflowExecutionWriter(Document document) {
		this.document = document;
	}

	public void add(WorkflowExecution workflowExecution) {

		Element workflowExecutionElement = new Element(WORKFLOW_EXCETUTION);
		document.setRootElement(workflowExecutionElement);

		Element workflowDefinitionIdElement = new Element(WORKFLOW_DEFINITION_ID);
		workflowDefinitionIdElement.setText(workflowExecution.getWorkflowDefinitionId());
		workflowExecutionElement.addContent(workflowDefinitionIdElement);

		Element startedOnElement = new Element(STARTED_ON);
		startedOnElement.setText(workflowExecution.getStartedOn().toString());
		workflowExecutionElement.addContent(startedOnElement);

		Element startedByElement = new Element(STARTED_BY);
		startedByElement.setText(workflowExecution.getStartedBy());
		workflowExecutionElement.addContent(startedByElement);

		Element triggerElement = createTriggerElement(workflowExecution.getTrigger());
		workflowExecutionElement.addContent(triggerElement);

		Element recordIdsElement = new Element("recordIds");
		for(String recordId: workflowExecution.getRecordIds()) {
			Element recordIdElement = new Element("recordId");
			recordIdElement.setText(recordId);
			recordIdsElement.addContent(recordIdElement);
		}
		workflowExecutionElement.addContent(recordIdsElement);

		Element currentTaskIdElement = new Element(CURRENT_TASK_ID);
		currentTaskIdElement.setText(workflowExecution.getCurrentTaskId());
		workflowExecutionElement.addContent(currentTaskIdElement);

		Element currentTaskStartedOnElement = new Element("currentTaskStartedOn");
		currentTaskStartedOnElement.setText(workflowExecution.getCurrentTaskStartedOn().toString());
		workflowExecutionElement.addContent(currentTaskStartedOnElement);

		Element variablesElements = new Element("variables");
		for (Map.Entry<String, String> variables : workflowExecution.getVariables().entrySet()) {
			Element variableElement = new Element("variable");
			Element variableKeyElement = new Element("variableKey");
			variableKeyElement.setText(variables.getKey());
			Element variableValueElement = new Element("variableValue");
			variableValueElement.setText(variables.getValue());
			variableElement.addContent(variableKeyElement);
			variableElement.addContent(variableValueElement);
			variablesElements.addContent(variableElement);
		}
		workflowExecutionElement.addContent(variablesElements);

		Element workflowExecutedTaskElements = new Element("workflowExecutedTask");
		for (WorkflowExecutedTask workflowExecutedTask : workflowExecution.getExecutedTasks()) {

			Element workflowExecutedTaskElement = new Element("workflowExecutedTask");

			Element taskIdElement = new Element("taskId");
			taskIdElement.setText(workflowExecutedTask.getTaskId());
			workflowExecutedTaskElement.addContent(taskIdElement);

			Element taskStartedOnElement = new Element("startedOn");
			taskStartedOnElement.setText(workflowExecutedTask.getStartedOn().toString());
			workflowExecutedTaskElement.addContent(taskStartedOnElement);

			Element taskFinishedOnElement = new Element("finishedOn");
			taskFinishedOnElement.setText(workflowExecutedTask.getFinishedOn().toString());
			workflowExecutedTaskElement.addContent(taskFinishedOnElement);

			Element taskFinishedByElement = new Element("finishedBy");
			taskFinishedByElement.setText(workflowExecutedTask.getFinishedBy());
			workflowExecutedTaskElement.addContent(taskFinishedByElement);

			workflowExecutedTaskElements.addContent(workflowExecutedTaskElement);
		}
		workflowExecutionElement.addContent(workflowExecutedTaskElements);

		Element markedAsWaitingForSystemElement = new Element("markedAsWaitingForSystem");
		boolean markedAsWaitingForSystem = workflowExecution.isMarkAsWaitingForSystem();
		markedAsWaitingForSystemElement.setText(String.valueOf(markedAsWaitingForSystem));
		workflowExecutionElement.addContent(markedAsWaitingForSystemElement);
	}

	private Element createTriggerElement(Trigger trigger) {
		Element triggerTypeElement = new Element(TRIGGER_TYPE);
		triggerTypeElement.setText(trigger.getTriggerType().name());
		Element actionCompletionElement = new Element(ACTION_COMPLETION);
		actionCompletionElement.setText(trigger.getActionCompletion().name());
		Element triggerSchemaCodeElement = new Element(TRIGGER_SCHEMA_CODE);
		triggerSchemaCodeElement.setText(trigger.getTriggeredSchemaCode());
		Element triggerMetadataCodeElement = new Element(TRIGGER_METADATA_CODE);
		triggerMetadataCodeElement.setText(trigger.getTriggeredMetadataCode());
		Element triggerElement = new Element(TRIGGER);
		triggerElement.addContent(triggerTypeElement);
		triggerElement.addContent(triggerSchemaCodeElement);
		triggerElement.addContent(triggerMetadataCodeElement);
		triggerElement.addContent(actionCompletionElement);
		return triggerElement;
	}
}
