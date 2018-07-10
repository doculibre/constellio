package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class ESMigrationTo8_0 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_0(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);

		transaction.add(manager.getMetadata(collection, "connectorHttpDocument_default_description").withMetadataGroup("").withInputType(MetadataInputType.TEXTAREA).withHighlightStatus(true).withVisibleInAdvancedSearchStatus(true));

		manager.execute(transaction.build());
	}

	class SchemaAlterationFor8_0 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder schema = typesBuilder.getDefaultSchema(ConnectorHttpDocument.SCHEMA_TYPE);

			schema.create(ConnectorHttpDocument.DESCRIPTION).setType(STRING).setSearchable(true);
		}
	}
}
