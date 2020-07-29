package com.constellio.app.services.users;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.users.UserAddUpdateRequest;

import java.util.List;
import java.util.function.Consumer;

public class AppUserServices {

	AppLayerFactory appLayerFactory;

	public AppUserServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public void execute(String username, Consumer<UserAddUpdateRequest> request, User currentUser) {
		//Execute and log
	}

	public void execute(UserAddUpdateRequest request, User currentUser) {
		//Execute and log
	}

	public List<String> getCollectionsWithSecurityManagementAccess() {
		return null;
	}
}
