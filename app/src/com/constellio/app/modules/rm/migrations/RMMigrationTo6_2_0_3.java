package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_2_0_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.2.0.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_2_0_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	public static class SchemaAlterationsFor6_2_0_3 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor6_2_0_3(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).get(Folder.ADMINISTRATIVE_UNIT_ENTERED).setDefaultRequirement(false);
		}

	}
}
