package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo8_1_1_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.1.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor8_1_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	class SchemaAlterationFor8_1_1_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "8.1.1.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			// Just to change labels, don't delete this script

		}

	}

}
