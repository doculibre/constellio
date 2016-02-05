package com.constellio.model.entities.workflows.definitions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.workflows.execution.WorkflowExecution;

public class WorkflowRouting {

	String id;

	List<WorkflowRoutingDestination> destinations;

	public WorkflowRouting(String id, List<WorkflowRoutingDestination> destinations) {
		this.id = id;
		this.destinations = destinations;
	}

	public WorkflowRouting(String id) {
		this.id = id;
		this.destinations = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public String getDestination(WorkflowExecution execution) {
		for (WorkflowRoutingDestination destination : destinations) {
			if (destination.getCondition().isTrue(execution)) {
				return destination.getDestinationTask();
			}
		}
		return "";
	}

	public void addDestination(WorkflowRoutingDestination destination) {
		destinations.add(destination);
	}
}
