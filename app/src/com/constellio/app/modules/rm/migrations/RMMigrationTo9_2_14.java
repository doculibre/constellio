package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_2_14 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.2.14";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_2_14(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_2_14 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_2_14(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder metadataSchemaBuilder = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			MetadataBuilder metadataBuilderContent = metadataSchemaBuilder.get(Document.CONTENT);
			if(!metadataBuilderContent.isEssentialInSummary()) {
				metadataBuilderContent.setEssentialInSummary(true);
				appLayerFactory.getSystemGlobalConfigsManager().markLocalCachesAsRequiringRebuild();
			}
		}
	}
}
