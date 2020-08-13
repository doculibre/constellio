package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_1_0_30 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.0.30";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_0_30(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_0_30 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_0_30(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			documentSchema.createUndeletable(Document.ENCRYPTION_KEY).setType(MetadataValueType.STRING)
					.setSystemReserved(true).setAvailableInSummary(true);
		}
	}
}
