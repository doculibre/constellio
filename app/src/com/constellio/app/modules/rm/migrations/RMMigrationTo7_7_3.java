package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_7_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor7_7_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_7_3 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_7_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.MAIN_COPY_RULE).setDefaultRequirement(true);
		}
	}
}
