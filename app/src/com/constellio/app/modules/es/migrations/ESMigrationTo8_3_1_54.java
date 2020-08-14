package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance.IGNORE_ROBOTS_TXT;

public class ESMigrationTo8_3_1_54 extends MigrationHelper implements MigrationScript {

	MigrationResourcesProvider migrationResourcesProvider;

	@Override
	public String getVersion() {
		return "8.3.1.54";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.migrationResourcesProvider = migrationResourcesProvider;

		new SchemaAlterationFor8_3_1_54(collection, migrationResourcesProvider, appLayerFactory).migrate();
		updateFormAndDisplay(collection, appLayerFactory);
	}

	private void updateFormAndDisplay(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
		transaction.add(manager.getSchema(collection, ConnectorHttpInstance.SCHEMA_CODE)
				.withNewFormMetadata(ConnectorHttpInstance.SCHEMA_CODE + "_" + IGNORE_ROBOTS_TXT));
		transaction.add(manager.getMetadata(collection, ConnectorHttpInstance.SCHEMA_CODE, IGNORE_ROBOTS_TXT).withMetadataGroup("default:connectors.configurationTab"));

		manager.execute(transaction.build());
	}

	static class SchemaAlterationFor8_3_1_54 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor8_3_1_54(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder types) {
			MetadataSchemaBuilder httpConnectorSchemaType = types.getSchema(ConnectorHttpInstance.SCHEMA_CODE);
			httpConnectorSchemaType.create(IGNORE_ROBOTS_TXT)
					.setType(MetadataValueType.BOOLEAN).setDefaultValue(false);
		}
	}
}
