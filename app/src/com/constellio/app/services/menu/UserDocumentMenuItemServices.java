package com.constellio.app.services.menu;

import com.constellio.app.services.action.UserDocumentActionsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.menu.behavior.MenuItemActionBehaviorParams;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;

public class UserDocumentMenuItemServices {
	enum UserDocumentMenuItemActionType {
		USER_DOCUMENT_CLASSIFY
	}

	private UserDocumentActionsServices userDocumentActionsServices;

	public UserDocumentMenuItemServices(String collection, AppLayerFactory appLayerFactory) {
		this.userDocumentActionsServices = new UserDocumentActionsServices(collection, appLayerFactory);
	}

	public boolean isMenuItemActionPossible(String menuItemActionType, UserDocument userDocument, User user,
											MenuItemActionBehaviorParams params) {
		Record record = userDocument.getWrappedRecord();

		switch (UserDocumentMenuItemActionType.valueOf(menuItemActionType)) {
			case USER_DOCUMENT_CLASSIFY:
				return userDocumentActionsServices.isFileActionPossible(record, user);
			default:
				throw new RuntimeException("Unknown MenuItemActionType : " + menuItemActionType);
		}
	}
}
