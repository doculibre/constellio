package com.constellio.app.services.menu;

import com.constellio.app.services.action.UserFolderActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserFolder;

public class UserFolderMenuItemServices {
	enum UserFolderMenuItemActionType {
		USER_FOLDER_CLASSIFY
	}

	private UserFolderActionsServices userFolderActionsServices;

	public UserFolderMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.userFolderActionsServices = new UserFolderActionsServices(collection, appLayerFactory);
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, UserFolder userDocument, User user,
											MenuItemActionBehaviorParams params) {
		Record record = userDocument.getWrappedRecord();

		switch (UserFolderMenuItemActionType.valueOf(menuItemActionType)) {
			case USER_FOLDER_CLASSIFY:
				return userFolderActionsServices.isFileActionPossible(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}
}
