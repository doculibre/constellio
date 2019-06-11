package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.MenuItemActionExtension;
import com.constellio.app.modules.rm.extensions.api.MenuItemActionExtension.MenuItemActionExtensionAddMenuItemActionsParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.MenuItemActionBehaviorParams;
import com.constellio.app.modules.rm.services.menu.record.DocumentMenuItemServices;
import com.constellio.app.modules.rm.services.menu.record.FolderMenuItemServices;
import com.constellio.app.modules.rm.services.menu.record.RecordListMenuItemServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private DocumentRecordActionsServices documentRecordActionsServices;
	private ContainerRecordActionsServices containerRecordActionsServices;
	private RMModuleExtensions rmModuleExtensions;
	private RMSchemasRecordsServices rm;

	private DocumentMenuItemServices documentMenuItemServices;
	private FolderMenuItemServices folderMenuItemServices;
	private RecordListMenuItemServices recordListMenuItemServices;

	public MenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		containerRecordActionsServices = new ContainerRecordActionsServices(collection, appLayerFactory);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);

		documentMenuItemServices = new DocumentMenuItemServices(collection, appLayerFactory);
		folderMenuItemServices = new FolderMenuItemServices(collection, appLayerFactory);
		recordListMenuItemServices = new RecordListMenuItemServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, MenuItemActionBehaviorParams params) {
		return getActionsForRecord(record, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		if (params.getView() == null) {
			return Collections.emptyList();
		}
		User user = params.getUser();

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			menuItemActions.addAll(documentMenuItemServices.getActionsForRecord(rm.wrapDocument(record), user,
					filteredActionTypes, params));
		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			menuItemActions.addAll(folderMenuItemServices.getActionsForRecord(rm.wrapFolder(record), user,
					filteredActionTypes, params));
		} else if (record.isOfSchemaType(ContainerRecord.SCHEMA_TYPE)) {
			if (!filteredActionTypes.contains(MenuItemActionType.CONTAINER_EDIT.name())) {
				boolean isEditPossible = containerRecordActionsServices.isEditActionPossible(record, user);
			}
		} else if (record.isOfSchemaType(User.SCHEMA_TYPE)) {
			// TODO
		} else if (record.isOfSchemaType(Group.SCHEMA_TYPE)) {
			// TODO
		}

		addMenuItemActionsFromExtensions(record, user, params.getView(), menuItemActions);

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, MenuItemActionBehaviorParams params) {
		return getActionsForRecords(records, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		menuItemActions.addAll(recordListMenuItemServices.getActionsForRecords(records, params.getUser(),
				filteredActionTypes, params));

		// TODO extensions
		//addMenuItemActionsFromExtensions(record, user, params.getView(), menuItemActions);

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForRecords(LogicalSearchQuery query, User user,
													 MenuItemActionBehaviorParams params) {
		// TODO
		return null;
	}

	public MenuItemActionState getStateForAction(MenuItemAction action, Record record, User user,
												 MenuItemActionBehaviorParams params) {
		boolean menuItemActionPossible = false;

		if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			menuItemActionPossible = folderMenuItemServices.isMenuItemActionPossible(
					MenuItemActionType.valueOf(action.getType()), rm.wrapFolder(record), user, params);
		} else if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			menuItemActionPossible = documentMenuItemServices.isMenuItemActionPossible(
					MenuItemActionType.valueOf(action.getType()), rm.wrapDocument(record), user, params);
		} else {
			// TODO
		}
		return toState(menuItemActionPossible, null);
	}

	public MenuItemActionState getStateForAction(MenuItemAction action, List<Record> records, User user,
												 MenuItemActionBehaviorParams params) {
		return recordListMenuItemServices.getMenuItemActionState(MenuItemActionType.valueOf(action.getType()), records, user, params);
	}

	public MenuItemActionState getStateForAction(MenuItemAction action, LogicalSearchQuery query, User user,
												 MenuItemActionBehaviorParams params) {
		return null;
	}

	private MenuItemAction buildMenuItemAction(MenuItemActionType type, boolean possible, String reason, String caption,
											   Resource icon, int group, int priority, Class buttonClass,
											   Runnable command) {
		return MenuItemAction.builder()
				.type(type.name())
				.state(toState(possible, reason))
				.reason(reason)
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.button(buttonClass)
				.build();
	}

	private MenuItemActionState toState(boolean possible, String reason) {
		return possible ? MenuItemActionState.VISIBLE :
			   (reason != null ? MenuItemActionState.DISABLED : MenuItemActionState.HIDDEN);
	}

	private void addMenuItemActionsFromExtensions(Record record, User user, BaseView baseView,
												  List<MenuItemAction> menuItemActions) {
		for (MenuItemActionExtension menuItemActionExtension : rmModuleExtensions.getMenuItemActionExtensions()) {
			menuItemActionExtension.addMenuItemActions(
					new MenuItemActionExtensionAddMenuItemActionsParams(record, user, baseView, menuItemActions));
		}
	}

	private boolean areAllRecordsOfSameSchema(List<Record> records) {
		String schemaCode = null;
		for (Record record : records) {
			if (schemaCode == null) {
				schemaCode = record.getSchemaCode();
			}
			if (!record.getSchemaCode().equals(schemaCode)) {
				return false;
			}
		}
		return true;
	}

}
