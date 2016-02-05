package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorDocumentInFolderActionParameters;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifySmbFolderInFolderActionParameters;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo5_1_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	class SchemaAlterationFor5_1_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor5_1_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "5.1.2";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			setupClassifySmbDocumentInFolderActionParametersSchema();
			setupClassifySmbFolderInFolderActionParametersSchema();
		}

		private void setupClassifySmbDocumentInFolderActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.createCustomSchema(ClassifyConnectorDocumentInFolderActionParameters.SCHEMA_LOCAL_CODE);
			schema.create(ClassifyConnectorDocumentInFolderActionParameters.IN_FOLDER).setDefaultRequirement(true)
					.defineReferencesTo(schemaType(Folder.SCHEMA_TYPE));
			schema.create(ClassifyConnectorDocumentInFolderActionParameters.MAJOR_VERSIONS).setDefaultRequirement(true)
					.setType(BOOLEAN).setDefaultValue(Boolean.TRUE);
		}

		private void setupClassifySmbFolderInFolderActionParametersSchema() {
			MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE)
					.createCustomSchema(ClassifySmbFolderInFolderActionParameters.SCHEMA_LOCAL_CODE);
			schema.create(ClassifySmbFolderInFolderActionParameters.IN_FOLDER).setDefaultRequirement(true)
					.defineReferencesTo(schemaType(Folder.SCHEMA_TYPE));
			schema.create(ClassifySmbFolderInFolderActionParameters.MAJOR_VERSIONS).setDefaultRequirement(true)
					.setType(BOOLEAN).setDefaultValue(Boolean.TRUE);
		}

	}
}
