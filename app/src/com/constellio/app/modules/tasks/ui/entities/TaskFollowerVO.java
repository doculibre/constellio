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
