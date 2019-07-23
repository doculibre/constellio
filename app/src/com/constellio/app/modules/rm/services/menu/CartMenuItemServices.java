package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.actions.CartActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.CartMenuItemActionBehaviors;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_BATCH_DELETE;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_BATCH_DUPLICATE;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_CONTAINER_RECORD_BATCH_PROCESSING;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_CONTAINER_RECORD_LABEL;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_CREATE_SIP_ARCHIVE;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_DECOMMISSIONING_LIST;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_DOCUMENT_BATCH_PROCESSING;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_DOCUMENT_LABEL;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_EMPTY;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_FOLDER_BATCH_PROCESSING;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_FOLDER_LABEL;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_PREPARE_EMAIL;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_PRINT_CONSOLIDATED_PDF;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_PRINT_METADATA_REPORT;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_RENAME;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_SHARE;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class CartMenuItemServices {

	private CartActionsServices cartActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	public CartMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.cartActionsServices = new CartActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(Cart cart, User user,
													List<String> excludedActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!excludedActionTypes.contains(CART_RENAME.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_RENAME.name(),
					isMenuItemActionPossible(CART_RENAME.name(), cart, user, params),
					$("CartView.reNameCartGroup"), null, -1, 100,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).rename(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_PREPARE_EMAIL.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_PREPARE_EMAIL.name(),
					isMenuItemActionPossible(CART_PREPARE_EMAIL.name(), cart, user, params),
					$("CartView.prepareEmail"), null, -1, 200,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).prepareEmail(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_BATCH_DUPLICATE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_BATCH_DUPLICATE.name(),
					isMenuItemActionPossible(CART_BATCH_DUPLICATE.name(), cart, user, params),
					$("CartView.documentsBatchProcessingButton"), null, -1, 300,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).batchDuplicate(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_DOCUMENT_BATCH_PROCESSING.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_DOCUMENT_BATCH_PROCESSING.name(),
					isMenuItemActionPossible(CART_DOCUMENT_BATCH_PROCESSING.name(), cart, user, params),
					$("CartView.documentsBatchProcessingButton"), null, -1, 400,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).documentBatchProcessing(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_FOLDER_BATCH_PROCESSING.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_FOLDER_BATCH_PROCESSING.name(),
					isMenuItemActionPossible(CART_FOLDER_BATCH_PROCESSING.name(), cart, user, params),
					$("CartView.foldersBatchProcessingButton"), null, -1, 500,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).folderBatchProcessing(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_CONTAINER_RECORD_BATCH_PROCESSING.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_CONTAINER_RECORD_BATCH_PROCESSING.name(),
					isMenuItemActionPossible(CART_CONTAINER_RECORD_BATCH_PROCESSING.name(), cart, user, params),
					$("CartView.containersBatchProcessingButton"), null, -1, 600,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).containerRecordBatchProcessing(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_DOCUMENT_LABEL.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_DOCUMENT_LABEL.name(),
					isMenuItemActionPossible(CART_DOCUMENT_LABEL.name(), cart, user, params),
					$("CartView.documentLabelsButton"), null, -1, 700,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).documentLabels(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_FOLDER_LABEL.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_FOLDER_LABEL.name(),
					isMenuItemActionPossible(CART_FOLDER_LABEL.name(), cart, user, params),
					$("CartView.foldersLabelsButton"), null, -1, 800,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).foldersLabels(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_CONTAINER_RECORD_LABEL.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_CONTAINER_RECORD_LABEL.name(),
					isMenuItemActionPossible(CART_CONTAINER_RECORD_LABEL.name(), cart, user, params),
					$("CartView.containersLabelsButton"), null, -1, 900,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).containerRecordLabels(cart, params));
			menuItemActions.add(menuItemAction);
		}


		if (!excludedActionTypes.contains(CART_BATCH_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_BATCH_DELETE.name(),
					isMenuItemActionPossible(CART_BATCH_DELETE.name(), cart, user, params),
					$(DeleteButton.CAPTION), null, -1, 1000,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).batchDelete(cart, params));

			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_EMPTY.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_EMPTY.name(),
					isMenuItemActionPossible(CART_EMPTY.name(), cart, user, params),
					$("CartView.empty"), null, -1, 1100,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).empty(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_SHARE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_SHARE.name(),
					isMenuItemActionPossible(CART_SHARE.name(), cart, user, params),
					$("CartView.share"), null, -1, 1200,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).share(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_DECOMMISSIONING_LIST.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_DECOMMISSIONING_LIST.name(),
					isMenuItemActionPossible(CART_DECOMMISSIONING_LIST.name(), cart, user, params),
					$("CartView.decommissioningList"), null, -1, 1300,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).decommission(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_PRINT_METADATA_REPORT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_PRINT_METADATA_REPORT.name(),
					isMenuItemActionPossible(CART_PRINT_METADATA_REPORT.name(), cart, user, params),
					$("ReportGeneratorButton.buttonText"), null, -1, 1400,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).printMetadataReportAction(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_CREATE_SIP_ARCHIVE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_CREATE_SIP_ARCHIVE.name(),
					isMenuItemActionPossible(CART_CREATE_SIP_ARCHIVE.name(), cart, user, params),
					$("SIPButton.caption"), null, -1, 1500,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).createSIPArchvesAction(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_PRINT_CONSOLIDATED_PDF.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_PRINT_CONSOLIDATED_PDF.name(),
					isMenuItemActionPossible(CART_PRINT_CONSOLIDATED_PDF.name(), cart, user, params),
					$("ConsolidatedPDFWindow.caption"), null, -1, 1600,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).consolidatedPdfAction(cart, params));
			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, Cart cart, User user,
											MenuItemActionBehaviorParams params) {
		Record record = cart.getWrappedRecord();

		switch (CartMenuItemActionType.valueOf(menuItemActionType)) {
			case CART_RENAME:
				return cartActionsServices.isRenameActionPossible(record, user);
			case CART_PREPARE_EMAIL:
				return cartActionsServices.isPrepareEmailActionPossible(record, user);
			case CART_BATCH_DUPLICATE:
				return cartActionsServices.isBatchDuplicateActionPossible(record, user);
			case CART_BATCH_DELETE:
				return cartActionsServices.isBatchDeleteActionPossible(record, user);
			case CART_DOCUMENT_BATCH_PROCESSING:
				return cartActionsServices.isDocumentBatchProcessingActionPossible(record, user);
			case CART_FOLDER_BATCH_PROCESSING:
				return cartActionsServices.isFolderBatchProcessingActionPossible(record, user);
			case CART_CONTAINER_RECORD_BATCH_PROCESSING:
				return cartActionsServices.isContainerBatchProcessingActionPossible(record, user);
			case CART_DOCUMENT_LABEL:
				return cartActionsServices.isDocumentLabelsActionPossible(record, user);
			case CART_FOLDER_LABEL:
				return cartActionsServices.isFoldersLabelsActionPossible(record, user);
			case CART_CONTAINER_RECORD_LABEL:
				return cartActionsServices.isContainersLabelsActionPossible(record, user);
			case CART_EMPTY:
				return cartActionsServices.isEmptyActionPossible(record, user);
			case CART_SHARE:
				return cartActionsServices.isShareActionPossible(record, user);
			case CART_DECOMMISSIONING_LIST:
				return cartActionsServices.isDecommissionActionPossible(record, user);
			case CART_PRINT_METADATA_REPORT:
				return cartActionsServices.isPrintMetadataReportActionPossible(record, user);
			case CART_CREATE_SIP_ARCHIVE:
				return cartActionsServices.isCreateSIPArchvesActionPossible(record, user);
			case CART_PRINT_CONSOLIDATED_PDF:
				return cartActionsServices.isPrntConsolidatedPdfActionPossible(record, user);
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

	enum CartMenuItemActionType {
		CART_RENAME,
		CART_PREPARE_EMAIL,
		CART_BATCH_DUPLICATE,

		CART_DOCUMENT_BATCH_PROCESSING,
		CART_FOLDER_BATCH_PROCESSING,
		CART_CONTAINER_RECORD_BATCH_PROCESSING,
		CART_DOCUMENT_LABEL,
		CART_FOLDER_LABEL,
		CART_CONTAINER_RECORD_LABEL,

		CART_BATCH_DELETE,

		CART_EMPTY,
		CART_SHARE,
		CART_DECOMMISSIONING_LIST,
		CART_PRINT_METADATA_REPORT,
		CART_CREATE_SIP_ARCHIVE,
		CART_PRINT_CONSOLIDATED_PDF
	}

}
