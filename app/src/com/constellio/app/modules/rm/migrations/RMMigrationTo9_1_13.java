package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.schemas.Schemas;

public class RMMigrationTo9_1_13 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.14";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		displayManager.saveSchema(displayManager.getSchema(collection, Event.DEFAULT_SCHEMA)
				.withNewTableMetadatas(Event.DEFAULT_SCHEMA + "_" + Schemas.CREATED_ON.getLocalCode()).withRemovedTableMetadatas(Event.DEFAULT_SCHEMA + "_" + Schemas.MODIFIED_ON.getLocalCode()));
	}
}
