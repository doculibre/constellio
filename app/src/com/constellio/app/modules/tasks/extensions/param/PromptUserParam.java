package com.constellio.app.modules.tasks.extensions.param;

import com.constellio.app.modules.tasks.extensions.action.Action;
import com.constellio.app.modules.tasks.model.wrappers.Task;

public class PromptUserParam {
	Task task;
	Action action;

	public PromptUserParam(Task task, Action action) {
		this.task = task;
		this.action = action;
	}

	public Task getTask() {
		return task;
	}

	public Action getAction() {
		return action;
	}
}
