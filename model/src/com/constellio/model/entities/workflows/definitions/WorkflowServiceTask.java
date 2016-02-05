package com.constellio.model.entities.workflows.definitions;

import java.util.List;

public class WorkflowServiceTask extends WorkflowTask {

	WorkflowAction action;

	public WorkflowServiceTask(String destinationTaskId, WorkflowAction action, List<WorkflowRoutingDestination> routings) {
		super(destinationTaskId, routings);
		this.action = action;
	}

	public WorkflowAction getAction() {
		return action;
	}
}
