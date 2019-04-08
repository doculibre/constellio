package com.constellio.app.modules.tasks.ui.pages;

import java.util.List;

import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BaseView;

public interface TaskManagementView extends BaseView, TasksViewGroup {
	
	String TASK_MANAGEMENT_PRESENTER_PREVIOUS_TAB = "TaskManagementPresenterPreviousTab";

	String TASKS_TAB = "tasks";
	
	String TASKS_ASSIGNED_BY_CURRENT_USER = "tasksAssignedByCurrentUser";
	String TASKS_NOT_ASSIGNED = "nonAssignedTasks";
	String TASKS_ASSIGNED_TO_CURRENT_USER = "tasksAssignedToCurrentUser";
	String TASKS_RECENTLY_COMPLETED = "recentlyCompletedTasks";
	
	void displayTasks(RecordVODataProvider provider);

//	void displayWorkflows(RecordVODataProvider provider);

	com.vaadin.ui.Component getTabComponent(String tabId);

	TaskManagementViewImpl.Timestamp getTimestamp();

	void registerPreviousSelectedTab();
	
	void setTasksTabs(List<String> tasksTabs);

//	void setWorkflowsTabsVisible(boolean visible);
//	
//	void setStartWorkflowButtonVisible(boolean visible);
	
	void reloadCurrentTab();
	
	void setTabBadge(String tabId, String badge);
	
}
