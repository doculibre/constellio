package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;

public class ESMigrationTo6_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		configureDisplayConfig(factory.getMetadataSchemasDisplayManager(), collection);
	}

	private void configureDisplayConfig(SchemasDisplayManager manager, String collection) {
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		transaction.add(manager.getMetadata(collection, ConnectorHttpDocument.DEFAULT_SCHEMA, ConnectorHttpDocument.CONNECTOR)
				.withVisibleInAdvancedSearchStatus(true));

		manager.execute(transaction.build());
	}
}
