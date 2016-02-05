package com.constellio.model.entities.workflows.definitions;

import java.util.List;

public class WorkflowUserTask extends WorkflowTask {

	private final AllUsersSelector userSelector;
	private final List<BPMNProperty> fields;
	private final String taskSchema;
	private final int dueDateInDays;

	public WorkflowUserTask(String destinationTaskId, String taskSchema, List<WorkflowRoutingDestination> routings,
			AllUsersSelector userSelector, int dueDateInDays, List<BPMNProperty> fields) {
		super(destinationTaskId, routings);
		this.taskSchema = taskSchema;
		this.userSelector = userSelector;
		this.dueDateInDays = dueDateInDays;
		this.fields = fields;
	}

	public AllUsersSelector getUserSelector() {
		return userSelector;
	}

	public List<BPMNProperty> getFields() {
		return fields;
	}

	public int getDueDateInDays() {
		return dueDateInDays;
	}

	public String getTaskSchema() {
		return taskSchema;
	}
}
