package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.FolderMenuItemActionBehaviors;
import com.constellio.app.modules.rm.wrappers.Folder;
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

import static com.constellio.app.modules.rm.services.menu.FolderMenuItemServices.FolderMenuItemActionType.*;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class FolderMenuItemServices {

	private FolderRecordActionsServices folderRecordActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	//	private static final Resource SELECTION_ICON_RESOURCE = new ThemeResource("images/icons/clipboard_12x16.png");
	private static final Resource SELECTION_ICON_RESOURCE = FontAwesome.SHOPPING_BASKET;

	public FolderMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Folder folder, User user, List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(FOLDER_ADD_DOCUMENT.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_ADD_DOCUMENT.name(),
					isMenuItemActionPossible(FOLDER_ADD_DOCUMENT.name(), folder, user, params),
					$("DisplayFolderView.addDocument"), FontAwesome.FILE_O, -1, 100,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addToDocument(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_MOVE.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_MOVE.name(),
					isMenuItemActionPossible(FOLDER_MOVE.name(), folder, user, params),
					$("DisplayFolderView.parentFolder"), null, -1, 200,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).move(folder, params)));
		}


		if (!filteredActionTypes.contains(FOLDER_ADD_SUBFOLDER.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_ADD_SUBFOLDER.name(),
					isMenuItemActionPossible(FOLDER_ADD_SUBFOLDER.name(), folder, user, params),
					$("DisplayFolderView.addSubFolder"), null, -1, 300,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addSubFolder(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_DISPLAY.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_DISPLAY.name(),
					isMenuItemActionPossible(FOLDER_DISPLAY.name(), folder, user, params),
					$("DisplayFolderView.displayFolder"), FontAwesome.FILE_O, -1, 400,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).display(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_EDIT.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_EDIT.name(),
					isMenuItemActionPossible(FOLDER_EDIT.name(), folder, user, params),
					$("DisplayFolderView.editFolder"), FontAwesome.EDIT, -1, 500,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).edit(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_CONSULT_LINK.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(FOLDER_CONSULT_LINK.name(),
					isMenuItemActionPossible(FOLDER_CONSULT_LINK.name(), folder, user, params),
					$("consultationLink"), FontAwesome.LINK, -1, 510,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).getConsultationLink(folder, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(FOLDER_DELETE.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_DELETE.name(),
					isMenuItemActionPossible(FOLDER_DELETE.name(), folder, user, params),
					$("DisplayFolderView.deleteFolder"), FontAwesome.TRASH_O, -1, 600,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).delete(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_COPY.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_COPY.name(),
					isMenuItemActionPossible(FOLDER_COPY.name(), folder, user, params),
					$("DisplayFolderView.duplicateFolder"), null, -1, 700,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).copy(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_ADD_AUTHORIZATION.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_ADD_AUTHORIZATION.name(),
					isMenuItemActionPossible(FOLDER_ADD_AUTHORIZATION.name(), folder, user, params),
					$("DisplayFolderView.addAuthorization"), null, -1, 800,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addAuthorization(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_SHARE.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_SHARE.name(),
					isMenuItemActionPossible(FOLDER_SHARE.name(), folder, user, params),
					$("DisplayFolderView.shareFolder"), null, -1, 900,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).share(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_ADD_TO_CART.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_ADD_TO_CART.name(),
					isMenuItemActionPossible(FOLDER_ADD_TO_CART.name(), folder, user, params),
					$("DisplayFolderView.addToCart"), FontAwesome.STAR, -1, 1000,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addToCart(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_BORROW.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_BORROW.name(),
					isMenuItemActionPossible(FOLDER_BORROW.name(), folder, user, params),
					$("DisplayFolderView.borrow"), null, -1, 1100,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).borrow(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_RETURN.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_RETURN.name(),
					isMenuItemActionPossible(FOLDER_RETURN.name(), folder, user, params),
					$("DisplayFolderView.returnFolder"), null, -1, 1200,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).returnFolder(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_RETURN_REMAINDER.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_RETURN_REMAINDER.name(),
					isMenuItemActionPossible(FOLDER_RETURN_REMAINDER.name(), folder, user, params),
					$("DisplayFolderView.reminderReturnFolder"), null, -1, 1300,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).sendReturnRemainder(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_AVAILABLE_ALERT.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_AVAILABLE_ALERT.name(),
					isMenuItemActionPossible(FOLDER_AVAILABLE_ALERT.name(), folder, user, params),
					$("RMObject.alertWhenAvailable"), null, -1, 1400,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).sendAvailableAlert(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_PRINT_LABEL.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_PRINT_LABEL.name(),
					isMenuItemActionPossible(FOLDER_PRINT_LABEL.name(), folder, user, params),
					$("DisplayFolderView.printLabel"), FontAwesome.PRINT, -1, 1500,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).printLabel(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_GENERATE_REPORT.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_GENERATE_REPORT.name(),
					isMenuItemActionPossible(FOLDER_GENERATE_REPORT.name(), folder, user, params),
					$("SearchView.metadataReportTitle"), null, -1, 1600,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).generateReport(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_ADD_TO_SELECTION.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_ADD_TO_SELECTION.name(),
					isMenuItemActionPossible(FOLDER_ADD_TO_SELECTION.name(), folder, user, params),
					$("addToOrRemoveFromSelection.add"), SELECTION_ICON_RESOURCE, -1, 1700,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addToSelection(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_REMOVE_FROM_SELECTION.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_REMOVE_FROM_SELECTION.name(),
					isMenuItemActionPossible(FOLDER_REMOVE_FROM_SELECTION.name(), folder, user, params),
					$("addToOrRemoveFromSelection.remove"), SELECTION_ICON_RESOURCE, -1, 1800,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).removeFromSelection(folder, params)));
		}

		if (!filteredActionTypes.contains(FOLDER_CREATE_TASK.name())) {
			menuItemActions.add(buildMenuItemAction(FOLDER_CREATE_TASK.name(),
					isMenuItemActionPossible(FOLDER_CREATE_TASK.name(), folder, user, params),
					$("DisplayFolderView.createTask"), FontAwesome.TASKS, -1, 1900,
					(ids) -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).createTask(folder, params)));
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, Folder folder, User user,
											MenuItemActionBehaviorParams params) {
		Record record = folder.getWrappedRecord();

		switch (FolderMenuItemActionType.valueOf(menuItemActionType)) {
			case FOLDER_DISPLAY:
				return folderRecordActionsServices.isDisplayActionPossible(record, user);
			case FOLDER_ADD_DOCUMENT:
				return folderRecordActionsServices.isAddDocumentActionPossible(record, user);
			case FOLDER_CONSULT_LINK:
				return folderRecordActionsServices.isConsultLinkActionPossible(record, user);
			case FOLDER_MOVE:
				return folderRecordActionsServices.isMoveActionPossible(record, user);
			case FOLDER_ADD_SUBFOLDER:
				return folderRecordActionsServices.isAddSubFolderActionPossible(record, user);
			case FOLDER_EDIT:
				return folderRecordActionsServices.isEditActionPossible(record, user);
			case FOLDER_DELETE:
				return folderRecordActionsServices.isDeleteActionPossible(record, user);
			case FOLDER_COPY:
				return folderRecordActionsServices.isCopyActionPossible(record, user);
			case FOLDER_ADD_AUTHORIZATION:
				return folderRecordActionsServices.isAddAuthorizationActionPossible(record, user);
			case FOLDER_SHARE:
				return folderRecordActionsServices.isShareActionPossible(record, user);
			case FOLDER_ADD_TO_CART:
				return folderRecordActionsServices.isAddToCartActionPossible(record, user);
			case FOLDER_BORROW:
				return folderRecordActionsServices.isBorrowActionPossible(record, user);
			case FOLDER_RETURN:
				return folderRecordActionsServices.isReturnActionPossible(record, user);
			case FOLDER_RETURN_REMAINDER:
				return Boolean.TRUE.equals(folder.getBorrowed()) &&
					   !user.getId().equals(folder.getBorrowUserEntered());
			case FOLDER_AVAILABLE_ALERT:
				return Boolean.TRUE.equals(folder.getBorrowed()) &&
					   !user.getId().equals(folder.getBorrowUserEntered());
			case FOLDER_PRINT_LABEL:
				return folderRecordActionsServices.isPrintLabelActionPossible(record, user);
			case FOLDER_GENERATE_REPORT:
				return folderRecordActionsServices.isGenerateReportActionPossible(record, user);
			case FOLDER_ADD_TO_SELECTION:
				return params.getView() != null &&
					   (params.getView().getSessionContext().getSelectedRecordIds() == null ||
						!params.getView().getSessionContext().getSelectedRecordIds().contains(record.getId()));
			case FOLDER_REMOVE_FROM_SELECTION:
				return params.getView() != null &&
					   (params.getView().getSessionContext().getSelectedRecordIds() != null &&
						params.getView().getSessionContext().getSelectedRecordIds().contains(record.getId()));
			case FOLDER_CREATE_TASK:
				return folderRecordActionsServices.isCreateTaskActionPossible(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(String type, boolean possible, String caption,
											   Resource icon, int group, int priority, Consumer<List<String>> command) {
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

	public static enum FolderMenuItemActionType {
		FOLDER_DISPLAY,
		FOLDER_ADD_DOCUMENT,
		FOLDER_MOVE,
		FOLDER_CONSULT_LINK,
		FOLDER_ADD_SUBFOLDER,
		FOLDER_EDIT,
		FOLDER_DELETE,
		FOLDER_COPY,
		FOLDER_ADD_AUTHORIZATION,
		FOLDER_SHARE,
		FOLDER_ADD_TO_CART,
		FOLDER_BORROW,
		FOLDER_RETURN,
		FOLDER_RETURN_REMAINDER,
		FOLDER_AVAILABLE_ALERT,
		FOLDER_PRINT_LABEL,
		FOLDER_GENERATE_REPORT,
		FOLDER_ADD_TO_SELECTION,
		FOLDER_REMOVE_FROM_SELECTION,
		FOLDER_CREATE_TASK;
	}

}
