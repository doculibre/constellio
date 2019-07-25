package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_2_1_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.2.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new CoreSchemaAlterationFor_8_2_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}
	}

	class CoreSchemaAlterationFor_8_2_1_1 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_2_1_1(String collection,
												  MigrationResourcesProvider migrationResourcesProvider,
												  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE);
			defaultSchema.get(UserCredential.DN).setUniqueValue(true);
		}
	}
}