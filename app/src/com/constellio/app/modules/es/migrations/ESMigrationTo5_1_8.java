package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;

public class ESMigrationTo5_1_8 extends MigrationHelper implements MigrationScript {

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "5.1.8";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;

		updateFormAndDisplay(collection, appLayerFactory);
		this.migrationResourcesProvider = null;
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		manager.execute(transactionBuilder.build());
	}
}
