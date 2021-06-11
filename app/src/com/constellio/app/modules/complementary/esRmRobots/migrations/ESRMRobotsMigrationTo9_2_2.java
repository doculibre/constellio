package com.constellio.app.modules.complementary.esRmRobots.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInParentFolderActionParameters;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.robots.model.wrappers.ActionParameters;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo9_2_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor9_2_2(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	class SchemaAlterationFor9_2_2 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor9_2_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (typesBuilder.hasSchemaType(ActionParameters.SCHEMA_TYPE)) {
				MetadataSchemaTypeBuilder folderSchemaType = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE);
				MetadataSchemaBuilder schema = typesBuilder.getSchemaType(ActionParameters.SCHEMA_TYPE).getSchema(ClassifyConnectorFolderInParentFolderActionParameters.SCHEMA_LOCAL_CODE);

				schema.get(ClassifyConnectorFolderInParentFolderActionParameters.DEFAULT_PARENT_FOLDER).removeOldReferences()
						.defineReferencesTo(folderSchemaType);
			}
		}

	}
}
