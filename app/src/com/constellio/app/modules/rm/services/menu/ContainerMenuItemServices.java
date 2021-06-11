package com.constellio.app.modules.rm.services.menu;

import com.constellio.app.modules.rm.services.actions.ContainerRecordActionsServices;
import com.constellio.app.modules.rm.services.menu.behaviors.ContainerRecordMenuItemActionBehaviors;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.MenuItemAction;
import com.constellio.app.services.menu.MenuItemActionState;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_ADD_TO_CART;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_ADD_TO_SELECTION;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_BORROW;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_CHECK_IN;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_CONSULT;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_CONSULT_LINK;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_DELETE;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_DELETE_CONTENT;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_EDIT;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_EMPTY_THE_BOX;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_GENERATE_REPORT;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_LABELS;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_REMOVE_FROM_SELECTION;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_RETURN_REMAINDER;
import static com.constellio.app.modules.rm.services.menu.ContainerMenuItemServices.ContainerRecordMenuItemActionType.CONTAINER_SLIP;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
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

		if (!filteredActionTypes.contains(CONTAINER_CONSULT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_CONSULT.name(),
					isMenuItemActionPossible(CONTAINER_CONSULT.name(), container, user, params),
					$("DisplayContainerView.consult"), FontAwesome.SEARCH, -1, 1,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).consult(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_EDIT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_EDIT.name(),
					isMenuItemActionPossible(CONTAINER_EDIT.name(), container, user, params),
					$("DisplayContainerView.edit"), FontAwesome.EDIT, -1, 20,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).edit(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_SLIP.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_SLIP.name(),
					isMenuItemActionPossible(CONTAINER_SLIP.name(), container, user, params),
					$("DisplayContainerView.slip"), FontAwesome.PRINT, -1, 30,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).report(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_LABELS.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_LABELS.name(),
					isMenuItemActionPossible(CONTAINER_LABELS.name(), container, user, params),
					$("SearchView.printLabels"), FontAwesome.PRINT, -1, 35,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).printLabel(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_ADD_TO_CART.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_ADD_TO_CART.name(),
					isMenuItemActionPossible(CONTAINER_ADD_TO_CART.name(), container, user, params),
					$("DisplayContainerView.addToCart"), FontAwesome.STAR, -1, 40,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).addToCart(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_CONSULT_LINK.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_CONSULT_LINK.name(),
					isMenuItemActionPossible(CONTAINER_CONSULT_LINK.name(), container, user, params),
					$("consultationLink"), FontAwesome.LINK, -1, 45,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).getConsultationLink(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_BORROW.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_BORROW.name(),
					isMenuItemActionPossible(CONTAINER_BORROW.name(), container, user, params),
					$("DisplayFolderView.borrow"), null, -1, 65,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).borrow(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_CHECK_IN.name())) {
			menuItemActions.add(buildMenuItemAction(CONTAINER_CHECK_IN.name(),
					isMenuItemActionPossible(CONTAINER_CHECK_IN.name(), container, user, params),
					$("DisplayContainerView.checkIn"), null, -1, 66,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).checkIn(container, params)));
		}

		if (!filteredActionTypes.contains(CONTAINER_RETURN_REMAINDER.name())) {
			menuItemActions.add(buildMenuItemAction(CONTAINER_RETURN_REMAINDER.name(),
					isMenuItemActionPossible(CONTAINER_RETURN_REMAINDER.name(), container, user, params),
					$("SendReturnReminderEmailButton.reminderReturn"), FontAwesome.PAPER_PLANE, -1, 70,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).sendReturnRemainder(container, params)));
		}

		if (!filteredActionTypes.contains(CONTAINER_GENERATE_REPORT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_GENERATE_REPORT.name(),
					isMenuItemActionPossible(CONTAINER_GENERATE_REPORT.name(), container, user, params),
					$("SearchView.metadataReportTitle"), null, -1, 75,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).generateReport(container, params));
			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_ADD_TO_SELECTION.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_ADD_TO_SELECTION.name(),
					isMenuItemActionPossible(CONTAINER_ADD_TO_SELECTION.name(), container, user, params),
					$("addToOrRemoveFromSelection.add"), FontAwesome.SHOPPING_BASKET, -1, 80,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).addToSelection(container, params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_REMOVE_FROM_SELECTION.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_REMOVE_FROM_SELECTION.name(),
					isMenuItemActionPossible(CONTAINER_REMOVE_FROM_SELECTION.name(), container, user, params),
					$("addToOrRemoveFromSelection.remove"), FontAwesome.SHOPPING_BASKET, -1, 81,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).removeToSelection(container, params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_DELETE_CONTENT.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_DELETE_CONTENT.name(),
					isMenuItemActionPossible(CONTAINER_DELETE_CONTENT.name(), container, user, params),
					$("ContainerMenuItemServices.deleteContent"), FontAwesome.ERASER, -1, Integer.MAX_VALUE - 3,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).deleteContent(container, params));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_EMPTY_THE_BOX.name())) {
			// confirm message
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_EMPTY_THE_BOX.name(),
					isMenuItemActionPossible(CONTAINER_EMPTY_THE_BOX.name(), container, user, params),
					$("DisplayContainerView.empty"), null, -1, Integer.MAX_VALUE - 1,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).empty(container, params));

			menuItemAction.setConfirmMessage($("DisplayContainerView.confirmEmpty"));

			menuItemActions.add(menuItemAction);
		}

		if (!filteredActionTypes.contains(CONTAINER_DELETE.name())) {
			MenuItemAction menuItemAction = buildMenuItemAction(CONTAINER_DELETE.name(),
					isMenuItemActionPossible(CONTAINER_DELETE.name(), container, user, params),
					$("DisplayContainerView.delete"), FontAwesome.TRASH_O, -1, Integer.MAX_VALUE,
					(ids) -> new ContainerRecordMenuItemActionBehaviors(collection, appLayerFactory).delete(container, params));

			menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

			menuItemActions.add(menuItemAction);
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, ContainerRecord container, User user,
											MenuItemActionBehaviorParams params) {
		SessionContext sessionContext = params.getView().getSessionContext();
		Record record = container.getWrappedRecord();

		switch (ContainerRecordMenuItemActionType.valueOf(menuItemActionType)) {
			case CONTAINER_CONSULT:
				return containerRecordActionsServices.isDisplayActionPossible(record, user);
			case CONTAINER_EDIT:
				return containerRecordActionsServices.isEditActionPossible(record, user);
			case CONTAINER_CONSULT_LINK:
				return containerRecordActionsServices.isConsultLinkActionPossible(record, user);
			case CONTAINER_SLIP:
				return containerRecordActionsServices.isSlipActionPossible(record, user);
			case CONTAINER_LABELS:
				return containerRecordActionsServices.isPrintLabelActionPossible(record, user);
			case CONTAINER_CHECK_IN:
				return containerRecordActionsServices.isCheckInActionPossible(record, user);
			case CONTAINER_RETURN_REMAINDER:
				return containerRecordActionsServices.isSendReturnReminderActionPossible(record, user);
			case CONTAINER_ADD_TO_CART:
				return containerRecordActionsServices.isAddToCartActionPossible(record, user);
			case CONTAINER_DELETE:
				return containerRecordActionsServices.isDeleteActionPossible(record, user);
			case CONTAINER_EMPTY_THE_BOX:
				return containerRecordActionsServices.isEmptyTheBoxActionPossible(record, user);
			case CONTAINER_GENERATE_REPORT:
				return containerRecordActionsServices.isGenerateReportActionPossible(record, user);
			case CONTAINER_ADD_TO_SELECTION:
				return containerRecordActionsServices.isAddToSelectionActionPossible(record, user) &&
					   (sessionContext.getSelectedRecordIds() == null ||
						!sessionContext.getSelectedRecordIds().contains(record.getId()));
			case CONTAINER_REMOVE_FROM_SELECTION:
				return containerRecordActionsServices.isRemoveToSelectionActionPossible(record, user) &&
					   sessionContext.getSelectedRecordIds() != null &&
					   sessionContext.getSelectedRecordIds().contains(record.getId());
			case CONTAINER_BORROW:
				return containerRecordActionsServices.isBorrowActionPossible(record, user);
			case CONTAINER_DELETE_CONTENT:
				return containerRecordActionsServices.isDeleteContainerContent(record, user);
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

	public enum ContainerRecordMenuItemActionType {
		CONTAINER_CONSULT,
		CONTAINER_EDIT,
		CONTAINER_CONSULT_LINK,
		CONTAINER_SLIP,
		CONTAINER_LABELS,
		CONTAINER_ADD_TO_CART,
		CONTAINER_DELETE,
		CONTAINER_EMPTY_THE_BOX,
		CONTAINER_GENERATE_REPORT,
		CONTAINER_ADD_TO_SELECTION,
		CONTAINER_REMOVE_FROM_SELECTION,
		CONTAINER_BORROW,
		CONTAINER_RETURN_REMAINDER,
		CONTAINER_CHECK_IN,
		CONTAINER_DELETE_CONTENT,
	}

}
