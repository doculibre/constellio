package com.constellio.app.modules.tasks.services.menu;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.actions.TaskRecordActionsServices;
import com.constellio.app.modules.tasks.services.menu.behaviors.TaskMenuItemActionBehaviors;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_AUTO_ASSIGN;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_CLOSE;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_COMPLETE;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_CONSULT;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_CONSULT_LINK;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_CREATE_SUB_TASK;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_DELETE;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_EDIT;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_GENERATE_REPORT;
import static com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType.TASK_SHARE;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class TaskMenuItemServices {

	private TaskRecordActionsServices taskRecordActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	public TaskMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		taskRecordActionsServices = new TaskRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Task task, User user,
													List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(TASK_CONSULT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_CONSULT.name(),
					isMenuItemActionPossible(TASK_CONSULT.name(), task, user, params),
					$("DisplayTaskView.consult"), FontAwesome.SEARCH, -1, 100,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).display(task, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(TASK_EDIT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_EDIT.name(),
					isMenuItemActionPossible(TASK_EDIT.name(), task, user, params),
					$("DisplayTaskView.modifyTask"), FontAwesome.EDIT, -1, 100,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).edit(task, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(TASK_CONSULT_LINK.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_CONSULT_LINK.name(),
					isMenuItemActionPossible(TASK_CONSULT_LINK.name(), task, user, params),
					$("consultationLink"), FontAwesome.LINK, -1, 160,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).getConsultationLink(task, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(TASK_AUTO_ASSIGN.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_AUTO_ASSIGN.name(),
					isMenuItemActionPossible(TASK_AUTO_ASSIGN.name(), task, user, params),
					$("DisplayTaskView.autoAssignTask"), null, -1, 200,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).autoAssign(task, params));
			menuItemActions.add(menuItemAction);
		}
		if (!filteredActionTypes.contains(TASK_COMPLETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_COMPLETE.name(),
					isMenuItemActionPossible(TASK_COMPLETE.name(), task, user, params),
					$("DisplayTaskView.completeTask"), null, -1, 300,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).complete(task, params));
			menuItemActions.add(menuItemAction);
		}
		if (!filteredActionTypes.contains(TASK_CLOSE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_CLOSE.name(),
					isMenuItemActionPossible(TASK_CLOSE.name(), task, user, params),
					$("DisplayTaskView.closeTask"), null, -1, 400,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).close(task, params));
			menuItemActions.add(menuItemAction);
		}
		if (!filteredActionTypes.contains(TASK_CREATE_SUB_TASK.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_CREATE_SUB_TASK.name(),
					isMenuItemActionPossible(TASK_CREATE_SUB_TASK.name(), task, user, params),
					$("DisplayTaskView.createSubTask"), null, -1, 500,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).createSubTask(task, params));
			menuItemActions.add(menuItemAction);
		}
		if (!filteredActionTypes.contains(TASK_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_DELETE.name(),
					isMenuItemActionPossible(TASK_DELETE.name(), task, user, params),
					$("DisplayTaskView.deleteTask"), FontAwesome.TRASH_O, -1, 600,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).delete(task, params));
			menuItemActions.add(menuItemAction);
		}
		if (!filteredActionTypes.contains(TASK_GENERATE_REPORT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_GENERATE_REPORT.name(),
					isMenuItemActionPossible(TASK_GENERATE_REPORT.name(), task, user, params),
					$("SearchView.metadataReportTitle"), null, -1, 700,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).generateReport(task, params));
			menuItemActions.add(menuItemAction);
		}
		if (!filteredActionTypes.contains(TASK_SHARE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(TASK_SHARE.name(),
					isMenuItemActionPossible(TASK_SHARE.name(), task, user, params),
					$("DisplayTaskView.share"), null, -1, 700,
					(ids) -> new TaskMenuItemActionBehaviors(collection, appLayerFactory).shareTask(task, params));
			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, Task container, User user,
											MenuItemActionBehaviorParams params) {
		Record record = container.getWrappedRecord();

		switch (TaskItemActionType.valueOf(menuItemActionType)) {
			case TASK_CONSULT:
				return taskRecordActionsServices.isConsultActionPossible(record, user);
			case TASK_EDIT:
				return taskRecordActionsServices.isEditActionPossible(record, user);
			case TASK_CONSULT_LINK:
				return taskRecordActionsServices.isConsultLinkActionPossible(record, user);
			case TASK_AUTO_ASSIGN:
				return taskRecordActionsServices.isAutoAssignActionPossible(record, user);
			case TASK_COMPLETE:
				return taskRecordActionsServices.isCompleteTaskActionPossible(record, user);
			case TASK_CLOSE:
				return taskRecordActionsServices.isCloseTaskActionPossible(record, user);
			case TASK_CREATE_SUB_TASK:
				return taskRecordActionsServices.isCreateSubTaskActionPossible(record, user);
			case TASK_DELETE:
				return taskRecordActionsServices.isDeleteActionPossible(record, user);
			case TASK_GENERATE_REPORT:
				return taskRecordActionsServices.isGenerateReportActionPossible(record, user);
			case TASK_SHARE:
				return taskActionsServices.isShareActionPossible(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(String type, boolean possible, String caption, Resource icon,
											   int group, int priority, Consumer<List<String>> command) {
		return MenuItemAction.builder()
				.type(type)
				.state(new MenuItemActionState(possible ? VISIBLE : HIDDEN))
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.recordsLimit(1)
				.build();
	}

	public enum TaskItemActionType {
		TASK_CONSULT,
		TASK_EDIT,
		TASK_CONSULT_LINK,
		TASK_AUTO_ASSIGN,
		TASK_COMPLETE,
		TASK_CLOSE,
		TASK_CREATE_SUB_TASK,
		TASK_DELETE,
		TASK_GENERATE_REPORT,
		TASK_SHARE
	}
}
