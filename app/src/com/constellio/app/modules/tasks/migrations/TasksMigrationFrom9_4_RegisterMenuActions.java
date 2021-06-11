package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices.TaskItemActionType;
import com.constellio.app.services.actionDisplayManager.MenuDisplayItem;
import com.constellio.app.services.actionDisplayManager.MenuPositionActionOptions;
import com.constellio.app.services.actionDisplayManager.MenusDisplayManager;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.Action;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemServices;
import com.vaadin.server.FontAwesome;

import java.util.Arrays;

import static com.constellio.app.modules.tasks.extensions.TaskMenuItemActionsExtension.CONSULTATION_LINK;

public class TasksMigrationFrom9_4_RegisterMenuActions implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		MenusDisplayManager menusDisplayManager = appLayerFactory.getMenusDisplayManager();

		MenusDisplayTransaction menusDisplayTransaction = new MenusDisplayTransaction();
		addTaskActionsToTransaction(menusDisplayTransaction);
		addRecordActionsToTransaction(menusDisplayTransaction);
		menusDisplayManager.execute(collection, menusDisplayTransaction);
	}

	public void addTaskActionsToTransaction(MenusDisplayTransaction transaction) {
		Action action = Action.ADD_UPDATE;
		String schemaType = Task.SCHEMA_TYPE;
		MenuPositionActionOptions options = MenuPositionActionOptions.displayActionAtEnd();

		Arrays
				.asList(
						new MenuDisplayItem(TaskItemActionType.TASK_CONSULT.name(), FontAwesome.SEARCH.name(), "DisplayTaskView.consult", true, null, true),
						new MenuDisplayItem(TaskItemActionType.TASK_EDIT.name(), FontAwesome.EDIT.name(), "DisplayTaskView.modifyTask", true, null, true),
						new MenuDisplayItem(TaskItemActionType.TASK_CREATE_SUB_TASK.name(), FontAwesome.TASKS.name(), "DisplayTaskView.createSubTask", true, null, false),
						new MenuDisplayItem(TaskItemActionType.TASK_CONSULT_LINK.name(), FontAwesome.LINK.name(), "consultationLink", true, null, false),
						new MenuDisplayItem(TaskItemActionType.TASK_AUTO_ASSIGN.name(), FontAwesome.USER.name(), "DisplayTaskView.autoAssignTask", true, null, false),
						new MenuDisplayItem(TaskItemActionType.TASK_GENERATE_REPORT.name(), FontAwesome.PRINT.name(), "SearchView.metadataReportTitle", true, null, false),
						new MenuDisplayItem(TaskItemActionType.TASK_SHARE.name(), FontAwesome.PAPER_PLANE_O.name(), "DisplayTaskView.share", true, null, false),
						new MenuDisplayItem(TaskItemActionType.TASK_COMPLETE.name(), FontAwesome.SEARCH.name(), "DisplayTaskView.completeTask", true, null, true),
						new MenuDisplayItem(TaskItemActionType.TASK_CLOSE.name(), FontAwesome.CHECK.name(), "DisplayTaskView.closeTask", true, null, false),
						new MenuDisplayItem(TaskItemActionType.TASK_DELETE.name(), FontAwesome.TIMES.name(), "DisplayTaskView.deleteTask", true, null, true)
				)
				.forEach(menuDisplayItem -> transaction.addElement(action, schemaType, menuDisplayItem, options));
	}

	public void addRecordActionsToTransaction(MenusDisplayTransaction transaction) {
		Action action = Action.ADD_UPDATE;
		String schemaType = MenuItemServices.BATCH_ACTIONS_FAKE_SCHEMA_TYPE;
		MenuPositionActionOptions options = MenuPositionActionOptions.displayActionAtEnd();

		transaction.addElement(action, schemaType, new MenuDisplayItem(CONSULTATION_LINK, FontAwesome.LINK.name(), "consultationLink", true, null, false), options);
	}
}
