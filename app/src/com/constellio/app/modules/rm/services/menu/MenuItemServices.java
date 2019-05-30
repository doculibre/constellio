package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.MenuItemActionExtension;
import com.constellio.app.modules.rm.extensions.api.MenuItemActionExtension.MenuItemActionExtensionAddMenuItemActionsParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.behaviors.DocumentRecordActionBehaviors;
import com.constellio.app.modules.rm.services.actions.behaviors.RecordActionBehaviorParams;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MenuItemServices {

	private String collection;
	private AppLayerFactory appLayerFactory;
	private DocumentRecordActionsServices documentRecordActionsServices;
	private RMModuleExtensions rmModuleExtensions;
	private RMSchemasRecordsServices rm;

	public MenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, RecordActionBehaviorParams params) {
		return getActionsForRecord(record, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, List<String> filteredActionTypes,
													RecordActionBehaviorParams params) {
		User user = params.getUser();

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_DISPLAY.name())) {
				boolean isDisplayActionPossible = documentRecordActionsServices.isDisplayActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_DISPLAY, isDisplayActionPossible,
						"DisplayDocumentView.displayDocument", FontAwesome.FILE_O, -1, 100,
						() -> new DocumentRecordActionBehaviors(collection, appLayerFactory).display(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_OPEN.name())) {
				boolean isOpenActionPossible = documentRecordActionsServices.isOpenActionPossible(record, user);
				// FIXME better way? get icon by mime-type?
				Document document = rm.wrapDocument(record);
				Resource icon = FileIconUtils.getIcon(document.getContent() != null ?
													  document.getContent().getCurrentVersion().getFilename() : "");
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_OPEN, isOpenActionPossible,
						"DisplayDocumentView.openDocument", icon, -1, 200,
						() -> new DocumentRecordActionBehaviors(collection, appLayerFactory).open(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_EDIT.name())) {
				boolean isEditActionPossible = documentRecordActionsServices.isEditActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_EDIT, isEditActionPossible,
						"DisplayDocumentView.editDocument", FontAwesome.EDIT, -1, 300,
						() -> new DocumentRecordActionBehaviors(collection, appLayerFactory).copy(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_DOWNLOAD.name())) {
				boolean isDownloadPossible = documentRecordActionsServices.isDownloadActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_DOWNLOAD, isDownloadPossible,
						"DocumentContextMenu.downloadDocument", FontAwesome.DOWNLOAD, -1, 400,
						() -> new DocumentRecordActionBehaviors(collection, appLayerFactory).download(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_DELETE.name())) {
				boolean isDeletePossible = documentRecordActionsServices.isDeleteActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_DELETE, isDeletePossible,
						"DocumentContextMenu.deleteDocument", FontAwesome.TRASH_O, -1, 500,
						() -> new DocumentRecordActionBehaviors(collection, appLayerFactory).delete(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_COPY.name())) {
				boolean isCopyActionPossible = documentRecordActionsServices.isCopyActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_COPY, isCopyActionPossible,
						"DocumentContextMenu.copyContent", FontAwesome.COPY, -1, 600,
						() -> new DocumentRecordActionBehaviors(collection, appLayerFactory).edit(params)));
			}

		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			// TODO
		} else {
			// TODO
			return Collections.emptyList();
		}

		addMenuItemActionsFromExtensions(record, user, menuItemActions);

		return menuItemActions;
	}

	public List<MenuItemAction> getActionsForRecords(List<Record> records, User user) {
		return null;
	}

	public List<MenuItemAction> getActionsForRecords(LogicalSearchQuery query, User user) {
		return null;
	}

	public MenuItemActionState getStateForAction(MenuItemAction action, Record record, User user) {
		return null;
	}

	public MenuItemActionState getStateForAction(MenuItemAction action, List<Record> records, User user) {
		return null;
	}

	public MenuItemActionState getStateForAction(MenuItemAction action, LogicalSearchQuery query, User user) {
		return null;
	}

	private MenuItemAction buildMenuItemAction(MenuItemActionType type, boolean possible, String caption,
											   Resource icon, int group, int priority, Runnable command) {
		return buildMenuItemAction(type, possible, null, caption, icon, group, priority, command);
	}

	private MenuItemAction buildMenuItemAction(MenuItemActionType type, boolean possible, String reason, String caption,
											   Resource icon, int group, int priority, Runnable command) {
		return MenuItemAction.builder()
				.type(type.name())
				.state(toState(possible, reason))
				.reason(reason)
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.build();
	}

	private MenuItemActionState toState(boolean possible, String reason) {
		return possible ? MenuItemActionState.VISIBLE :
			   (reason != null ? MenuItemActionState.DISABLED : MenuItemActionState.HIDDEN);
	}

	private void addMenuItemActionsFromExtensions(Record record, User user, List<MenuItemAction> menuItemActions) {
		for (MenuItemActionExtension menuItemActionExtension : rmModuleExtensions.getMenuItemActionExtensions()) {
			menuItemActionExtension.addMenuItemActions(
					new MenuItemActionExtensionAddMenuItemActionsParams(record, user, menuItemActions));
		}


	}

}
