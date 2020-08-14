package com.constellio.app.services.menu.behavior;

import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton;
import com.constellio.app.modules.rm.ui.buttons.CollectionsWindowButton.AddedToCollectionRecordType;
import com.constellio.app.modules.rm.ui.buttons.UsersAddToGroupsWindowButton;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.buttons.DeleteButton;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import com.vaadin.ui.Button;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	private Map<String, String> clone(Map<String, String> map) {
		if (map == null) {
			return null;
		}

		Map<String, String> newMap = new HashMap<>();

		newMap.putAll(map);

		return newMap;
	}


	public void edit(Group groupRecords, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editGlobalGroup(groupRecords.getCode());
	}

	public void consult(Group groupRecords, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().displayGlobalGroup(groupRecords.getCode());
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
		Button deleteUserButton = new DeleteButton($("CollectionSecurityManagement.deleteGroups"), false) {
			@Override
			protected void confirmButtonClick(ConfirmDialog dialog) {
				deleteGroupFromCollection(groupRecords);
				params.getView().navigate().to().collectionSecurityShowGroupFirst();
				params.getView().showMessage($("CollectionSecurityManagement.groupRemovedFromCollection"));
			}

			@Override
			protected String getConfirmDialogMessage() {
				return $("ConfirmDialog.confirmDeleteWithAllRecords", $("CollectionSecurityManagement.groupLowerCase"));
			}
		};

		deleteUserButton.click();
	}

	public void deleteGroupFromCollection(List<Group> userRecords) {
		for (Group currentGroup : userRecords) {
			GroupAddUpdateRequest userAddUpdateRequest = userServices.request(currentGroup.getCode());
			userAddUpdateRequest.removeCollection(collection);
			userServices.execute(userAddUpdateRequest);
		}
	}

	public void activate(List<Group> groupRecords, MenuItemActionBehaviorParams params, boolean isActivated) {
	}

	public void removeUser(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}

	public void manageSecurity(Group group, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().listPrincipalAccessAuthorizations(group.getId());
	}

	public void manageRoles(Group group, MenuItemActionBehaviorParams params) {
		params.getView().navigate().to().editCollectionUserRoles(group.getId());
	}

	public void removeFromCollection(List<Group> groupRecords, MenuItemActionBehaviorParams params) {
	}
}
