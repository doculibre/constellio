package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;

public class TasksMigrationTo7_6_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		transaction.add(metadataSchemasDisplayManager.getMetadata(collection, "userTask_default_contents")
													 .withHighlightStatus(true));

		transaction.add(metadataSchemasDisplayManager.getMetadata(collection, "userTask_default_description")
													 .withHighlightStatus(true));

		transaction.add(metadataSchemasDisplayManager.getMetadata(collection, "userTask_default_title")
													 .withHighlightStatus(true));

		metadataSchemasDisplayManager.execute(transaction);
	}
}
