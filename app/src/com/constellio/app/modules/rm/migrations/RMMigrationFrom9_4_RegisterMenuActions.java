package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.extensions.app.RMMenuItemActionsRequestTaskExtension.RequestTypeMenuItem;
import com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType;
import com.constellio.app.modules.rm.services.menu.DocumentMenuItemServices.DocumentMenuItemActionType;
import com.constellio.app.modules.rm.services.menu.FolderMenuItemServices.FolderMenuItemActionType;
import com.constellio.app.modules.rm.services.menu.RMRecordsMenuItemServices.RMRecordsMenuItemActionType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.actionDisplayManager.MenuDisplayItem;
import com.constellio.app.services.actionDisplayManager.MenuPositionActionOptions;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction;
import com.constellio.app.services.actionDisplayManager.MenusDisplayTransaction.Action;
import com.constellio.app.services.factories.AppLayerFactory;
import com.vaadin.server.FontAwesome;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.constellio.app.modules.rm.extensions.app.RMDecommissioningBuilderMenuItemActionsExtension.RMRECORDS_CREATE_DECOMMISSIONING_LIST;
import static com.constellio.app.services.menu.MenuItemServices.BATCH_ACTIONS_FAKE_SCHEMA_TYPE;


public class RMMigrationFrom9_4_RegisterMenuActions implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.4";
	}


	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		MenusDisplayTransaction transaction = new MenusDisplayTransaction();
		addDocumentActionsToTransaction(transaction);
		addFolderActionsToTransaction(transaction);
		addContainerActionsToTransaction(transaction);
		addBatchActionsToTransaction(transaction);
		appLayerFactory.getMenusDisplayManager().execute(collection, transaction);
	}

	private void addContainerActionsToTransaction(MenusDisplayTransaction transaction) {
		addActionsToTransaction(transaction, ContainerRecord.SCHEMA_TYPE, Action.ADD_UPDATE, MenuPositionActionOptions::displayActionAtEnd,
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_CONSULT.name(), FontAwesome.SEARCH.name(), "DisplayContainerView.consult", true, null, true),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_EDIT.name(), FontAwesome.EDIT.name(), "DisplayContainerView.edit", true, null, true),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_SLIP.name(), FontAwesome.PRINT.name(), "DisplayContainerView.slip", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_LABELS.name(), FontAwesome.PRINT.name(), "SearchView.printLabels", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_ADD_TO_CART.name(), FontAwesome.STAR.name(), "DisplayContainerView.addToCart", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_CONSULT_LINK.name(), FontAwesome.LINK.name(), "consultationLink", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_BORROW.name(), FontAwesome.LOCK.name(), "DisplayFolderView.borrow", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_CHECK_IN.name(), FontAwesome.UNLOCK.name(), "DisplayContainerView.checkIn", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_RETURN_REMAINDER.name(), FontAwesome.PAPER_PLANE.name(), "SendReturnReminderEmailButton.reminderReturn", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_GENERATE_REPORT.name(), FontAwesome.PRINT.name(), "SearchView.metadataReportTitle", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_ADD_TO_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "addToOrRemoveFromSelection.add", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_REMOVE_FROM_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "addToOrRemoveFromSelection.remove", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_EMPTY_THE_BOX.name(), FontAwesome.DROPBOX.name(), "DisplayContainerView.empty", true, null, false),
				new MenuDisplayItem(ContainerRecordMenuItemActionType.CONTAINER_DELETE.name(), FontAwesome.TRASH_O.name(), "DisplayContainerView.delete", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.REQUEST_BORROW_BUTTON.name(), FontAwesome.LOCK.name(), "RMRequestTaskButtonExtension.borrowRequest", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.REACTIVATION_BUTTON.name(), FontAwesome.REFRESH.name(), "RMRequestTaskButtonExtension.reactivationRequest", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.REQUEST_BORROW_EXTENSION_BUTTON.name(), FontAwesome.LOCK.name(), "RMRequestTaskButtonExtension.borrowExtensionRequest", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.RETURN_REQUEST_BUTTON.name(), FontAwesome.UNLOCK.name(), "RMRequestTaskButtonExtension.returnRequest", true, null, true)
		);
	}

	private void addFolderActionsToTransaction(MenusDisplayTransaction transaction) {
		addActionsToTransaction(transaction, Folder.SCHEMA_TYPE, Action.ADD_UPDATE, MenuPositionActionOptions::displayActionAtEnd,
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_DISPLAY.name(), FontAwesome.SEARCH.name(), "DisplayFolderView.displayFolder", true, null, true),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_EDIT.name(), FontAwesome.EDIT.name(), "DisplayFolderView.editFolder", true, null, true),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_ADD_SUBFOLDER.name(), FontAwesome.FOLDER_O.name(), "DisplayFolderView.addSubFolder", true, null, true),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_ADD_DOCUMENT.name(), FontAwesome.FILE_O.name(), "DisplayFolderView.addDocument", true, null, true),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_CONSULT_LINK.name(), FontAwesome.LINK.name(), "consultationLink", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_MOVE.name(), FontAwesome.FOLDER_OPEN_O.name(), "DisplayFolderView.parentFolder", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_COPY.name(), FontAwesome.CLIPBOARD.name(), "DisplayFolderView.duplicateFolder", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_SHARE.name(), FontAwesome.SHARE_SQUARE_O.name(), "DisplayFolderView.shareFolder", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_UNSHARE.name(), FontAwesome.UNDO.name(), "DisplayFolderView.unshareFolder", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_ADD_TO_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "addToOrRemoveFromSelection.add", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_REMOVE_FROM_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "addToOrRemoveFromSelection.remove", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_ADD_TO_CART.name(), FontAwesome.STAR.name(), "DisplayFolderView.addToCart", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_BORROW.name(), FontAwesome.LOCK.name(), "DisplayFolderView.borrow", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_RETURN.name(), FontAwesome.UNLOCK.name(), "DisplayFolderView.returnFolder", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_AVAILABLE_ALERT.name(), FontAwesome.BELL_O.name(), "RMObject.alertWhenAvailable", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_RETURN_REMAINDER.name(), FontAwesome.PAPER_PLANE.name(), "SendReturnReminderEmailButton.reminderReturn", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_PRINT_LABEL.name(), FontAwesome.PRINT.name(), "DisplayFolderView.printLabel", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_GENERATE_REPORT.name(), FontAwesome.PRINT.name(), "SearchView.metadataReportTitle", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_CREATE_TASK.name(), FontAwesome.TASKS.name(), "DisplayFolderView.createTask", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_TRIGGER_MANAGEMENT.name(), FontAwesome.COGS.name(), "DisplayFolderView.recordTriggerManager", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_LIST_EXTERNAL_LINKS.name(), FontAwesome.CLOUD.name(), "DisplayFolderView.externalLink", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_VIEW_OR_MANAGE_AUTHORIZATION.name(), FontAwesome.KEY.name(), "DisplayFolderView.addAuthorization", true, null, false),
				new MenuDisplayItem(FolderMenuItemActionType.FOLDER_DELETE.name(), FontAwesome.TRASH_O.name(), "DisplayFolderView.deleteFolder", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.REQUEST_BORROW_BUTTON.name(), FontAwesome.LOCK.name(), "RMRequestTaskButtonExtension.borrowRequest", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.REACTIVATION_BUTTON.name(), FontAwesome.REFRESH.name(), "RMRequestTaskButtonExtension.reactivationRequest", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.REQUEST_BORROW_EXTENSION_BUTTON.name(), FontAwesome.LOCK.name(), "RMRequestTaskButtonExtension.borrowExtensionRequest", true, null, true),
				new MenuDisplayItem(RequestTypeMenuItem.RETURN_REQUEST_BUTTON.name(), FontAwesome.UNLOCK.name(), "RMRequestTaskButtonExtension.returnRequest", true, null, true)
		);
	}


	private void addDocumentActionsToTransaction(MenusDisplayTransaction transaction) {
		addActionsToTransaction(transaction, Document.SCHEMA_TYPE, Action.ADD_UPDATE, MenuPositionActionOptions::displayActionAtEnd,
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_DISPLAY.name(), FontAwesome.SEARCH.name(), "DisplayDocumentView.displayDocument", true, null, true),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_OPEN.name(), FontAwesome.FILE_O.name(), "DisplayDocumentView.openDocument", true, null, true),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_EDIT.name(), FontAwesome.EDIT.name(), "DisplayDocumentView.editDocument", true, null, true),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_CHECK_OUT.name(), FontAwesome.LOCK.name(), "DocumentContextMenu.checkOut", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_CHECK_IN.name(), FontAwesome.UNLOCK.name(), "DocumentContextMenu.checkIn", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_RETURN_REMAINDER.name(), FontAwesome.PAPER_PLANE.name(), "SendReturnReminderEmailButton.reminderReturn", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_AVAILABLE_ALERT.name(), FontAwesome.BELL_O.name(), "DocumentContextMenu.alertWhenAvailable", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_CONSULT_LINK.name(), FontAwesome.LINK.name(), "consultationLink", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_DOWNLOAD.name(), FontAwesome.DOWNLOAD.name(), "DocumentContextMenu.downloadDocument", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_MOVE.name(), FontAwesome.FOLDER_O.name(), "DocumentContextMenu.changeParentFolder", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_GET_PUBLIC_LINK.name(), FontAwesome.LAPTOP.name(), "DocumentActionsComponent.linkToDocument", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_PUBLISH.name(), FontAwesome.GLOBE.name(), "DocumentContextMenu.publish", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_UNPUBLISH.name(), FontAwesome.GLOBE.name(), "DocumentContextMenu.unpublish", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_PRINT_LABEL.name(), FontAwesome.PRINT.name(), "DisplayFolderView.printLabel", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_GENERATE_REPORT.name(), FontAwesome.LIST_ALT.name(), "DocumentContextMenu.ReportGeneratorButton", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_ADD_TO_CART.name(), FontAwesome.STAR.name(), "DisplayFolderView.addToCart", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_COPY.name(), FontAwesome.PASTE.name(), "DocumentContextMenu.copyContent", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_RENAME_CONTENT.name(), FontAwesome.EDIT.name(), "DocumentContextMenu.renameContent", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_FINALIZE.name(), FontAwesome.LEVEL_UP.name(), "DocumentContextMenu.finalize", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_UPLOAD.name(), FontAwesome.UPLOAD.name(), "DocumentContextMenu.upload", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_CREATE_PDF.name(), FontAwesome.FILE_PDF_O.name(), "DocumentContextMenu.createPDF", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_SHARE_DOCUMENT.name(), FontAwesome.PAPER_PLANE_O.name(), "DocumentContextMenu.shareDocument", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_UNSHARE.name(), FontAwesome.REPLY.name(), "DocumentContextMenu.unshareDocument", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_ADD_TASK.name(), FontAwesome.TASKS.name(), "DocumentContextMenu.createTask", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_ADD_TO_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "DocumentContextMenu.addToSelection", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_REMOVE_TO_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "DocumentContextMenu.removeToSelection", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_EXTRACT_ATTACHEMENTS.name(), FontAwesome.FILES_O.name(), "DocumentContextMenu.extractEmailAttachement", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_SIGNATURE_REQUEST.name(), FontAwesome.PENCIL_SQUARE.name(), "DocumentContextMenu.signatureRequest", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_VIEW_OR_MANAGE_AUTHORIZATIONS.name(), FontAwesome.KEY.name(), "DocumentContextMenu.manageAuthorizations", true, null, false),
				new MenuDisplayItem(DocumentMenuItemActionType.DOCUMENT_DELETE.name(), FontAwesome.TRASH_O.name(), "DocumentContextMenu.deleteDocument", true, null, true)
		);
	}

	private void addBatchActionsToTransaction(MenusDisplayTransaction transaction) {
		addActionsToTransaction(transaction, BATCH_ACTIONS_FAKE_SCHEMA_TYPE, Action.ADD_UPDATE, MenuPositionActionOptions::displayActionAtEnd,
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_ADD_CART.name(), FontAwesome.STAR.name(), "ConstellioHeader.selection.actions.addToCart", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_MOVE.name(), FontAwesome.FOLDER_O.name(), "ConstellioHeader.selection.actions.moveInFolder", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_COPY.name(), FontAwesome.CLIPBOARD.name(), "ConstellioHeader.selection.actions.duplicate", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_CREATE_SIP.name(), FontAwesome.FILE_ARCHIVE_O.name(), "SIPButton.caption", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_SEND_EMAIL.name(), FontAwesome.ENVELOPE_O.name(), "ConstellioHeader.selection.actions.prepareEmail", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_CONSULT_LINK.name(), FontAwesome.LINK.name(), "consultationLink", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_CREATE_PDF.name(), FontAwesome.FILE_PDF_O.name(), "ConstellioHeader.selection.actions.pdf", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_PRINT_LABEL.name(), FontAwesome.PRINT.name(), "SearchView.printLabels", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_BORROW.name(), FontAwesome.LOCK.name(), "DocumentContextMenu.checkOut", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_BORROW_REQUEST.name(), FontAwesome.LOCK.name(), "RMRequestTaskButtonExtension.borrowRequest", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_RETURN.name(), FontAwesome.UNLOCK.name(), "DocumentContextMenu.checkIn", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_CANCEL_RETURN.name(), FontAwesome.UNLOCK.name(), "DocumentContextMenu.cancelCheckOut", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_RETURN_REQUEST.name(), FontAwesome.UNLOCK.name(), "RMRequestTaskButtonExtension.returnRequest", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_RETURN_REMAINDER.name(), FontAwesome.PAPER_PLANE.name(), "SendReturnReminderEmailButton.reminderReturn", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_ADD_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "SearchView.addToSelection", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_REMOVE_SELECTION.name(), FontAwesome.SHOPPING_BASKET.name(), "SearchView.removeFromSelection", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP.name(), FontAwesome.FILE_ARCHIVE_O.name(), "MenuDisplayConfigViewImpl.RMRecordsMenuItemActionType.RMRECORDS_DOWNLOAD_ZIP.default", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_BATCH_DELETE.name(), FontAwesome.TRASH_O.name(), "delete", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_CREATE_TASK.name(), FontAwesome.TASKS.name(), "ConstellioHeader.selection.actions.createTask", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_BATCH_UNSHARE.name(), FontAwesome.REPLY.name(), "DocumentContextMenu.batchunshare", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_CHECKIN.name(), FontAwesome.UNLOCK.name(), "DocumentContextMenu.checkIn", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_BATCH_UNPUBLISH.name(), FontAwesome.GLOBE.name(), "DocumentContextMenu.batchunPublish", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_GENERATE_REPORT.name(), FontAwesome.LIST_ALT.name(), "DocumentContextMenu.ReportGeneratorButton", true, null, false),
				new MenuDisplayItem(RMRecordsMenuItemActionType.RMRECORDS_PUT_IN_CONTAINER.name(), FontAwesome.ARCHIVE.name(), "ContainersButton.containerAssigner", true, null, false),
				new MenuDisplayItem(RMRECORDS_CREATE_DECOMMISSIONING_LIST, FontAwesome.ARCHIVE.name(), "DecommissioningBuilderView.createDecommissioningList", true, null, false)

		);
	}

	private void addActionsToTransaction(MenusDisplayTransaction transaction, String schemaType, Action action,
										 Supplier<MenuPositionActionOptions> optionsSupplier,
										 MenuDisplayItem... menuDisplayItems) {
		Stream.of(menuDisplayItems).forEach(menuDisplayItem -> transaction.addElement(action, schemaType, menuDisplayItem, optionsSupplier.get()));
	}
}
