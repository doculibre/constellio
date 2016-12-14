package com.constellio.app.modules.tasks.ui.pages.tasks;

import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface DisplayTaskView extends BaseView, TasksViewGroup {
	void refreshSubTasksTable();

	void setSubTasks(RecordVODataProvider dataProvider);

	void setEvents(RecordVODataProvider tasksDataProvider);

	void selectMetadataTab();

	void selectTasksTab();
}
