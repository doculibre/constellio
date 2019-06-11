package com.constellio.app.modules.rm.extensions;

import com.constellio.app.extensions.menu.MenuItemActionsExtension;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices;
import com.constellio.app.modules.rm.services.menu.DocumentMenuItemServices;
import com.constellio.app.modules.rm.services.menu.FolderMenuItemServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;

import java.util.List;

public class RMMenuItemActionsExtension extends MenuItemActionsExtension {

	private RMSchemasRecordsServices rm;

	private DocumentMenuItemServices documentMenuItemServices;
	private FolderMenuItemServices folderMenuItemServices;
	private ContainerMenuItemServices containerMenuItemServices;

	public RMMenuItemActionsExtension(String collection, AppLayerFactory appLayerFactory) {
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		documentMenuItemServices = new DocumentMenuItemServices(collection, appLayerFactory);
		folderMenuItemServices = new FolderMenuItemServices(collection, appLayerFactory);
		containerMenuItemServices = new ContainerMenuItemServices(collection, appLayerFactory);
	}

	@Override
	public void addMenuItemActions(MenuItemActionExtensionAddMenuItemActionsParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();
		List<MenuItemAction> menuItemActions = params.getMenuItemActions();
		List<String> filteredActionTypes = params.getFilteredActionTypes();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();

		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			menuItemActions.addAll(documentMenuItemServices.getActionsForRecord(rm.wrapDocument(record), user,
					filteredActionTypes, behaviorParams));
		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			menuItemActions.addAll(folderMenuItemServices.getActionsForRecord(rm.wrapFolder(record), user,
					filteredActionTypes, behaviorParams));
		} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			menuItemActions.addAll(containerMenuItemServices.getActionsForRecord(rm.wrapContainerRecord(record), user,
					filteredActionTypes, behaviorParams));
		}
	}

	@Override
	public MenuItemActionState getStateForAction(MenuItemActionExtensionGetStateForActionParams params) {
		Record record = params.getRecord();
		User user = params.getBehaviorParams().getUser();
		MenuItemAction action = params.getMenuItemAction();
		MenuItemActionBehaviorParams behaviorParams = params.getBehaviorParams();

		if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			return toState(folderMenuItemServices.isMenuItemActionPossible(action.getType(), rm.wrapFolder(record),
					user, behaviorParams), null);
		} else if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			return toState(documentMenuItemServices.isMenuItemActionPossible(action.getType(), rm.wrapDocument(record),
					user, behaviorParams), null);
		} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			return toState(containerMenuItemServices.isMenuItemActionPossible(action.getType(),
					rm.wrapContainerRecord(record), user, behaviorParams), null);
		}
		return null;
	}

	private MenuItemActionState toState(boolean possible, String reason) {
		return possible ? MenuItemActionState.VISIBLE :
			   (reason != null ? MenuItemActionState.DISABLED : MenuItemActionState.HIDDEN);
	}
}
