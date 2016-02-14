package com.constellio.app.modules.tasks.ui.entities;

import java.io.Serializable;

import org.joda.time.LocalDate;

import com.constellio.app.modules.tasks.model.wrappers.TaskStatusType;

public class WorkflowTaskProgressionVO implements Serializable {

	String title;

	WorkflowTaskVO workflowTaskVO;

	String decision;

	LocalDate dueDate;

	TaskStatusType status;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public WorkflowTaskVO getWorkflowTaskVO() {
		return workflowTaskVO;
	}

	public void setWorkflowTaskVO(WorkflowTaskVO workflowTaskVO) {
		this.workflowTaskVO = workflowTaskVO;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public TaskStatusType getStatus() {
		return status;
	}

	public void setStatus(TaskStatusType status) {
		this.status = status;
	}
}
