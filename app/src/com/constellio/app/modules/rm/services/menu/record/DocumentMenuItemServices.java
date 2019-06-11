package com.constellio.app.modules.rm.services.menu.record;

import com.constellio.app.modules.rm.services.actions.DocumentRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.MenuItemAction;
import com.constellio.app.modules.rm.services.menu.MenuItemActionState;
import com.constellio.app.modules.rm.services.menu.MenuItemActionType;
import com.constellio.app.modules.rm.services.menu.behaviors.DocumentMenuItemActionBehaviors;
import com.constellio.app.modules.rm.services.menu.behaviors.MenuItemActionBehaviorParams;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.util.FileIconUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_ADD_AUTHORIZATION;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_ADD_TO_CART;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_ADD_TO_SELECTION;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_CHECK_IN;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_CHECK_OUT;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_COPY;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_CREATE_PDF;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_DELETE;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_DISPLAY;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_DOWNLOAD;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_EDIT;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_FINALIZE;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_GENERATE_REPORT;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_OPEN;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_PRINT_LABEL;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_PUBLISH;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_REMOVE_TO_SELECTION;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_UNPUBLISH;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.DOCUMENT_UPLOAD;

public class DocumentMenuItemServices {

	private DocumentRecordActionsServices documentRecordActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	public DocumentMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		documentRecordActionsServices = new DocumentRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Document document, User user, List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(DOCUMENT_DISPLAY.name())) {
			menuItemActions.add(buildMenuItemAction(DOCUMENT_DISPLAY,
					isMenuItemActionPossible(DOCUMENT_DISPLAY, document, user, params),
					"DisplayDocumentView.displayDocument", FontAwesome.FILE_O, -1, 100,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).display(params)));
		}

		if (!filteredActionTypes.contains(DOCUMENT_OPEN.name())) {
			// FIXME better way? get icon by mime-type?
			Resource icon = FileIconUtils.getIcon(document.getContent() != null ?
												  document.getContent().getCurrentVersion().getFilename() : "");
			menuItemActions.add(buildMenuItemAction(DOCUMENT_OPEN,
					isMenuItemActionPossible(DOCUMENT_OPEN, document, user, params),
					"DisplayDocumentView.openDocument", icon, -1, 200,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).open(params)));
		}

		if (!filteredActionTypes.contains(DOCUMENT_EDIT.name())) {
			menuItemActions.add(buildMenuItemAction(DOCUMENT_EDIT,
					isMenuItemActionPossible(DOCUMENT_EDIT, document, user, params),
					"DisplayDocumentView.editDocument", FontAwesome.EDIT, -1, 250,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).edit(params)));
		}

		if (!filteredActionTypes.contains(DOCUMENT_DOWNLOAD.name())) {
			menuItemActions.add(buildMenuItemAction(DOCUMENT_DOWNLOAD,
					isMenuItemActionPossible(DOCUMENT_DOWNLOAD, document, user, params),
					"DocumentContextMenu.downloadDocument", FontAwesome.DOWNLOAD, -1, 400,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).download(params)));
		}

		if (!filteredActionTypes.contains(DOCUMENT_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_DELETE,
					isMenuItemActionPossible(DOCUMENT_DELETE, document, user, params),
					"DocumentContextMenu.deleteDocument", FontAwesome.TRASH_O, -1, 500,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).delete(params));

			menuItemAction.setConfirmMessage(i18n.$("ConfirmDialog.confirmDelete"));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_COPY.name())) {
			menuItemActions.add(buildMenuItemAction(DOCUMENT_COPY,
					isMenuItemActionPossible(DOCUMENT_COPY, document, user, params),
					"DocumentContextMenu.copyContent", null, -1, 600,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).copy(params)));
		}

		if (!filteredActionTypes.contains(DOCUMENT_FINALIZE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_FINALIZE,
					isMenuItemActionPossible(DOCUMENT_FINALIZE, document, user, params),
					"DocumentContextMenu.finalize", FontAwesome.LEVEL_UP, -1, 700,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).finalize(params));

			menuItemAction.setConfirmMessage(i18n.$("DocumentActionsComponent.finalize.confirm"));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_PUBLISH.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_PUBLISH,
					isMenuItemActionPossible(DOCUMENT_PUBLISH, document, user, params),
					"DocumentContextMenu.publish", null, -1, 800,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).publish(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_UNPUBLISH.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_UNPUBLISH,
					isMenuItemActionPossible(DOCUMENT_UNPUBLISH, document, user, params),
					"DocumentContextMenu.unpublish", null, -1, 800,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).unPublish(params));

			menuItemActions.add(menuItemAction);
		}


		if (!filteredActionTypes.contains(DOCUMENT_CREATE_PDF.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_CREATE_PDF,
					isMenuItemActionPossible(DOCUMENT_CREATE_PDF, document, user, params),
					"DocumentContextMenu.createPDFA", null, -1, 900,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).createPdf(params));
			menuItemAction.setConfirmMessage(i18n.$("ConfirmDialog.confirmCreatePDFA"));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_ADD_TO_SELECTION.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_ADD_TO_SELECTION,
					isMenuItemActionPossible(DOCUMENT_ADD_TO_SELECTION, document, user, params),
					"DocumentContextMenu.addToSelection", null, -1, 1000,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addToSelection(params));

			menuItemActions.add(menuItemAction);
		}


		if (!filteredActionTypes.contains(DOCUMENT_REMOVE_TO_SELECTION.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_REMOVE_TO_SELECTION,
					isMenuItemActionPossible(DOCUMENT_REMOVE_TO_SELECTION, document, user, params),
					"DocumentContextMenu.removeToSelection", null, -1, 1100,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).removeToSelection(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_ADD_TO_CART.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_ADD_TO_CART,
					isMenuItemActionPossible(DOCUMENT_ADD_TO_CART, document, user, params),
					"DisplayFolderView.addToCart", null, -1, 1200,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addToCart(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_UPLOAD.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_UPLOAD,
					isMenuItemActionPossible(DOCUMENT_UPLOAD, document, user, params),
					"DocumentContextMenu.upload", null, -1, 1250,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).upload(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_PRINT_LABEL.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_PRINT_LABEL,
					isMenuItemActionPossible(DOCUMENT_PRINT_LABEL, document, user, params),
					"DisplayFolderView.printLabel", null, -1, 1300,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).printLabel(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_CHECK_IN.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_CHECK_IN,
					isMenuItemActionPossible(DOCUMENT_CHECK_IN, document, user, params),
					"DocumentContextMenu.checkIn", null, -1, 1400,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).checkIn(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_CHECK_OUT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_CHECK_OUT,

					isMenuItemActionPossible(DOCUMENT_CHECK_OUT, document, user, params),
					"DocumentContextMenu.checkOut", null, -1, 1400,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).checkOut(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_ADD_AUTHORIZATION.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_ADD_AUTHORIZATION,

					isMenuItemActionPossible(DOCUMENT_ADD_AUTHORIZATION, document, user, params),
					"DocumentContextMenu.addAuthorization", null, -1, 1500,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).addAuthorization(params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(DOCUMENT_GENERATE_REPORT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(DOCUMENT_ADD_AUTHORIZATION,

					isMenuItemActionPossible(DOCUMENT_GENERATE_REPORT, document, user, params),
					"DocumentContextMenu.ReportGeneratorButton", null, -1, 1600,
					() -> new DocumentMenuItemActionBehaviors(collection, appLayerFactory).reportGeneratorButton(params));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(MenuItemActionType menuItemActionType, Document document, User user,
											MenuItemActionBehaviorParams params) {
		SessionContext sessionContext = params.getView().getSessionContext();
		Record record = document.getWrappedRecord();

		switch (menuItemActionType) {
			case DOCUMENT_EDIT:
				return documentRecordActionsServices.isEditActionPossible(record, user);
			case DOCUMENT_DISPLAY:
				return documentRecordActionsServices.isDisplayActionPossible(record, user);
			case DOCUMENT_OPEN:
				return documentRecordActionsServices.isOpenActionPossible(record, user);
			case DOCUMENT_DOWNLOAD:
				return documentRecordActionsServices.isDownloadActionPossible(record, user);
			case DOCUMENT_DELETE:
				return documentRecordActionsServices.isDeleteActionPossible(record, user);
			case DOCUMENT_COPY:
				return documentRecordActionsServices.isEditActionPossible(record, user);
			case DOCUMENT_FINALIZE:
				return documentRecordActionsServices.isFinalizeActionPossible(record, user);
			case DOCUMENT_PUBLISH:
				return documentRecordActionsServices.isPublishActionPossible(record, user);
			case DOCUMENT_UNPUBLISH:
				return documentRecordActionsServices.isUnPublishActionPossible(record, user);
			case DOCUMENT_CREATE_PDF:
				return documentRecordActionsServices.isCreatePdfActionPossible(record, user);
			case DOCUMENT_ADD_TO_SELECTION:
				return documentRecordActionsServices.isAddToSelectionActionPossible(record, user, sessionContext) &&
					   (sessionContext.getSelectedRecordIds() == null ||
						!sessionContext.getSelectedRecordIds().contains(record.getId()));
			case DOCUMENT_REMOVE_TO_SELECTION:
				return documentRecordActionsServices.isRemoveToSelectionActionPossible(record, user) &&
					   sessionContext.getSelectedRecordIds() != null &&
					   sessionContext.getSelectedRecordIds().contains(record.getId());
			case DOCUMENT_ADD_TO_CART:
				return documentRecordActionsServices.isAddToCartActionPossible(record, user);
			case DOCUMENT_UPLOAD:
				return documentRecordActionsServices.isUploadActionPossible(record, user);
			case DOCUMENT_PRINT_LABEL:
				return documentRecordActionsServices.isPrintLabelActionPossible(record, user);
			case DOCUMENT_CHECK_OUT:
				return documentRecordActionsServices.isCheckOutActionPossible(record, user);
			case DOCUMENT_CHECK_IN:
				return documentRecordActionsServices.isCheckInActionPossible(record, user);
			case DOCUMENT_ADD_AUTHORIZATION:
				return documentRecordActionsServices.isAddAuthorizationActionPossible(record, user);
			case DOCUMENT_GENERATE_REPORT:
				return documentRecordActionsServices.isGenerateReportActionPossible(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}

	private MenuItemAction buildMenuItemAction(MenuItemActionType type, boolean possible, String caption,
											   Resource icon, int group, int priority, Runnable command) {
		return MenuItemAction.builder()
				.type(type.name())
				.state(possible ? MenuItemActionState.VISIBLE : MenuItemActionState.HIDDEN)
				.caption(caption)
				.icon(icon)
				.group(group)
				.priority(priority)
				.command(command)
				.build();
	}
}
