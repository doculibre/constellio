package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo5_1_6 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_6(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor5_1_6 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_1_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.1.6";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifyConnectorTaxonomyActionParametersSchema();
		}

		private void setupClassifyConnectorTaxonomyActionParametersSchema() {

			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.getCustomSchema(ClassifyConnectorFolderInTaxonomyActionParameters.SCHEMA_LOCAL_CODE);
			schema.get(ClassifyConnectorFolderInTaxonomyActionParameters.IN_TAXONOMY).setDefaultRequirement(false);
		}

	}


}
