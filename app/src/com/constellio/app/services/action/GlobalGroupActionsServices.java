package com.constellio.app.services.action;

import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.services.records.SchemasRecordsServices;

public class GlobalGroupActionsServices {

	private SchemasRecordsServices core;
	private AppLayerSystemExtensions appLayerSystemExtensions;

	public GlobalGroupActionsServices(AppLayerFactory appLayerFactory) {
		this.core = new SchemasRecordsServices(null, appLayerFactory.getModelLayerFactory());
		this.appLayerSystemExtensions = appLayerFactory.getExtensions().getSystemWideExtensions();
	}

	public boolean isAddSubGroupActionPossible(Record record, User user) {
		GlobalGroup group = core.wrapGlobalGroup(record);

		return group.getStatus() == GlobalGroupStatus.ACTIVE
			   && group.isLocallyCreated()
			   && appLayerSystemExtensions.isAddSubGroupActionPossibleOnGlobalGroup(core.wrapGlobalGroup(record), user);
	}

	public boolean isEditActionPossible(Record record, User user) {

		GlobalGroup globalGroup = core.wrapGlobalGroup(record);
		return globalGroup.isLocallyCreated()
			   && appLayerSystemExtensions.isEditActionPossibleOnGlobalGroup(globalGroup, user);
	}

	public boolean isDeleteActionPossible(Record record, User user) {
		GlobalGroup globalGroup = core.wrapGlobalGroup(record);

		return globalGroup.getStatus() == GlobalGroupStatus.ACTIVE
			   && globalGroup.isLocallyCreated()
			   && appLayerSystemExtensions.isDeleteActionPossibleOnGlobalGroup(core.wrapGlobalGroup(record), user);
	}
}
