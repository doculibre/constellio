package com.constellio.app.modules.tasks.navigation;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.utils.TimedCache;
import com.vaadin.navigator.Navigator;
import org.joda.time.Duration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.ui.params.ParamUtils.addParams;

public class TaskViews extends CoreViews {
	public static String TASK_PARAM_KEY = "createTask";

	public TaskViews(Navigator navigator) {
		super(navigator);
	}

	public void taskManagement() {
		clearBreadcrumbTrail();
		navigator.navigateTo(TasksNavigationConfiguration.TASK_MANAGEMENT);
	}

	// TASKS

	public void displayTask(String id) {
		navigator.navigateTo(TasksNavigationConfiguration.DISPLAY_TASK + "/" + id);
	}

	public void addTask() {
		addTask(null);
	}

	public void addTask(String parentTaskId) {
		Map<String, String> params = new HashMap<>();
		if (parentTaskId != null) {
			params.put("parentId", parentTaskId);
		}
		navigator.navigateTo(addParams(TasksNavigationConfiguration.ADD_TASK, params));
	}


	private String getCurrentPageFragment() {
		return ConstellioUI.getCurrent().getPage().getLocation().getFragment().substring(1);
	}

	public void addLinkedRecordsToTask(List<String> recordIds) {
		SessionContext sessionContext = ConstellioUI.getCurrent().getSessionContext();
		TimedCache timedCache = new TimedCache(Duration.standardHours(1));
		if (recordIds != null) {
			timedCache.insert("linkedRecords", recordIds);
		}
		timedCache.insert("previousPage", getCurrentPageFragment());
		sessionContext.setAttribute(TASK_PARAM_KEY, timedCache);

		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("tempParams", TASK_PARAM_KEY);
		navigator.navigateTo(addParams(TasksNavigationConfiguration.ADD_TASK, paramMap));
	}


	public void listTasksLogs() {
		navigator.navigateTo(TasksNavigationConfiguration.LIST_TASKS_LOGS);
	}

	// WORKFLOWS

	public void listWorkflows() {
		navigator.navigateTo(TasksNavigationConfiguration.LIST_WORKFLOWS);
	}

	public void displayWorkflow(String id) {
		navigator.navigateTo(TasksNavigationConfiguration.DISPLAY_WORKFLOW + "/" + id);
	}

	public void displayWorkflowInstance(String id) {
		navigator.navigateTo(TasksNavigationConfiguration.DISPLAY_WORKFLOW_INSTANCE + "/" + id);
	}

	public void addWorkflow() {
		navigator.navigateTo(TasksNavigationConfiguration.ADD_WORKFLOW);
	}

	public void editWorkflow(String id) {
		navigator.navigateTo(TasksNavigationConfiguration.EDIT_WORKFLOW + "/" + id);
	}
}
