package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.extensions.menu.AdvancedSearchMenuItemActionExtension;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RMAdvancedSearchMenuItemActionsExtension extends AdvancedSearchMenuItemActionExtension {


	public RMAdvancedSearchMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
	}

	protected MenuItemActionState getActionStateForBatchProcessing(LogicalSearchQuery query, User user) {

		if (!user.has(CorePermissions.MODIFY_RECORDS_USING_BATCH_PROCESS).globally()) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		}

		String schemaType = getSchemaType(query);
		if (schemaType == null) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		} else if (!schemaType.equals(Document.SCHEMA_TYPE) && !schemaType.equals(Folder.SCHEMA_TYPE) &&
				   !schemaType.equals(ContainerRecord.SCHEMA_TYPE) && !schemaType.equals(StorageSpace.SCHEMA_TYPE)) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		}

		return new MenuItemActionState(MenuItemActionStateStatus.VISIBLE);
	}

	protected MenuItemActionState getActionStateForReports(LogicalSearchQuery query) {
		String schemaType = getSchemaType(query);
		if (schemaType == null) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		} else if (!schemaType.equals(Document.SCHEMA_TYPE) && !schemaType.equals(Folder.SCHEMA_TYPE) &&
				   !schemaType.equals(ContainerRecord.SCHEMA_TYPE)) {
			return new MenuItemActionState(MenuItemActionStateStatus.HIDDEN);
		}

		return new MenuItemActionState(MenuItemActionStateStatus.VISIBLE);
	}

	@Override
	protected boolean noPDFButton(String schemaType) {
		return !(Folder.SCHEMA_TYPE.equals(schemaType) || Document.SCHEMA_TYPE.equals(schemaType));
	}
}
