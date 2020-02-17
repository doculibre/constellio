package com.constellio.app.modules.tasks.navigation;

import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.BaseView;
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

	public void addTaskToFolder(String folderId) {
		Map<String, String> params = new HashMap<>();
		if (folderId != null) {
			params.put("folderId", folderId);
		}
		params.put("previousPage", getCurrentPageFragment());
		navigator.navigateTo(addParams(TasksNavigationConfiguration.ADD_TASK, params));
	}

	public void addTaskToDocument(String documentId) {
		Map<String, Object> params = new HashMap<>();
		if (documentId != null) {
			params.put("documentId", documentId);
		}
		params.put("previousPage", getCurrentPageFragment());


		navigator.navigateTo(addParams(TasksNavigationConfiguration.ADD_TASK, params));
	}

	private String getCurrentPageFragment() {
		return ConstellioUI.getCurrent().getPage().getLocation().getFragment().substring(1);
	}

	public void addTaskToFoldersOrDocuments(List<String> foldersId, List<String> documentsId,
											BaseView view) {

		Map<String, List<String>> params = new HashMap<>();
		if (foldersId != null) {
			if (foldersId != null) {
				params.put("folderId", foldersId);
			}
			if (documentsId != null) {
				params.put("documentId", documentsId);
			}
			TimedCache timedCache = new TimedCache(Duration.standardHours(1));
			timedCache.insert(TASK_PARAM_KEY, params);

			view.getSessionContext().setAttribute(TASK_PARAM_KEY, timedCache);

			Map<String, String> paramMap = new HashMap<>();
			paramMap.put("tempParams", TASK_PARAM_KEY);
			paramMap.put("previousPage", getCurrentPageFragment());

			navigator.navigateTo(addParams(TasksNavigationConfiguration.ADD_TASK, paramMap));
		}

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
