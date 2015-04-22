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
