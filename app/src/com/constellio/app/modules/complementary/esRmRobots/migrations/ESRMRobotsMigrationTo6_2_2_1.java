package com.constellio.app.modules.complementary.esRmRobots.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESRMRobotsMigrationTo6_2_2_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.2.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationsFor6_2_2_1(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	public static class SchemaAlterationsFor6_2_2_1 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor6_2_2_1(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(Folder.DEFAULT_SCHEMA).createSystemReserved(Folder.CREATED_BY_ROBOT).setType(STRING);
			typesBuilder.getSchema(Document.DEFAULT_SCHEMA).createSystemReserved(Document.CREATED_BY_ROBOT).setType(STRING);
		}

	}
}
