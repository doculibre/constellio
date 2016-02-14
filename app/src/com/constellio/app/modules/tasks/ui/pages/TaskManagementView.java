package com.constellio.app.modules.tasks.ui.pages;

import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface TaskManagementView extends BaseView, TasksViewGroup {

	void displayTasks(RecordVODataProvider provider);

	void reloadCurrentTab();

	void displayWorkflows(RecordVODataProvider provider);
}
