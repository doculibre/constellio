package com.constellio.model.entities.workflows.definitions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WorkflowDefinition {

	private final String collection;

	private final String configId;

	private final boolean enabled;

	private final Map<String, WorkflowTask> tasks;
	private final Map<String, WorkflowRouting> routings;

	public WorkflowDefinition(String configId, Map<String, WorkflowTask> tasks, boolean enabled,
			Map<String, WorkflowRouting> routings, String collection) {
		this.configId = configId;
		this.tasks = tasks;
		this.enabled = enabled;
		this.routings = routings;
		this.collection = collection;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Collection<WorkflowTask> getTasks() {
		return tasks.values();
	}

	public WorkflowTask getTask(String id) {
		return tasks.get(id);
	}

	public WorkflowRouting getRouting(String id) {
		return routings.get(id);
	}

	public Map<String, WorkflowRouting> getRoutings() {
		return routings;
	}

	public String getConfigId() {
		return configId;
	}

	public String getCollection() {
		return collection;
	}

	// PROTOTYPE
	public Map<String, String> getVariables() {
		Map<String, String> variables = new HashMap<>();
		for (WorkflowTask task : tasks.values()) {
			if (task instanceof WorkflowUserTask) {
				for (BPMNProperty property : ((WorkflowUserTask) task).getFields()) {
					if (property.getVariableCode() != null) {
						variables.put(property.getVariableCode(), property.getExpressionValue());
					}
				}
			}
		}
		return variables;
	}
}
