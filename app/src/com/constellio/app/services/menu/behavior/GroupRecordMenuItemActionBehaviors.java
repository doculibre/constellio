package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.AddRemoveUsersFromGroupsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsSelectWindowButton;
import com.constellio.app.modules.rm.ui.buttons.DeleteGroupsWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;

import java.util.List;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;

public class GroupRecordMenuItemActionBehaviors {
	private AppLayerFactory appLayerFactory;
	private ModelLayerFactory modelLayerFactory;
	private UserServices userServices;
	private String collection;

	public GroupRecordMenuItemActionBehaviors(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.userServices = modelLayerFactory.newUserServices();
		this.collection = collection;
	}

	public void edit(Group groupRecords, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editGlobalGroup(groupRecords.getCode());
	}

	public void consult(Group groupRecords, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().displayGlobalGroup(groupRecords.getCode());
	}

	public void addUserToGroup(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		AddRemoveUsersFromGroupsWindowButton addRemoveUsersFromGroupsWindowButton =
				new AddRemoveUsersFromGroupsWindowButton(groupRecords, params, true);
		addRemoveUsersFromGroupsWindowButton.click();
	}

	public void addToCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		List<Record> records = groupRecords.stream().map(group -> group.getWrappedRecord()).collect(Collectors.toList());
		CollectionsSelectWindowButton collectionsSelectWindowButton = new CollectionsSelectWindowButton($("CollectionSecurityManagement.addToCollections"), records, params) {
			@Override
			protected void saveButtonClick(BaseView baseView) {
				List<String> collectionCodes = getSelectedValues();

				for (Record record : records) {
					Group currentGrp = getCore().wrapGroup(record);
					GroupAddUpdateRequest groupAddUpdateRequest = userServices.request(currentGrp.getCode());
					groupAddUpdateRequest.addCollections(collectionCodes);
					userServices.execute(groupAddUpdateRequest);
				}

				baseView.showMessage($("CollectionSecurityManagement.addedGroupToCollections"));
			}
		};

		collectionsSelectWindowButton.addToCollections();
	}

	public void delete(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		DeleteGroupsWindowButton deleteGroupsWindowButton = new DeleteGroupsWindowButton(groupRecords, params);
		deleteGroupsWindowButton.click();
	}

	public void activate(List<Group> groupRecords, MenuItemActionBehaviorParams params, boolean isActivated) {
	}

	public void removeUser(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		AddRemoveUsersFromGroupsWindowButton addRemoveUsersFromGroupsWindowButton =
				new AddRemoveUsersFromGroupsWindowButton(groupRecords, params, false);
		addRemoveUsersFromGroupsWindowButton.click();
	}

	public void manageSecurity(Group group, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().listPrincipalAccessAuthorizations(group.getId());
	}

	public void manageRoles(Group group, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editCollectionUserRoles(group.getId());
	}

	public void removeFromCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
		List<Record> records = groupRecords.stream().map(group -> group.getWrappedRecord()).collect(Collectors.toList());
		CollectionsSelectWindowButton collectionsSelectWindowButton = new CollectionsSelectWindowButton($("CollectionSecurityManagement.removeToCollections"),
				records, params) {
			@Override
			protected void saveButtonClick(BaseView baseView) {
				List<String> collectionCodes = getSelectedValues();

				for (Record record : this.getRecords()) {
					Group currentGrp = getCore().wrapGroup(record);
					GroupAddUpdateRequest groupAddUpdateRequest = userServices.request(currentGrp.getCode());
					groupAddUpdateRequest.removeCollections(collectionCodes.toArray(new String[0]));
					userServices.execute(groupAddUpdateRequest);
				}

				baseView.showMessage($("CollectionSecurityManagement.groupRemovedFromCollection"));

			}
		};

		collectionsSelectWindowButton.addToCollections();
	}
}
