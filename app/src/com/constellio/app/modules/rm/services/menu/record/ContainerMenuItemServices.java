package com.constellio.app.modules.rm.services.menu.record;

import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.MenuItemAction;
import com.constellio.app.modules.rm.services.menu.MenuItemActionState;
import com.constellio.app.modules.rm.services.menu.MenuItemActionType;
import com.constellio.app.modules.rm.services.menu.behaviors.ContainerRecordMenuItemActionBehaviors;
import com.constellio.app.modules.rm.services.menu.behaviors.MenuItemActionBehaviorParams;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.CONTAINER_ADD_TO_CART;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.CONTAINER_DELETE;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.CONTAINER_EDIT;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.CONTAINER_EMPTY_THE_BOX;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.CONTAINER_LABELS;
import static com.constellio.app.modules.rm.services.menu.MenuItemActionType.CONTAINER_SLIP;
import static com.constellio.app.ui.i18n.i18n.$;

public class ContainerMenuItemServices {

	private ContainerRecordActionsServices containerRecordActionsServices;
	private String collection;
	private AppLayerFactory appLayerFactory;

	public ContainerMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		containerRecordActionsServices = new ContainerRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecord(ContainerRecord container, User user,
													List<String> filteredActionTypes,
													MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!filteredActionTypes.contains(CONTAINER_EDIT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_EDIT,
					isMenuItemActionPossible(CONTAINER_EDIT, container, user, params),
					"DisplayContainerView.edit", FontAwesome.EDIT, -1, 100,
					() -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).edit(params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_SLIP.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_SLIP,
					isMenuItemActionPossible(CONTAINER_SLIP, container, user, params),
					"DisplayContainerView.slip", null, -1, 200,
					() -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).report(params));
			menuItemActions.add(menuItemAction);
		}


		if (!filteredActionTypes.contains(CONTAINER_LABELS.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_LABELS,
					isMenuItemActionPossible(CONTAINER_LABELS, container, user, params),
					"SearchView.labels", null, -1, 300,
					() -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).printLabel(params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_ADD_TO_CART.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_ADD_TO_CART,
					isMenuItemActionPossible(CONTAINER_ADD_TO_CART, container, user, params),
					"DisplayContainerView.addToCart", null, -1, 400,
					() -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).addToCart(params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_DELETE,
					isMenuItemActionPossible(CONTAINER_DELETE, container, user, params),
					"DisplayContainerView.delete", FontAwesome.TRASH_O, -1, 500,
					() -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).delete(params));

			menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_EMPTY_THE_BOX.name())) {
			// confirm message
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_EMPTY_THE_BOX,
					isMenuItemActionPossible(CONTAINER_EMPTY_THE_BOX, container, user, params),
					"DisplayContainerView.empty", null, -1, 600,
					() -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).empty(params));

			menuItemAction.setConfirmMessage($("DisplayContainerView.confirmEmpty"));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(MenuItemActionType menuItemActionType, ContainerRecord container, User user,
											MenuItemActionBehaviorParams params) {
		Record record = container.getWrappedRecord();

		switch (menuItemActionType) {
			case CONTAINER_EDIT:
				return containerRecordActionsServices.isEditActionPossible(record, user);
			case CONTAINER_SLIP:
				return containerRecordActionsServices.isSlipActionPossible(record, user);
			case CONTAINER_LABELS:
				return containerRecordActionsServices.isLabelsActionPossible(record, user);
			case CONTAINER_ADD_TO_CART:
				return containerRecordActionsServices.isAddToCartActionPossible(record, user);
			case CONTAINER_DELETE:
				return containerRecordActionsServices.isDeleteActionPossible(record, user);
			case CONTAINER_EMPTY_THE_BOX:
				return containerRecordActionsServices.isEmptyTheBoxActionPossible(record, user);
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
