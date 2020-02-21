package com.constellio.app.services.extensions.core;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordPhysicalDeletionEvent;
import com.constellio.model.services.configs.UserConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;

public class CoreUserRecordExtension extends RecordExtension {
	private SchemasRecordsServices schemasRecordsServices;
	private UserConfigurationsManager userConfigsManager;

	public CoreUserRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
		userConfigsManager = modelLayerFactory.getUserConfigurationsManager();
	}

	@Override
	public void recordPhysicallyDeleted(RecordPhysicalDeletionEvent event) {
		if (!User.SCHEMA_TYPE.equals(event.getRecord().getTypeCode())) {
			return;
		}

		User user = schemasRecordsServices.wrapUser(event.getRecord());
		userConfigsManager.deleteConfigurations(user);
	}
}
