package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_6_6_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.6.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6_6_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_6_6_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_6_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).setSmallCode("f");
			typesBuilder.getSchemaType(Document.SCHEMA_TYPE).setSmallCode("d");
			typesBuilder.getSchemaType(Task.SCHEMA_TYPE).setSmallCode("t");
			typesBuilder.getSchemaType(ContainerRecord.SCHEMA_TYPE).setSmallCode("c");
		}
	}
}
