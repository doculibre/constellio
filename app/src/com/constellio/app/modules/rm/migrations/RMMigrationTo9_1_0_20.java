package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class RMMigrationTo9_1_0_20 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.0.20";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_0_20(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_0_20 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_0_20(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder decomListSchema = typesBuilder.getSchemaType(DecommissioningList.SCHEMA_TYPE).getDefaultSchema();
			decomListSchema.createSystemReserved(DecommissioningList.CURRENT_BATCH_PROCESS_ID).setType(STRING);

			MetadataSchemaBuilder emailSchema = typesBuilder.getSchemaType(Email.SCHEMA_TYPE).getCustomSchema(Email.SCHEMA_LOCAL_CODE);
			emailSchema.createSystemReserved(Email.EMAIL_VERSIONS).setType(STRING).setMultivalue(true);
		}
	}
}
