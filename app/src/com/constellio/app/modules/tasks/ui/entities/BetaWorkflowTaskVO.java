package com.constellio.app.modules.tasks.ui.entities;

import java.io.Serializable;

public class BetaWorkflowTaskVO implements Serializable {

	TaskVO taskVO;

	String id;
	
	String type;

	String decision;

	String title;

	boolean hasChildren;

	public TaskVO getTaskVO() {
		return taskVO;
	}

	public void setTaskVO(TaskVO taskVO) {
		this.taskVO = taskVO;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public boolean hasChildren() {
		return hasChildren;
	}

	public void setHasChildren(boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean hasSameIdDecision(BetaWorkflowTaskVO workflowTaskVO) {
		return equals(id, workflowTaskVO.getId()) && equals(decision, workflowTaskVO.getDecision());
	}

	private boolean equals(String value1, String value2) {
		if (value1 == null) {
			return value2 == null;
		} else {
			return value1.equals(value2);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((decision == null) ? 0 : decision.hashCode());
		result = prime * result + (hasChildren ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((taskVO == null) ? 0 : taskVO.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BetaWorkflowTaskVO other = (BetaWorkflowTaskVO) obj;
		if (decision == null) {
			if (other.decision != null)
				return false;
		} else if (!decision.equals(other.decision))
			return false;
		if (hasChildren != other.hasChildren)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (taskVO == null) {
			if (other.taskVO != null)
				return false;
		} else if (!taskVO.equals(other.taskVO))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
