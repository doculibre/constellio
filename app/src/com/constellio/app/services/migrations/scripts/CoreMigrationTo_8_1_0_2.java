package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_1_0_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		//for i18ns
		new SchemaAlterationsFor8_1_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private static class SchemaAlterationsFor8_1_0_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor8_1_0_2(String collection, MigrationResourcesProvider provider,
											  AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
		}
	}
}