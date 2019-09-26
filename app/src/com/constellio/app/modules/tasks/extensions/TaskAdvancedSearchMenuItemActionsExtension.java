package com.constellio.app.modules.tasks.extensions;

import com.constellio.app.extensions.menu.AdvancedSearchMenuItemActionExtension;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskAdvancedSearchMenuItemActionsExtension extends AdvancedSearchMenuItemActionExtension {

	public TaskAdvancedSearchMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
	}

	protected MenuItemActionState getActionStateForBatchProcessing(LogicalSearchQuery query, User user) {

		if (!user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally()) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		}

		String schemaType = getSchemaType(query);
		if (schemaType == null) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		} else if (!schemaType.equals(Task.SCHEMA_TYPE)) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		}

		return new MenuItemActionState(MenuItemActionStateStatus.VISIBLE);
	}

	protected MenuItemActionState getActionStateForReports(LogicalSearchQuery query) {
		String schemaType = getSchemaType(query);
		if (schemaType == null) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		} else if (!schemaType.equals(Task.SCHEMA_TYPE)) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		}

		return new MenuItemActionState(MenuItemActionStateStatus.VISIBLE);
	}

	@Override
	protected boolean noPDFButton(String schemaType) {
		return !(Task.SCHEMA_TYPE.equals(schemaType));
	}
}
