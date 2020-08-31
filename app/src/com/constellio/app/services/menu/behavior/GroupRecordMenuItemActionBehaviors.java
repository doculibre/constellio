package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.AddGroupsToCollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.AddUsersInGroupsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.RemoveGroupsFromCollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.RemoveUsersFromGroupsWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Group;

import java.util.List;

public class GroupRecordMenuItemActionBehaviors {

	private String collection;
	private AppLayerFactory appLayerFactory;

	public GroupRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	public void edit(Group groupRecords, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editGlobalGroup(groupRecords.getCode());
	}

	public void consult(Group groupRecords, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().displayGlobalGroup(groupRecords.getCode());
	}

	public void addUserToGroup(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		AddUsersInGroupsWindowButton addButton = new AddUsersInGroupsWindowButton(groupRecords, params);
		addButton.click();
	}

	public void addToCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		AddGroupsToCollectionsWindowButton addButton = new AddGroupsToCollectionsWindowButton(groupRecords, params);
		addButton.click();
	}

	public void removeUser(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		RemoveUsersFromGroupsWindowButton deleteButton = new RemoveUsersFromGroupsWindowButton(groupRecords, params);
		deleteButton.click();
	}

	public void manageSecurity(Group group, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().listPrincipalAccessAuthorizations(group.getId());
	}

	public void manageRoles(Group group, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editCollectionGroupRoles(group.getId());
	}

	public void removeFromCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		RemoveGroupsFromCollectionsWindowButton deleteButton = new RemoveGroupsFromCollectionsWindowButton(groupRecords, params);
		deleteButton.click();
	}
}
