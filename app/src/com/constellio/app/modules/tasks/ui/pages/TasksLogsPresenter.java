package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.records.wrappers.User;

public class TasksLogsPresenter extends BasePresenter<TasksLogsView> {
	public TasksLogsPresenter(TasksLogsView view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		//TODO
		return false;
	}
}
