package com.constellio.app.services.users;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;

public class AppUserServices {

	AppLayerFactory appLayerFactory;

	public AppUserServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void addNewColumnInTableForUser(User user, String table, String column) {

	}

	public void removeColumnInTableForUser(User user, String table, String column) {

	}

}
