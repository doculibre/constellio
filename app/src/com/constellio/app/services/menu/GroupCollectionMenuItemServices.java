package com.constellio.app.services.menu;

import com.constellio.app.services.action.GroupRecordActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.GroupRecordMenuItemActionBehaviors;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_ADD_TO_COLLECTION;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_ADD_USER;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_CONSULT;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_EDIT;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_MANAGE_ROLES;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_MANAGE_SECURITY;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_REMOVE_FROM_COLLECTION;
import static com.constellio.app.services.menu.GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.GROUP_REMOVE_USER;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.HIDDEN;
import static com.constellio.app.services.menu.MenuItemActionState.MenuItemActionStateStatus.VISIBLE;
import static com.constellio.app.ui.i18n.i18n.$;

public class GroupCollectionMenuItemServices {
	private AppLayerFactory appLayerFactory;
	private GroupRecordActionsServices groupRecordActionsServices;
	private String collection;

	public GroupCollectionMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;

		groupRecordActionsServices = new GroupRecordActionsServices(collection, appLayerFactory);
	}

	public List<MenuItemAction> getActionsForRecords(List<Group> groupRecords, User user,
													 List<String> filteredActionTypes,
													 MenuItemActionBehaviorParams params) {
		List<MenuItemAction> menuItemActions = new ArrayList<>();

		if (!groupRecords.isEmpty()) {


			if (groupRecords.size() == 1) {
				if (!filteredActionTypes.contains(GROUP_CONSULT.name())) {
					MenuItemAction menuItemAction = buildMenuItemAction(GROUP_CONSULT.name(),
							isMenuItemActionPossible(GROUP_CONSULT.name(), groupRecords.get(0), user, params),
							$("CollectionSecurityManagement.consult"), FontAwesome.SEARCH, -1, 100,
							(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).consult(groupRecords.get(0), params));
					menuItemActions.add(menuItemAction);
				}

				if (!filteredActionTypes.contains(GROUP_EDIT.name())) {
					MenuItemAction menuItemAction = buildMenuItemAction(GROUP_EDIT.name(),
							isMenuItemActionPossible(GROUP_EDIT.name(), groupRecords.get(0), user, params),
							$("CollectionSecurityManagement.edit"), FontAwesome.EDIT, -1, 200,
							(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).edit(groupRecords.get(0), params));
					menuItemActions.add(menuItemAction);
				}
			}

			if (!filteredActionTypes.contains(GROUP_MANAGE_SECURITY.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(GROUP_MANAGE_SECURITY.name(),
						isMenuItemActionPossible(GROUP_MANAGE_SECURITY.name(), groupRecords.get(0), user, params),
						$("CollectionSecurityManagement.manageSecurity"), FontAwesome.LOCK, -1, 300,
						(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).manageSecurity(groupRecords.get(0), params));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(GROUP_MANAGE_ROLES.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(GROUP_MANAGE_ROLES.name(),
						isMenuItemActionPossible(GROUP_MANAGE_ROLES.name(), groupRecords.get(0), user, params),
						$("CollectionSecurityManagement.manageRoles"), FontAwesome.USER_SECRET, -1, 400,
						(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).manageRoles(groupRecords.get(0), params));

				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(GROUP_ADD_TO_COLLECTION.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(GROUP_ADD_TO_COLLECTION.name(),
						isMenuItemActionPossible(GROUP_ADD_TO_COLLECTION.name(), groupRecords.get(0), user, params),
						$("CollectionSecurityManagement.addToCollections"), FontAwesome.LOCATION_ARROW, -1, 500,
						(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).addToCollection(groupRecords, params));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(GROUP_ADD_USER.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(GROUP_ADD_USER.name(),
						isMenuItemActionPossible(GROUP_ADD_USER.name(), groupRecords.get(0), user, params),
						$("CollectionSecurityManagement.addUserToGroups"), FontAwesome.USER_PLUS, -1, 700,
						(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).addUserToGroup(groupRecords, params));
				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(GROUP_REMOVE_USER.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(GROUP_REMOVE_USER.name(),
						isMenuItemActionPossible(GROUP_REMOVE_USER.name(), groupRecords.get(0), user, params),
						$("CollectionSecurityManagement.removeUser"), FontAwesome.LONG_ARROW_DOWN, -1, 800,
						(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).removeUser(groupRecords, params));

				menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

				menuItemActions.add(menuItemAction);
			}

			if (!filteredActionTypes.contains(GROUP_REMOVE_FROM_COLLECTION.name())) {
				MenuItemAction menuItemAction = buildMenuItemAction(GROUP_REMOVE_FROM_COLLECTION.name(),
						isMenuItemActionPossible(GROUP_REMOVE_FROM_COLLECTION.name(), groupRecords.get(0), user, params),
						$("CollectionSecurityManagement.removeFromCollection"), FontAwesome.MINUS_CIRCLE, -1, Integer.MAX_VALUE,
						(ids) -> new GroupRecordMenuItemActionBehaviors(collection, appLayerFactory).removeFromCollection(groupRecords, params));

				menuItemAction.setConfirmMessage($("ConfirmDialog.confirmDelete"));

				menuItemActions.add(menuItemAction);
			}
		}

		return menuItemActions;
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, Group groupRecord, User user,
											MenuItemActionBehaviorParams params) {
		SessionContext sessionContext = params.getView().getSessionContext();
		Record record = groupRecord.getWrappedRecord();

		switch (GroupCollectionMenuItemServices.GroupRecordMenuItemActionType.valueOf(menuItemActionType)) {
			case GROUP_CONSULT:
				return groupRecordActionsServices.isDisplayActionPossible(record, user);
			case GROUP_EDIT:
				return groupRecordActionsServices.isEditActionPossible(record, user);
			case GROUP_ADD_USER:
				return groupRecordActionsServices.isAddAUserActionPossible(record, user);
			case GROUP_ADD_TO_COLLECTION:
				return groupRecordActionsServices.isAddToCollectionActionPossible(record, user);
			case GROUP_MANAGE_SECURITY:
				return groupRecordActionsServices.isManageSecurityActionPossible(record, user);
			case GROUP_MANAGE_ROLES:
				return groupRecordActionsServices.isManageRoleActionPossible(record, user);
			case GROUP_REMOVE_USER:
				return groupRecordActionsServices.isRemoveUserActionPossible(record, user);
			case GROUP_REMOVE_FROM_COLLECTION:
				return groupRecordActionsServices.isRemoveFromCollectionActionPossible(record, user);
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

	public enum GroupRecordMenuItemActionType {
		GROUP_CONSULT,
		GROUP_EDIT,
		GROUP_ADD_USER,
		GROUP_ADD_TO_COLLECTION,
		GROUP_MANAGE_SECURITY,
		GROUP_MANAGE_ROLES,
		GROUP_REMOVE_USER,
		GROUP_REMOVE_FROM_COLLECTION,
	}
}
