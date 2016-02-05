package com.constellio.model.entities.workflows.trigger;

import com.constellio.model.entities.workflows.definitions.WorkflowDefinition;

public class TriggeredWorkflowDefinition {

	WorkflowDefinition workflowDefinition;

	Trigger trigger;

	public TriggeredWorkflowDefinition(WorkflowDefinition workflowDefinition, Trigger trigger) {
		this.workflowDefinition = workflowDefinition;
		this.trigger = trigger;
	}

	public WorkflowDefinition getWorkflowDefinition() {
		return workflowDefinition;
	}

	public Trigger getTrigger() {
		return trigger;
	}
}
