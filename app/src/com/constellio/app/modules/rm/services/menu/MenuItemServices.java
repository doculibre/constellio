package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.extensions.api.MenuItemActionExtension;
import com.constellio.app.modules.rm.extensions.api.MenuItemActionExtension.MenuItemActionExtensionAddMenuItemActionsParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.actions.FolderRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.DocumentMenuItemActionBehaviors;
import com.constellio.app.modules.rm.services.menu.behaviors.FolderMenuItemActionBehaviors;
import com.constellio.app.modules.rm.services.menu.behaviors.MenuItemActionBehaviorParams;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
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
	private FolderRecordActionsServices folderRecordActionsServices;
	private RMModuleExtensions rmModuleExtensions;
	private RMSchemasRecordsServices rm;

	public MenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
		folderRecordActionsServices = new FolderRecordActionsServices(collection, appLayerFactory);
		rmModuleExtensions = appLayerFactory.getExtensions().forCollection(collection).forModule(ConstellioRMModule.ID);
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, MenuItemActionBehaviorParams params) {
		return getActionsForRecord(record, Collections.emptyList(), params);
	}

	public List<MenuItemAction> getActionsForRecord(Record record, List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		User user = params.getUser();

		List<MenuItemAction> menuItemActions = new ArrayList<>();
		if (record.isOfSchemaType(Document.SCHEMA_TYPE)) {
			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_DISPLAY.name())) {
				boolean isDisplayActionPossible = documentRecordActionsServices.isDisplayActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_DISPLAY, isDisplayActionPossible,
						"DisplayDocumentView.displayDocument", FontAwesome.FILE_O, -1, 100,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).display(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_OPEN.name())) {
				boolean isOpenActionPossible = documentRecordActionsServices.isOpenActionPossible(record, user);
				// FIXME better way? get icon by mime-type?
				Document document = rm.wrapDocument(record);
				Resource icon = FileIconUtils.getIcon(document.getContent() != null ?
													  document.getContent().getCurrentVersion().getFilename() : "");
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_OPEN, isOpenActionPossible,
						"DisplayDocumentView.openDocument", icon, -1, 200,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).open(params)));
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_EDIT.name())) {
				boolean isEditActionPossible = documentRecordActionsServices.isEditActionPossible(record, user);

				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_EDIT, isEditActionPossible,
						"DisplayDocumentView.editDocument", FontAwesome.EDIT, -1, 250,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).edit(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_EDIT.name())) {
				boolean isCopyActionPossible = documentRecordActionsServices.isCopyActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_EDIT, isCopyActionPossible,
						"DocumentContextMenu.editDocument", FontAwesome.EDIT, -1, 600,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).edit(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_DOWNLOAD.name())) {
				boolean isDownloadPossible = documentRecordActionsServices.isDownloadActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_DOWNLOAD, isDownloadPossible,
						"DocumentContextMenu.downloadDocument", FontAwesome.DOWNLOAD, -1, 400,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).download(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_DELETE.name())) {
				boolean isDeletePossible = documentRecordActionsServices.isDeleteActionPossible(record, user);
				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_DELETE, isDeletePossible,
						"DocumentContextMenu.deleteDocument", FontAwesome.TRASH_O, -1, 500,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).delete(params));

				menuItemAction.setConfirmMessage("ConfirmDialog.confirmDelete");

				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_COPY.name())) {
				boolean isEditActionPossible = documentRecordActionsServices.isEditActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.DOCUMENT_COPY, isEditActionPossible,
						"DocumentContextMenu.copyContent", FontAwesome.COPY, -1, 300,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).copy(params)));
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_FINALIZE.name())) {
				boolean isFinalisationPossible = documentRecordActionsServices.isFinalizeActionPossible(record, user);
				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_FINALIZE, isFinalisationPossible,
						"DocumentContextMenu.finalize", FontAwesome.LEVEL_UP, -1, 700,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).finalize(params));

				menuItemAction.setConfirmMessage("DocumentActionsComponent.finalize.confirm");
				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_PUBLISH.name())) {
				boolean isPublishPossible = documentRecordActionsServices.isPublishActionPossible(record, user);
				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_PUBLISH, isPublishPossible,
							"DocumentContextMenu.publish", null, -1, 800,
							() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).publish(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_UNPUBLISH.name())) {
				boolean isUnPublishActionPossible = documentRecordActionsServices.isUnPublishActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_UNPUBLISH, isUnPublishActionPossible,
						"DocumentContextMenu.unpublish", null,-1 ,800,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).unPublish(params));

				menuItemActions.add(menuItemAction);
			}


			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_CREATE_PDF.name())) {
				boolean isCreatePdfActionPossible = documentRecordActionsServices.isCreatePdfActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_CREATE_PDF, isCreatePdfActionPossible,
						"DocumentContextMenu.createPDFA", null, -1, 900,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).createPdf(params));
				menuItemAction.setConfirmMessage("ConfirmDialog.confirmCreatePDFA");

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_ADD_TO_SELECTION.name())) {
				SessionContext sessionContext = params.getView().getSessionContext();

				boolean isAddToSelectionPossible = documentRecordActionsServices.isAddToSelectionActionPossible(record, user,
						params.getView().getSessionContext())
						&& (sessionContext.getSelectedRecordIds() == null
							|| !sessionContext.getSelectedRecordIds().contains(record.getId()));

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_ADD_TO_SELECTION,
						isAddToSelectionPossible, "addToOrRemoveFromSelection.add", null, -1, 1000,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addToSelection(params));

				menuItemActions.add(menuItemAction);
			}


			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_REMOVE_TO_SELECTION.name())) {
				SessionContext sessionContext = params.getView().getSessionContext();
				boolean isRemoveToSelectionPossible = documentRecordActionsServices.isRemoveToSelectionActionPossible(record, user)
													  && sessionContext.getSelectedRecordIds()!= null
													  && sessionContext.getSelectedRecordIds().contains(record.getId());

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_ADD_TO_SELECTION,
						isRemoveToSelectionPossible, "addToOrRemoveFromSelection.remove", null, -1, 1100,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).removeToSelection(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_ADD_TO_CART.name())) {
				boolean isAddCartActionPossible = documentRecordActionsServices.isAddCartActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_ADD_TO_CART,
						isAddCartActionPossible, "DisplayFolderView.addToCart", null, -1, 1200,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addToCart(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_ADD_TO_MY_CART.name())) {
				boolean isAddToMyDefaultCartActionPossible = documentRecordActionsServices.isAddToMyCartActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_ADD_TO_MY_CART,
						isAddToMyDefaultCartActionPossible, "DisplayFolderView.addToMyCart", null, -1, 1200,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addToDefaultCart(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_UPLOAD.name())) {
				boolean isAddCartActionPossible = documentRecordActionsServices.isUploadActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_UPLOAD,
						isAddCartActionPossible, "DocumentContextMenu.upload", null, -1, 1200,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).upload(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_PRINT_LABEL.name())) {
				boolean isPrintLabelPossible = documentRecordActionsServices.isPrintLabelActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_PRINT_LABEL,
						isPrintLabelPossible, "DisplayFolderView.printLabel", null, -1, 1300,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).printLabel(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_CHECK_IN.name())) {
				boolean isCheckInActionPossible = documentRecordActionsServices.isCheckInActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_CHECK_IN,
						isCheckInActionPossible, "DocumentContextMenu.checkIn", null, -1, 1400,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).checkIn(params));

				menuItemActions.add(menuItemAction);
			}

			if(!filteredActionTypes.contains(MenuItemActionType.DOCUMENT_CHECK_OUT.name())) {
				boolean isCheckInActionPossible = documentRecordActionsServices.isCheckOutActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_CHECK_OUT,
						isCheckInActionPossible, "DocumentContextMenu.checkOut", null, -1, 1400,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).checkOut(params));

				menuItemActions.add(menuItemAction);
			}

			if (filteredActionTypes.contains(MenuItemActionType.DOCUMENT_ADD_AUTHORIZATION.name())) {
				boolean isAddAuthorizationPossible = documentRecordActionsServices.isAddAuthorizationActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_ADD_AUTHORIZATION,
						isAddAuthorizationPossible, "DocumentContextMenu.addAuthorization", null, -1, 1500,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addAuthorization(params));

				menuItemActions.add(menuItemAction);
			}

			if (filteredActionTypes.contains(MenuItemActionType.DOCUMENT_GENERATE_REPORT.name())) {
				boolean isGenerateReportPossible = documentRecordActionsServices.isGenerateReportActionPossible(record, user);

				MenuItemAction menuItemAction = buildMenuItemAction(MenuItemActionType.DOCUMENT_ADD_AUTHORIZATION,
						isGenerateReportPossible, "DocumentContextMenu.ReportGeneratorButton", null, -1, 1600,
						() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).reportGeneratorButton(params));

				menuItemActions.add(menuItemAction);
			}

		} else if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_ADD_DOCUMENT.name())) {
				boolean isAddDocumentActionPossible = folderRecordActionsServices.isAddDocumentActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_ADD_DOCUMENT, isAddDocumentActionPossible,
						"DisplayFolderView.addDocument", null, -1, 100,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addToDocument(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_MOVE.name())) {
				boolean isMoveActionPossible = folderRecordActionsServices.isMoveActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_MOVE, isMoveActionPossible,
						"DisplayFolderView.parentFolder", null, -1, 200,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).move(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_ADD_SUBFOLDER.name())) {
				boolean isAddSubFolderActionPossible = folderRecordActionsServices.isAddSubFolderActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_ADD_SUBFOLDER, isAddSubFolderActionPossible,
						"DisplayFolderView.addSubFolder", null, -1, 300,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addSubFolder(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_DISPLAY.name())) {
				boolean isDisplayActionPossible = folderRecordActionsServices.isDisplayActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_DISPLAY, isDisplayActionPossible,
						"DisplayFolderView.displayFolder", null, -1, 400,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).display(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_EDIT.name())) {
				boolean isEditActionPossible = folderRecordActionsServices.isEditActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_EDIT, isEditActionPossible,
						"DisplayFolderView.editFolder", null, -1, 500,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).edit(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_DELETE.name())) {
				boolean isDeletePossible = folderRecordActionsServices.isDeleteActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_DELETE, isDeletePossible,
						"DisplayFolderView.deleteFolder", null, -1, 600,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).delete(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_COPY.name())) {
				boolean isDuplicatePossible = folderRecordActionsServices.isDuplicateActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_COPY, isDuplicatePossible,
						"DisplayFolderView.duplicateFolder", null, -1, 700,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).copy(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_ADD_AUTHORIZATION.name())) {
				boolean isAddAuthorizationPossible = folderRecordActionsServices.isAddAuthorizationActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_ADD_AUTHORIZATION, isAddAuthorizationPossible,
						"DisplayFolderView.addAuthorization", null, -1, 800,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).addAuthorization(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_SHARE.name())) {
				boolean isSharePossible = folderRecordActionsServices.isShareActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_SHARE, isSharePossible,
						"DisplayFolderView.shareFolder", null, -1, 900,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).share(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_ADD_TO_CART.name())) {
				boolean isAddToCartPossible = folderRecordActionsServices.isAddToCartActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_ADD_TO_CART, isAddToCartPossible,
						"DisplayFolderView.addToCart", null, -1, 1000,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).share(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_BORROW.name())) {
				boolean isBorrowPossible = folderRecordActionsServices.isBorrowActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_BORROW, isBorrowPossible,
						"DisplayFolderView.borrow", null, -1, 1100,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).borrow(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_RETURN.name())) {
				boolean isReturnPossible = folderRecordActionsServices.isReturnActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_BORROW, isReturnPossible,
						"DisplayFolderView.returnFolder", null, -1, 1200,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).returnFolder(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_RETURN.name())) {
				boolean isReturnPossible = folderRecordActionsServices.isReturnActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_RETURN, isReturnPossible,
						"DisplayFolderView.returnFolder", null, -1, 1300,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).returnFolder(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_RETURN_REMAINDER.name())) {
				Folder folder = rm.wrapFolder(record);
				boolean borrowedByOtherUser = folder.getBorrowed() &&
											  !user.getId().equals(folder.getBorrowUserEntered());
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_RETURN_REMAINDER, borrowedByOtherUser,
						"DisplayFolderView.reminderReturnFolder", null, -1, 1400,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).sendReturnRemainder(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_AVAILABLE_ALERT.name())) {
				Folder folder = rm.wrapFolder(record);
				boolean borrowedByOtherUser = folder.getBorrowed() &&
											  !user.getId().equals(folder.getBorrowUserEntered());
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_AVAILABLE_ALERT, borrowedByOtherUser,
						"DisplayFolderView.alertWhenAvailable", null, -1, 1500,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).sendAvailableAlert(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_PRINT_LABEL.name())) {
				boolean isPrintPossible = folderRecordActionsServices.isPrintLabelActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_PRINT_LABEL, isPrintPossible,
						"DisplayFolderView.printLabel", null, -1, 1600,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).printLabel(params)));
			}

			if (!filteredActionTypes.contains(MenuItemActionType.FOLDER_GENERATE_REPORT.name())) {
				boolean isGenerateReportPossible = folderRecordActionsServices.isGenerateReportActionPossible(record, user);
				menuItemActions.add(buildMenuItemAction(MenuItemActionType.FOLDER_GENERATE_REPORT, isGenerateReportPossible,
						"DisplayFolderView.metadataReportTitle", null, -1, 1700,
						() -> new FolderMenuItemActionBehaviors(collection, appLayerFactory).printLabel(params)));
			}

			// FIXME une autre possibilité est d'avoir MenuItemAction.button et faire en sorte que le runnable fasse un button.click?
			// Ça éviterait de reconstruire le bouton à chaque fois

		} else {
			// TODO
			return Collections.emptyList();
		}

		addMenuItemActionsFromExtensions(record, user, params.getView(), menuItemActions);

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
											   Resource icon, int group, int priority, Class buttonClass) {
		return buildMenuItemAction(type, possible, null, caption, icon, group, priority, buttonClass, null);
	}

	private MenuItemAction buildMenuItemAction(MenuItemActionType type, boolean possible, String caption,
											   Resource icon, int group, int priority, Runnable command) {
		return buildMenuItemAction(type, possible, null, caption, icon, group, priority, null, command);
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

}
