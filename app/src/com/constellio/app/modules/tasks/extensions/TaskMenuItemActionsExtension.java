package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.modules.tasks.services.TasksSchemasRecordsServices;
import com.constellio.app.modules.tasks.services.menu.TaskMenuItemServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class TaskMenuItemActionsExtension extends MenuItemActionsExtension {

	private TasksSchemasRecordsServices tasksSchema;

	private TaskMenuItemServices taskMenuItemServices;

	public TaskMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		tasksSchema = new TasksSchemasRecordsServices(collection, appLayerFactory);

		taskMenuItemServices = new TaskMenuItemServices(collection, appLayerFactory);
	}

	@Override
	public void addMenuItemActionsForRecord(MenuItemActionExtensionAddMenuItemActionsForRecordParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();
		List<MenuItemAction> menuItemActions = params.getMenuItemActions();
		List<String> excludedActionTypes = params.getExcludedActionTypes();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();

		if (record != null) {
			if (record.isOfSchemaType(Task.SCHEMA_TYPE)) {
				menuItemActions.addAll(taskMenuItemServices.getActionsForRecord(tasksSchema.wrapTask(record), user,
						excludedActionTypes, behaviorParams));
			}
		}
	}


	@Override
	public MenuItemActionState getActionStateForRecord(MenuItemActionExtensionGetActionStateForRecordParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();
		String actionType = params.getMenuItemActionType();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();

		if (record.isOfSchemaType(Task.SCHEMA_TYPE)) {
			return toState(taskMenuItemServices.isMenuItemActionPossible(actionType, tasksSchema.wrapTask(record),
					user, behaviorParams));
		}

		return null;
	}
}
