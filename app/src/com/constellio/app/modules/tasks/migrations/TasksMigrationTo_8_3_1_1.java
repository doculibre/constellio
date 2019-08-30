package com.constellio.app.modules.tasks.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class TasksMigrationTo_8_3_1_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.1.1";
	}


	@Override
	public void migrate(final String collection, MigrationResourcesProvider provider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor8_3_1_1(collection, provider, appLayerFactory);

	}

	private class CoreSchemaAlterationFor8_3_1_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_3_1_1(String collection,
											  MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

		}
	}
}
