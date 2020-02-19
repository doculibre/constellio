package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_0_1_427 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.427";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreMigrationTo_9_0_1_427.SchemaAlterationFor9_0_1_427(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_427 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_427(String collection, MigrationResourcesProvider migrationResourcesProvider,
											   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			if (userSchema.hasMetadata("visibleTableColumns")) {
				userSchema.deleteMetadataWithoutValidation("visibleTableColumns");
			}
		}
	}
}


