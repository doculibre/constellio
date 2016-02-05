package com.constellio.model.entities.workflows.definitions;

import java.util.List;

public abstract class WorkflowTask {

	List<WorkflowRoutingDestination> routings;

	String taskId;

	public WorkflowTask(String destinationTaskId, List<WorkflowRoutingDestination> routings) {
		this.routings = routings;
		this.taskId = destinationTaskId;
	}

	public List<WorkflowRoutingDestination> getRoutings() {
		return routings;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getSingleDestination() {
		if (routings.size() == 1) {
			return routings.get(0).getDestinationTask();
		} else {
			throw new RuntimeException("This task has multiple routings. It is probably a gateway.");
		}
	}
}
