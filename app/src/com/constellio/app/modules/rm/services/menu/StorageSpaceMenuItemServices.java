package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.actions.StorageSpaceRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.StorageSpaceMenuItemActionBehaviors;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
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

import static com.constellio.app.modules.rm.services.menu.StorageSpaceMenuItemServices.StorageSpaceMenuItemActionType.STORAGE_SPACE_CONSULT;
import static com.constellio.app.modules.rm.services.menu.StorageSpaceMenuItemServices.StorageSpaceMenuItemActionType.STORAGE_SPACE_CONSULT_LINK;
import static com.constellio.app.modules.rm.services.menu.StorageSpaceMenuItemServices.StorageSpaceMenuItemActionType.STORAGE_SPACE_DELETE;
import static com.constellio.app.modules.rm.services.menu.StorageSpaceMenuItemServices.StorageSpaceMenuItemActionType.STORAGE_SPACE_EDIT;
import static com.constellio.app.modules.rm.services.menu.StorageSpaceMenuItemServices.StorageSpaceMenuItemActionType.STORAGE_SPACE_GENERATE_REPORT;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class StorageSpaceMenuItemServices {

	private StorageSpaceRecordActionsServices storageSpaceRecordActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	public StorageSpaceMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		storageSpaceRecordActionsServices = new StorageSpaceRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(StorageSpace storageSpace, User user,
													List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(STORAGE_SPACE_CONSULT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(STORAGE_SPACE_CONSULT.name(),
					isMenuItemActionPossible(STORAGE_SPACE_CONSULT.name(), storageSpace, user, params),
					$("StorageSpaceMenuItemServices.consult"), FontAwesome.SEARCH, -1, 100,
					(ids) -> new StorageSpaceMenuItemActionBehaviors(collection, appLayerFactory).consult(storageSpace, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(STORAGE_SPACE_EDIT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(STORAGE_SPACE_EDIT.name(),
					isMenuItemActionPossible(STORAGE_SPACE_EDIT.name(), storageSpace, user, params),
					$("StorageSpaceMenuItemServices.edit"), FontAwesome.EDIT, -1, 150,
					(ids) -> new StorageSpaceMenuItemActionBehaviors(collection, appLayerFactory).edit(storageSpace, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(STORAGE_SPACE_CONSULT_LINK.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(STORAGE_SPACE_CONSULT_LINK.name(),
					isMenuItemActionPossible(STORAGE_SPACE_CONSULT_LINK.name(), storageSpace, user, params),
					$("consultationLink"), FontAwesome.LINK, -1, 510,
					(ids) -> new StorageSpaceMenuItemActionBehaviors(collection, appLayerFactory).getConsultationLink(storageSpace, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(STORAGE_SPACE_GENERATE_REPORT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(STORAGE_SPACE_GENERATE_REPORT.name(),
					isMenuItemActionPossible(STORAGE_SPACE_GENERATE_REPORT.name(), storageSpace, user, params),
					$("SearchView.metadataReportTitle"), null, -1, 700,
					(ids) -> new StorageSpaceMenuItemActionBehaviors(collection, appLayerFactory).generateReport(storageSpace, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(STORAGE_SPACE_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(STORAGE_SPACE_DELETE.name(),
					isMenuItemActionPossible(STORAGE_SPACE_DELETE.name(), storageSpace, user, params),
					$("StorageSpaceMenuItemServices.delete"), FontAwesome.TRASH_O, -1, Integer.MAX_VALUE,
					(ids) -> new StorageSpaceMenuItemActionBehaviors(collection, appLayerFactory).delete(storageSpace, params));

			menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, StorageSpace storageSpace, User user,
											MenuItemActionBehaviorParams params) {
		Record record = storageSpace.getWrappedRecord();

		switch (StorageSpaceMenuItemActionType.valueOf(menuItemActionType)) {
			case STORAGE_SPACE_CONSULT:
				return storageSpaceRecordActionsServices.isDisplayActionPossible(record, user);
			case STORAGE_SPACE_EDIT:
				return storageSpaceRecordActionsServices.isEditActionPossible(record, user);
			case STORAGE_SPACE_DELETE:
				return storageSpaceRecordActionsServices.isDeleteActionPossible(record, user);
			case STORAGE_SPACE_CONSULT_LINK:
				return storageSpaceRecordActionsServices.isConsultLinkActionPossible(record, user);
			case STORAGE_SPACE_GENERATE_REPORT:
				return storageSpaceRecordActionsServices.isGenerateReportActionPossible(record, user);
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

	public enum StorageSpaceMenuItemActionType {
		STORAGE_SPACE_CONSULT,
		STORAGE_SPACE_EDIT,
		STORAGE_SPACE_DELETE,
		STORAGE_SPACE_GENERATE_REPORT,
		STORAGE_SPACE_CONSULT_LINK;
	}

}
