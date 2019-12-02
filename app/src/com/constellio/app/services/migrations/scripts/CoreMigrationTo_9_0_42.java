package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_0_42 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_42(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_42 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_42(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).get(UserDocument.USER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(UserDocument.SCHEMA_TYPE).get(UserDocument.USER_FOLDER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(UserFolder.SCHEMA_TYPE).get(UserFolder.USER).setCacheIndex(true);
		}
	}
}
