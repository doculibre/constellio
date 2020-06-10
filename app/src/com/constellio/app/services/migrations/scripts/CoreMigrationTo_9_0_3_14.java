package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_0_3_14 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.3.14";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new CoreSchemaAlterationFor_9_0_3_14(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}
	}

	class CoreSchemaAlterationFor_9_0_3_14 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_9_0_3_14(String collection,
												   MigrationResourcesProvider migrationResourcesProvider,
												   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE);
			MetadataBuilder dn = defaultSchema.get(UserCredential.DN);
			if (!dn.isUniqueValue()) {
				dn.setUniqueValue(true);
			}
		}
	}
}
