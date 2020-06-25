package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;


public class RMMigrationTo9_1_10_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.10.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_10_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

		appLayerFactory.getSystemGlobalConfigsManager().markLocalCachesAsRequiringRebuild();
	}

	private class SchemaAlterationFor9_1_10_1 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor9_1_10_1(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();

			documentSchema.get(Document.FILENAME).setAvailableInSummary(true);
		}
	}

}
