package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
<<<<<<< HEAD
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
=======
import com.constellio.app.modules.rm.wrappers.RMUser;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
>>>>>>> origin/9.1-earlyAprilMerge
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_1_0_23 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.1.0.23";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_1_0_23(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_0_23 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor9_1_0_23(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();

			documentSchema.get(Document.FILENAME).setAvailableInSummary(true);

			typesBuilder.getDefaultSchema(RMUser.SCHEMA_TYPE).createUndeletable(RMUser.FAVORITES_DISPLAY_ORDER)
					.setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);
		}
	}

}
