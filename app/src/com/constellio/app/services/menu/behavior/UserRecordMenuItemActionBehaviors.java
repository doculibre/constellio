package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton.AddedToCollectionRecordType;
import com.constellio.app.modules.rm.ui.buttons.GroupWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRecordMenuItemActionBehaviors {

	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;
	private String collection;

	public UserRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
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


	public void edit(List<User> userRecords, MenuItemActionBehaviorParams params) {

	}

	public void consult(List<User> userRecords, MenuItemActionBehaviorParams params) {
	}

	public void addToGroup(List<User> userRecords, MenuItemActionBehaviorParams params) {
		GroupWindowButton groupWindowButton = new GroupWindowButton(userRecords, params);
		groupWindowButton.addToGroup();
	}

	public void addToCollection(List<User> userRecords, MenuItemActionBehaviorParams params) {
		List<Record> records = userRecords.stream().map(user -> user.getWrappedRecord()).collect(Collectors.toList());
		CollectionsWindowButton cartWindowButton = new CollectionsWindowButton(records, params, AddedToCollectionRecordType.USER);
		cartWindowButton.addToCollections();
	}

	public void delete(List<User> userRecords, MenuItemActionBehaviorParams params) {
	}

	public void changeStatus(List<User> userRecords, MenuItemActionBehaviorParams params) {
	}

	public void manageSecurity(List<User> userRecords, MenuItemActionBehaviorParams params) {
	}

	public void manageRole(List<User> userRecords, MenuItemActionBehaviorParams params) {
	}
}
