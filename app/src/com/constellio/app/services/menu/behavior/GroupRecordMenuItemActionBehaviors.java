package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton.AddedToCollectionRecordType;
import com.constellio.app.modules.rm.ui.buttons.UsersAddToGroupsWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupRecordMenuItemActionBehaviors {
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;
	private String collection;

	public GroupRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
	}

	private Map<String, String> clone(Map<String, String> map) {
		if (map == null) {
			return null;
		}

		Map<String, String> newMap = new HashMap<>();

		newMap.putAll(map);

		return newMap;
	}


	public void edit(List<Group> groupRecords, MenuItemActionBehaviorParams params) {

	}

	public void consult(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}

	public void addUserToGroup(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		UsersAddToGroupsWindowButton button = new UsersAddToGroupsWindowButton(groupRecords, params);
		button.click();
	}

	public void addToCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		List<Record> records = groupRecords.stream().map(group -> group.getWrappedRecord()).collect(Collectors.toList());
		CollectionsWindowButton collectionsWindowButton = new CollectionsWindowButton(records, params, AddedToCollectionRecordType.GROUP);
		collectionsWindowButton.addToCollections();
	}

	public void delete(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}

	public void activate(List<Group> groupRecords, MenuItemActionBehaviorParams params, boolean isActivated) {
	}

	public void removeUser(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}

	public void manageSecurity(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}

	public void manageRoles(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}

	public void removeFromCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}
}
