package com.constellio.app.modules.tasks.ui.entities;

import java.io.Serializable;

public class TaskFollowerVO implements Serializable {
	String followerId;
	boolean followTaskStatusModified;
	boolean followTaskAssigneeModified;
	boolean followSubTasksModified;
	boolean followTaskCompleted;
	boolean followTaskDeleted;

	public TaskFollowerVO(String followerId, Boolean followTaskAssigneeModified,
			Boolean followSubTasksModified, Boolean followTaskStatusModified, Boolean followTaskCompleted,
			Boolean followTaskDeleted) {
		this.followerId = followerId;
		this.followTaskAssigneeModified = followTaskAssigneeModified;
		this.followSubTasksModified = followSubTasksModified;
		this.followTaskStatusModified = followTaskStatusModified;
		this.followTaskCompleted = followTaskCompleted;
		this.followTaskDeleted = followTaskDeleted;
	}

	public TaskFollowerVO() {

	}

	public String getFollowerId() {
		return followerId;
	}

	public void setFollowerId(String followerId) {
		this.followerId = followerId;
	}

	public boolean isFollowTaskStatusModified() {
		return followTaskStatusModified;
	}

	public void setFollowTaskStatusModified(boolean followTaskStatusModified) {
		this.followTaskStatusModified = followTaskStatusModified;
	}

	public boolean isFollowTaskAssigneeModified() {
		return followTaskAssigneeModified;
	}

	public void setFollowTaskAssigneeModified(boolean followTaskAssigneeModified) {
		this.followTaskAssigneeModified = followTaskAssigneeModified;
	}

	public boolean isFollowSubTasksModified() {
		return followSubTasksModified;
	}

	public void setFollowSubTasksModified(boolean followSubTasksModified) {
		this.followSubTasksModified = followSubTasksModified;
	}

	public boolean isFollowTaskCompleted() {
		return followTaskCompleted;
	}

	public void setFollowTaskCompleted(boolean followTaskCompleted) {
		this.followTaskCompleted = followTaskCompleted;
	}

	public boolean isFollowTaskDeleted() {
		return followTaskDeleted;
	}

	public void setFollowTaskDeleted(boolean followTaskDeleted) {
		this.followTaskDeleted = followTaskDeleted;
	}
}
