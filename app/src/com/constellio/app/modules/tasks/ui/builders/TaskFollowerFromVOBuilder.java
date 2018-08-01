package com.constellio.app.modules.tasks.ui.builders;

import com.constellio.app.modules.tasks.model.wrappers.structures.TaskFollower;
import com.constellio.app.modules.tasks.ui.entities.TaskFollowerVO;

public class TaskFollowerFromVOBuilder {
	public TaskFollower build(TaskFollowerVO taskFollowerVO) {
		return new TaskFollower().setFollowSubTasksModified(taskFollowerVO.isFollowSubTasksModified())
				.setFollowTaskAssigneeModified(taskFollowerVO.isFollowTaskAssigneeModified())
				.setFollowTaskDeleted(taskFollowerVO.isFollowTaskDeleted())
				.setFollowTaskCompleted(taskFollowerVO.isFollowTaskCompleted())
				.setFollowerId(taskFollowerVO.getFollowerId())
				.setFollowTaskStatusModified(taskFollowerVO.isFollowTaskStatusModified());
	}
}
