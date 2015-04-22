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
package com.constellio.model.entities.workflows.execution;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.workflows.trigger.Trigger;

public class WorkflowExecution {

	String id;

	String workflowDefinitionId;

	LocalDateTime startedOn;

	String startedBy;

	Trigger trigger;

	List<String> recordIds;

	String currentTaskId;

	LocalDateTime currentTaskStartedOn;

	Map<String, String> variables;

	List<WorkflowExecutedTask> executedTasks;

	boolean markAsWaitingForSystem = false;

	String collection;

	public WorkflowExecution(String id, String workflowDefinitionId, LocalDateTime startedOn, String startedBy,
			Trigger trigger, List<String> recordIds, String currentTaskId, LocalDateTime currentTaskStartedOn,
			Map<String, String> variables, List<WorkflowExecutedTask> executedTasks, String collection) {
		this.id = id;
		this.workflowDefinitionId = workflowDefinitionId;
		this.startedOn = startedOn;
		this.startedBy = startedBy;
		this.trigger = trigger;
		this.recordIds = recordIds;
		this.currentTaskId = currentTaskId;
		this.currentTaskStartedOn = currentTaskStartedOn;
		this.variables = variables;
		this.executedTasks = executedTasks;
		this.collection = collection;
	}

	public void markCurrentTaskAsDoneAndStartNextTask(String finishedBy, LocalDateTime finishedOn, String nextTaskId) {

		this.executedTasks.add(new WorkflowExecutedTask(currentTaskId, currentTaskStartedOn, finishedOn, finishedBy));
		this.currentTaskId = nextTaskId;
		this.currentTaskStartedOn = finishedOn;
	}

	public void markCurrentTaskAsDoneAndWorkflowFinished(String finishedBy, LocalDateTime finishedOn) {

		this.executedTasks.add(new WorkflowExecutedTask(currentTaskId, currentTaskStartedOn, finishedOn, finishedBy));
		this.currentTaskId = null;
		this.currentTaskStartedOn = null;
	}

	public String getVariable(String key) {
		return variables.get(key);
	}

	public void setVariable(String key, String value) {
		variables.put(key, value);
	}

	public String getId() {
		return id;
	}

	public String getWorkflowDefinitionId() {
		return workflowDefinitionId;
	}

	public LocalDateTime getStartedOn() {
		return startedOn;
	}

	public String getStartedBy() {
		return startedBy;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public List<String> getRecordIds() {
		return recordIds;
	}

	public String getCurrentTaskId() {
		return currentTaskId;
	}

	public LocalDateTime getCurrentTaskStartedOn() {
		return currentTaskStartedOn;
	}

	public Map<String, String> getVariables() {
		return variables;
	}

	public List<WorkflowExecutedTask> getExecutedTasks() {
		return executedTasks;
	}

	public boolean isMarkAsWaitingForSystem() {
		return markAsWaitingForSystem;
	}

	public void setMarkAsWaitingForSystem(boolean markAsWaitingForSystem) {
		this.markAsWaitingForSystem = markAsWaitingForSystem;
	}

	public String getCollection() {
		return collection;
	}
}
