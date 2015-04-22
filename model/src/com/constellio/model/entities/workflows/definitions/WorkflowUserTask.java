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
