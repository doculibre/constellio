package com.constellio.app.modules.tasks.navigation;

import static com.constellio.app.ui.params.ParamUtils.addParams;

import java.util.Map;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.google.gwt.dev.util.collect.HashMap;
import com.vaadin.navigator.Navigator;

public class TaskViews extends CoreViews {
	public TaskViews(Navigator navigator) {
		super(navigator);
	}

	public void taskManagement() {
		navigator.navigateTo(NavigatorConfigurationService.TASK_MANAGEMENT);
	}

	// TASKS

	public void displayTask(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_TASK + "/" + id);
	}

	public void addTask() {
		addTask(null);
	}

	public void addTask(String parentTaskId) {
		Map<String, String> params = new HashMap<>();
		if (parentTaskId != null) {
			params.put("parentId", parentTaskId);
		}
		navigator.navigateTo(addParams(NavigatorConfigurationService.ADD_TASK, params));
	}

	// WORKFLOWS

	public void listWorkflows() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_WORKFLOWS);
	}

	public void displayWorkflow(String id) {
		navigator.navigateTo(NavigatorConfigurationService.DISPLAY_WORKFLOW + "/" + id);
	}
}
