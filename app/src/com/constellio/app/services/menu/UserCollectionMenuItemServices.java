package com.constellio.app.services.menu;

import com.constellio.app.services.action.UserRecordActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.services.menu.behavior.UserRecordMenuItemActionBehaviors;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_ADD_TO_COLLECTION;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_ADD_TO_GROUP;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_CHANGE_STATUS;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_DELETE;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_MANAGE_ROLES;
import static com.constellio.app.services.menu.UserCollectionMenuItemServices.UserRecordMenuItemActionType.USER_MANAGE_SECURITY;
import static com.constellio.app.ui.i18n.i18n.$;

public class UserCollectionMenuItemServices {
	private AppLayerFactory appLayerFactory;
	private UserRecordActionsServices userRecordActionsServices;
	private UserServices userServices;
	private String collection;

	public UserCollectionMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		userRecordActionsServices = new UserRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecords(List<User> userRecords, User user,
													 List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!userRecords.isEmpty()) {
			//			if (!filteredActionTypes.contains(USER_CONSULT.name())) {
			//				MenuItemAction menuItemAction = buildMenuItemAction(USER_CONSULT.name(),
			//						isMenuItemActionPossible(USER_CONSULT.name(), userRecords.get(0), user, params),
			//						$("CollectionSecurityManagement.consult"), FontAwesome.SEARCH, -1, 100,
			//						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).consult(userRecords, params));
			//				menuItemActions.add(menuItemAction);
			//			}

			if (userRecords.size() == 1) {
				//				if (!filteredActionTypes.contains(USER_EDIT.name())) {
				//					MenuItemAction menuItemAction = buildMenuItemAction(USER_EDIT.name(),
				//							isMenuItemActionPossible(USER_EDIT.name(), userRecords.get(0), user, params),
				//							$("CollectionSecurityManagement.edit"), FontAwesome.EDIT, -1, 150,
				//							(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).edit(userRecords, params));
				//					menuItemActions.add(menuItemAction);
				//				}
			}

			if (!filteredActionTypes.contains(USER_ADD_TO_GROUP.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(USER_ADD_TO_GROUP.name(),
						isMenuItemActionPossible(USER_ADD_TO_GROUP.name(), userRecords.get(0), user, params),
						$("CollectionSecurityManagement.addToGroup"), FontAwesome.USERS, -1, 200,
						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).addToGroup(userRecords, params));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(USER_ADD_TO_COLLECTION.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(USER_ADD_TO_COLLECTION.name(),
						isMenuItemActionPossible(USER_ADD_TO_COLLECTION.name(), userRecords.get(0), user, params),
						$("CollectionSecurityManagement.addToCollections"), FontAwesome.LOCATION_ARROW, -1, 300,
						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).addToCollection(userRecords, params));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(USER_DELETE.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(USER_DELETE.name(),
						isMenuItemActionPossible(USER_DELETE.name(), userRecords.get(0), user, params),
						$("CollectionSecurityManagement.delete"), FontAwesome.TRASH_O, -1, 325,
						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).delete(userRecords, params));
				menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(USER_CHANGE_STATUS.name())) {
				menuItemActions.add(buildMenuItemAction(USER_CHANGE_STATUS.name(),
						isMenuItemActionPossible(USER_CHANGE_STATUS.name(), userRecords.get(0), user, params),
						$("CollectionSecurityManagement.changeStatus"), FontAwesome.FLAG, -1, 350,
						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).changeStatus(userRecords, params)));
			}

			if (!filteredActionTypes.contains(USER_MANAGE_SECURITY.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(USER_MANAGE_SECURITY.name(),
						isMenuItemActionPossible(USER_MANAGE_SECURITY.name(), userRecords.get(0), user, params),
						$("CollectionSecurityManagement.manageSecurity"), FontAwesome.LOCK, -1, 400,
						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).manageSecurity(userRecords, params));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(USER_MANAGE_ROLES.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(USER_MANAGE_ROLES.name(),
						isMenuItemActionPossible(USER_MANAGE_ROLES.name(), userRecords.get(0), user, params),
						$("CollectionSecurityManagement.manageRoles"), FontAwesome.USER_SECRET, -1, 500,
						(ids) -> new UserRecordMenuItemActionBehaviors(collection, appLayerFactory).manageRole(userRecords, params));

				menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

				menuItemActions.add(menuItemAction);
			}
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, User userRecord, User user,
											MenuItemActionBehaviorParams params) {
		SessionContext sessionContext = params.getView().getSessionContext();
		Record record = userRecord.getWrappedRecord();

		switch (UserCollectionMenuItemServices.UserRecordMenuItemActionType.valueOf(menuItemActionType)) {
			case USER_CONSULT:
				return userRecordActionsServices.isDisplayActionPossible(record, user);
			case USER_EDIT:
				return userRecordActionsServices.isEditActionPossible(record, user);
			case USER_ADD_TO_GROUP:
				return userRecordActionsServices.isAddToGroupActionPossible(record, user);
			case USER_ADD_TO_COLLECTION:
				return userRecordActionsServices.isAddToCollectionActionPossible(record, user);
			case USER_DELETE:
				return userRecordActionsServices.isDeleteActionPossible(record, user);
			case USER_CHANGE_STATUS:
				return userRecordActionsServices.isChangeStatusActionPossible(record, user);
			case USER_MANAGE_SECURITY:
				return userRecordActionsServices.isManageSecurityActionPossible(record, user);
			case USER_MANAGE_ROLES:
				return userRecordActionsServices.isManageRoleActionPossible(record, user);
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

	public enum UserRecordMenuItemActionType {
		USER_CONSULT,
		USER_EDIT,
		USER_ADD_TO_GROUP,
		USER_ADD_TO_COLLECTION,
		USER_DELETE,
		USER_CHANGE_STATUS,
		USER_MANAGE_SECURITY,
		USER_MANAGE_ROLES,
	}
}
