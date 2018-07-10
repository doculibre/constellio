package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;

public class ESMigrationTo8_0_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		transaction.add(manager.getMetadata(collection, "connectorInstance_http_documentsPerJobs").withInputType(MetadataInputType.HIDDEN));
		transaction.add(manager.getMetadata(collection, "connectorInstance_http_jobsInParallel").withInputType(MetadataInputType.HIDDEN));

		manager.execute(transaction.build());
	}
}
