package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.BatchProcessReport;

public class CoreMigrationTo_8_1_0_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		displayManager.saveSchema(displayManager.getSchema(collection, BatchProcessReport.FULL_SCHEMA)
				.withRemovedDisplayMetadatas(BatchProcessReport.FULL_SCHEMA + "_" + BatchProcessReport.TITLE)
				.withNewDisplayMetadataQueued(BatchProcessReport.FULL_SCHEMA + "_" + BatchProcessReport.CONTENT));
	}
}