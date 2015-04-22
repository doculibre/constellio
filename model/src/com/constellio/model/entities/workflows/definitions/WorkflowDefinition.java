/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
