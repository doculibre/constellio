package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;

public class ESMigrationTo8_0_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		updateFormAndDisplay(collection, appLayerFactory);
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(collection);

		transactionBuilder.in(ConnectorHttpInstance.SCHEMA_TYPE)
				.addToDisplay(ConnectorHttpInstance.ON_DEMANDS)
				.atTheEnd();

		transactionBuilder.in(ConnectorHttpInstance.SCHEMA_TYPE)
				.addToForm(ConnectorHttpInstance.ON_DEMANDS)
				.atTheEnd();

		manager.execute(transactionBuilder.build());
	}
}
