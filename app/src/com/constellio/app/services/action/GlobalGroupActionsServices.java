package com.constellio.app.services.action;

import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.records.SchemasRecordsServices;

public class GlobalGroupActionsServices {

	private SchemasRecordsServices core;
	private AppLayerSystemExtensions appLayerSystemExtensions;

	public GlobalGroupActionsServices(AppLayerFactory appLayerFactory) {
		this.core = new SchemasRecordsServices(null, appLayerFactory.getModelLayerFactory());
		this.appLayerSystemExtensions = appLayerFactory.getExtensions().getSystemWideExtensions();
	}

	public boolean isAddSubGroupActionPossible(SystemWideGroup group, User user) {

		return group.getStatus() == GlobalGroupStatus.ACTIVE
			   && group.isLocallyCreated()
			   && appLayerSystemExtensions.isAddSubGroupActionPossibleOnGlobalGroup(group, user);
	}

	public boolean isEditActionPossible(SystemWideGroup group, User user) {

		return group.isLocallyCreated()
			   && appLayerSystemExtensions.isEditActionPossibleOnGlobalGroup(group, user);
	}

	public boolean isDeleteActionPossible(SystemWideGroup group, User user) {

		return group.getStatus() == GlobalGroupStatus.ACTIVE
			   && group.isLocallyCreated()
			   && appLayerSystemExtensions.isDeleteActionPossibleOnGlobalGroup(group, user);
	}
}
