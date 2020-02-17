package com.constellio.app.modules.tasks.navigation;

import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.entities.navigation.NavigationItem;
import com.constellio.app.modules.rm.ui.pages.document.DisplayDocumentView;
import com.constellio.app.modules.rm.ui.pages.folder.DisplayFolderView;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.TasksSearchServices;
import com.constellio.app.modules.tasks.ui.pages.TaskManagementView;
import com.constellio.app.modules.tasks.ui.pages.TaskManagementViewImpl;
import com.constellio.app.modules.tasks.ui.pages.TasksLogsViewImpl;
import com.constellio.app.modules.tasks.ui.pages.tasks.AddEditTaskViewImpl;
import com.constellio.app.modules.tasks.ui.pages.tasks.DisplayTaskViewImpl;
import com.constellio.app.modules.tasks.ui.pages.viewGroups.TasksViewGroup;
import com.constellio.app.modules.tasks.ui.pages.workflow.BetaAddEditWorkflowViewImpl;
import com.constellio.app.modules.tasks.ui.pages.workflow.BetaDisplayWorkflowViewImpl;
import com.constellio.app.modules.tasks.ui.pages.workflow.BetaListWorkflowsViewImpl;
import com.constellio.app.modules.tasks.ui.pages.workflowInstance.BetaDisplayWorkflowInstanceViewImpl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.ConstellioHeader;
import com.constellio.app.ui.pages.base.MainLayout;
import com.constellio.app.ui.pages.management.AdminView;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;

import java.io.Serializable;

public class TasksNavigationConfiguration implements Serializable {
	public static final String TASK_MANAGEMENT = "taskManagement";
	public static final String ADD_TASK = "addTask";
	public static final String WORKFLOW_MANAGEMENT = "workflowManagement";
	public static final String WORKFLOW_MANAGEMENT_ICON = "images/icons/config/workflows.png";
	public static final String EDIT_TASK = "editTask";
	public static final String DISPLAY_TASK = "displayTask";
	public static final String ADD_WORKFLOW = "addWorkflow";
	public static final String EDIT_WORKFLOW = "editWorkflow";
	public static final String DISPLAY_WORKFLOW = "displayWorkflow";
	public static final String LIST_WORKFLOWS = "listWorkflows";
	public static final String LIST_TASKS_LOGS = "listTaksLogs";
	public static final String DISPLAY_WORKFLOW_INSTANCE = "displayWorkflowInstance";

	public static void configureNavigation(NavigationConfig config) {
		configureMainLayoutNavigation(config);
		configureHeaderActionMenu(config);
		configureCollectionAdmin(config);
	}

	public static void configureNavigation(NavigatorConfigurationService service) {
		service.register(ADD_TASK, AddEditTaskViewImpl.class);
		service.register(EDIT_TASK, AddEditTaskViewImpl.class);
		service.register(DISPLAY_TASK, DisplayTaskViewImpl.class);
		service.register(ADD_WORKFLOW, BetaAddEditWorkflowViewImpl.class);
		service.register(EDIT_WORKFLOW, BetaAddEditWorkflowViewImpl.class);
		service.register(DISPLAY_WORKFLOW, BetaDisplayWorkflowViewImpl.class);
		service.register(LIST_WORKFLOWS, BetaListWorkflowsViewImpl.class);
		service.register(TASK_MANAGEMENT, TaskManagementViewImpl.class);
		service.register(LIST_TASKS_LOGS, TasksLogsViewImpl.class);
		service.register(DISPLAY_WORKFLOW_INSTANCE, BetaDisplayWorkflowInstanceViewImpl.class);
	}

	private static void configureHeaderActionMenu(NavigationConfig config) {
		config.add(ConstellioHeader.ACTION_MENU, new NavigationItem.Active(ADD_TASK) {
			@Override
			public void activate(Navigation navigate) {
				View currentView = ConstellioUI.getCurrent().getCurrentView();
				if (currentView instanceof DisplayFolderView) {
					DisplayFolderView displayFolderView = (DisplayFolderView) currentView;
					String folderId = displayFolderView.getSummaryRecord().getId();
					navigate.to(TaskViews.class).addTaskToFolder(folderId);
				} else if (currentView instanceof DisplayDocumentView) {
					DisplayDocumentView displayFolderView = (DisplayDocumentView) currentView;
					String documentId = displayFolderView.getRecordVO().getId();
					navigate.to(TaskViews.class).addTaskToDocument(documentId);
				} else {
					navigate.to(TaskViews.class).addTask();
				}
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return ComponentState.ENABLED;
			}
		}, 0);
	}

	private static void configureMainLayoutNavigation(NavigationConfig config) {
		config.add(MainLayout.MAIN_LAYOUT_NAVIGATION, new NavigationItem.Active(TASK_MANAGEMENT, FontAwesome.TASKS, TasksViewGroup.class) {
			@Override
			public void activate(Navigation navigate) {
				ConstellioUI.getCurrentSessionContext().setAttribute(TaskManagementView.TASK_MANAGEMENT_PRESENTER_PREVIOUS_TAB, null);

				navigate.to(TaskViews.class).taskManagement();
			}

			@Override
			public int getOrderValue() {
				return 20;
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return ComponentState.ENABLED;
			}

			@Override
			public String getBadge(User user, AppLayerFactory appLayerFactory) {
				if (user != null) {
					TasksSchemasRecordsServices tasksSchemasRecordsServices = new TasksSchemasRecordsServices(user.getCollection(), appLayerFactory);
					TasksSearchServices tasksSearchServices = new TasksSearchServices(tasksSchemasRecordsServices);
					if (Toggle.SHOW_UNREAD_TASKS.isEnabled()) {
						long unreadCount = tasksSearchServices.getCountUnreadTasksToUserQuery(user);
						return unreadCount > 0 ? "" + unreadCount : "";
					}
				}
				return "";
			}
		});
	}

	private static void configureCollectionAdmin(NavigationConfig config) {
		config.add(AdminView.COLLECTION_SECTION, new NavigationItem.Active(WORKFLOW_MANAGEMENT, WORKFLOW_MANAGEMENT_ICON) {
			@Override
			public void activate(Navigation navigate) {
				navigate.to(TaskViews.class).listWorkflows();
			}

			@Override
			public ComponentState getStateFor(User user, AppLayerFactory appLayerFactory) {
				return ComponentState.visibleIf(false);
			}
		});
	}
}
