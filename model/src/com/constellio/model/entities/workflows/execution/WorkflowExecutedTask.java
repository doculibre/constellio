package com.constellio.model.entities.workflows.execution;

import org.joda.time.LocalDateTime;

public class WorkflowExecutedTask {

	private String taskId;

	private LocalDateTime startedOn;

	private LocalDateTime finishedOn;

	private String finishedBy;

	public WorkflowExecutedTask(String taskId, LocalDateTime startedOn, LocalDateTime finishedOn, String finishedBy) {
		this.taskId = taskId;
		this.startedOn = startedOn;
		this.finishedOn = finishedOn;
		this.finishedBy = finishedBy;
	}

	public String getTaskId() {
		return taskId;
	}

	public LocalDateTime getStartedOn() {
		return startedOn;
	}

	public LocalDateTime getFinishedOn() {
		return finishedOn;
	}

	public String getFinishedBy() {
		return finishedBy;
	}
}
