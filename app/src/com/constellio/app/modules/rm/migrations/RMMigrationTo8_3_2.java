package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_3_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new RMMigrationTo8_3_2.SchemaAlterationFor8_3_2(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_3_2 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_3_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
										AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
		}
	}
}
