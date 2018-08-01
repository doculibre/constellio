package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo8_0_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_8_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
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

	class SchemaAlterationFor_8_0_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_8_0_1(String collection,
											MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder thesaurusConfig = typesBuilder.getSchemaType(ConnectorHttpDocument.SCHEMA_TYPE).getDefaultSchema();
			if (!thesaurusConfig.hasMetadata(ConnectorHttpDocument.THESAURUS_MATCH)) {
				thesaurusConfig.createUndeletable(ConnectorHttpDocument.THESAURUS_MATCH).setType(MetadataValueType.STRING).setMultivalue(true);
			}
			if (!thesaurusConfig.hasMetadata(ConnectorHttpDocument.DOMAIN)) {
				thesaurusConfig.createUndeletable(ConnectorHttpDocument.DOMAIN).setType(MetadataValueType.STRING).setMultivalue(false);
			}
		}
	}
}
