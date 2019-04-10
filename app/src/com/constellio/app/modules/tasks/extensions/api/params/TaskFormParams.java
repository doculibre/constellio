package com.constellio.app.modules.tasks.extensions.api.params;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.ui.pages.tasks.AddEditTaskPresenter;

public class TaskFormParams {
	private AddEditTaskPresenter addEditTaskPresenter;
	private Task task;

	public TaskFormParams(AddEditTaskPresenter addEditTaskPresenter, Task task) {
		this.addEditTaskPresenter = addEditTaskPresenter;
		this.task = task;
	}

	public AddEditTaskPresenter getPresenter() {
		return addEditTaskPresenter;
	}

	public Task getTask() {
		return task;
	}
}
