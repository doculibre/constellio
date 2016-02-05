package com.constellio.model.entities.workflows.definitions;

public class WorkflowRoutingDestination {

	public static final String DESTINATION_END = "end";
	public static final String DESTINATION_START = "start";

	WorkflowCondition condition;

	String destinationTask;

	String source;

	public WorkflowRoutingDestination(WorkflowCondition condition, String destinationTask, String source) {
		this.condition = condition;
		this.destinationTask = destinationTask;
		this.source = source;
	}

	public WorkflowCondition getCondition() {
		return condition;
	}

	public String getDestinationTask() {
		return destinationTask;
	}

	public String getSource() {
		return source;
	}
}
