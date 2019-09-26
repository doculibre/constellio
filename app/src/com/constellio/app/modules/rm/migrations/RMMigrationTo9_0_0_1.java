package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_0_1 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder taskSchema = typesBuilder.getSchemaType(RMTask.SCHEMA_TYPE).getDefaultSchema();

			if (taskSchema.hasMetadata(RMTask.CREATED_AUTHORIZATIONS)) {
				taskSchema.deleteMetadataWithoutValidation(RMTask.CREATED_AUTHORIZATIONS);
			}

			taskSchema.createUndeletable(RMTask.CREATED_AUTHORIZATIONS).setSystemReserved(true)
					.setType(MetadataValueType.STRING).setMultivalue(true);
		}
	}
}
