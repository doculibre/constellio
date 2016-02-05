package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_5_0_5 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {
		new CoreSchemaAlterationFor5_0_4(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor5_0_4 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor5_0_4(String collection,
				MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			addMetadataToUserSchema(typesBuilder);
		}

		private void addMetadataToUserSchema(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(User.DEFAULT_SCHEMA).getMetadata(User.EMAIL).setUniqueValue(false);
		}
	}
}
