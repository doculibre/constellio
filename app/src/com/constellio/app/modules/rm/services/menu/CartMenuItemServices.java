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
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_BATCH_DELETE;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_CONTAINER_RECORD_BATCH_PROCESSING;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_DECOMMISSIONING_LIST;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_DOCUMENT_BATCH_PROCESSING;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_EMPTY;
import static com.constellio.app.modules.rm.services.menu.CartMenuItemServices.CartMenuItemActionType.CART_FOLDER_BATCH_PROCESSING;
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

		if (!excludedActionTypes.contains(CART_DOCUMENT_BATCH_PROCESSING.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_DOCUMENT_BATCH_PROCESSING.name(),
					isMenuItemActionPossible(CART_DOCUMENT_BATCH_PROCESSING.name(), cart, user, params),
					$("CartView.documentsBatchProcessingButton"), FontAwesome.LIST, -1, 400,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).documentBatchProcessing(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_FOLDER_BATCH_PROCESSING.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_FOLDER_BATCH_PROCESSING.name(),
					isMenuItemActionPossible(CART_FOLDER_BATCH_PROCESSING.name(), cart, user, params),
					$("CartView.foldersBatchProcessingButton"), FontAwesome.LIST, -1, 500,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).folderBatchProcessing(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_CONTAINER_RECORD_BATCH_PROCESSING.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_CONTAINER_RECORD_BATCH_PROCESSING.name(),
					isMenuItemActionPossible(CART_CONTAINER_RECORD_BATCH_PROCESSING.name(), cart, user, params),
					$("CartView.containersBatchProcessingButton"), FontAwesome.LIST, -1, 600,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).containerRecordBatchProcessing(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_EMPTY.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_EMPTY.name(),
					isMenuItemActionPossible(CART_EMPTY.name(), cart, user, params),
					$("CartView.empty"), FontAwesome.BAN, -1, 1100,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).empty(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_SHARE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_SHARE.name(),
					isMenuItemActionPossible(CART_SHARE.name(), cart, user, params),
					$("CartView.share"), FontAwesome.MAIL_FORWARD, -1, 1200,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).share(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_DECOMMISSIONING_LIST.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_DECOMMISSIONING_LIST.name(),
					isMenuItemActionPossible(CART_DECOMMISSIONING_LIST.name(), cart, user, params),
					$("CartView.decommissioningList"), FontAwesome.LIST_ALT, -1, 1300,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).decommission(cart, params));
			menuItemActions.add(menuItemAction);
		}

		if (!excludedActionTypes.contains(CART_BATCH_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CART_BATCH_DELETE.name(),
					isMenuItemActionPossible(CART_BATCH_DELETE.name(), cart, user, params),
					$(DeleteButton.CAPTION), FontAwesome.TRASH_O, -1, Integer.MAX_VALUE-1,
					(ids) -> new CartMenuItemActionBehaviors(collection, appLayerFactory).batchDelete(cart, params));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, Cart cart, User user,
											MenuItemActionBehaviorParams params) {
		Record record = cart.getWrappedRecord();

		switch (CartMenuItemActionType.valueOf(menuItemActionType)) {
			case CART_BATCH_DELETE:
				return cartActionsServices.isBatchDeleteActionPossible(record, user);
			case CART_DOCUMENT_BATCH_PROCESSING:
				return cartActionsServices.isDocumentBatchProcessingActionPossible(record, user);
			case CART_FOLDER_BATCH_PROCESSING:
				return cartActionsServices.isFolderBatchProcessingActionPossible(record, user);
			case CART_CONTAINER_RECORD_BATCH_PROCESSING:
				return cartActionsServices.isContainerBatchProcessingActionPossible(record, user);
			case CART_EMPTY:
				return cartActionsServices.isEmptyActionPossible(record, user);
			case CART_SHARE:
				return cartActionsServices.isShareActionPossible(record, user);
			case CART_DECOMMISSIONING_LIST:
				return cartActionsServices.isDecommissionActionPossible(record, user);
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
		CART_DOCUMENT_BATCH_PROCESSING,
		CART_FOLDER_BATCH_PROCESSING,
		CART_CONTAINER_RECORD_BATCH_PROCESSING,

		CART_BATCH_DELETE,

		CART_EMPTY,
		CART_SHARE,
		CART_DECOMMISSIONING_LIST,
	}

}
