package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

//9.0
public class RMMigrationTo9_0_0_4 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_4 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_4(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
			SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection);
			transaction.add(manager.getMetadata(collection, "document_default_description").withInputType(MetadataInputType.TEXTAREA));
			manager.execute(transaction.build());
		}
	}
}
