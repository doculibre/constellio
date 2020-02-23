package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_1_12 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.12";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_12(collection, migrationResourcesProvider, appLayerFactory).migrate();

		setupDisplayConfig(collection, appLayerFactory);
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder transactionBuilder = displayManager.newTransactionBuilderFor(collection);

		transactionBuilder.in(Document.SCHEMA_TYPE)
				.addToForm(Document.LINKED_TO)
				.atTheEnd();
		transactionBuilder.in(Document.SCHEMA_TYPE)
				.addToDisplay(Document.LINKED_TO)
				.atTheEnd();

		displayManager.execute(transactionBuilder.build());
	}

	private class SchemaAlterationFor9_1_12 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_12(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			types().getDefaultSchema(Document.SCHEMA_TYPE).create(Document.LINKED_TO)
					.setType(MetadataValueType.REFERENCE)
					.defineReferencesTo(types().getDefaultSchema(Folder.SCHEMA_TYPE))
					.setMultivalue(true)
					.setCacheIndex(true);
		}
	}
}
