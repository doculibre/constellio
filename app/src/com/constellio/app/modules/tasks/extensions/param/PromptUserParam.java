package com.constellio.app.modules.tasks.extensions.param;

import com.constellio.app.modules.tasks.model.wrappers.Task;

public class PromptUserParam {
	Task task;

	public PromptUserParam(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}
}
