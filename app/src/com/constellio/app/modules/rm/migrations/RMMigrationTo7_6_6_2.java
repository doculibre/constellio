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
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import java.util.List;

import static java.util.Arrays.asList;

public class RMMigrationTo7_6_6_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.6.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_6_6_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor7_6_6_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_6_6_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			List<String> typesToAddEssentialMetadatas = asList(Folder.SCHEMA_TYPE, Document.SCHEMA_TYPE, ContainerRecord.SCHEMA_TYPE);
			for(String type: typesToAddEssentialMetadatas) {
				typesBuilder.getDefaultSchema(type).getMetadata(Schemas.LOGICALLY_DELETED_STATUS.getLocalCode()).setEssentialInSummary(true);
			}
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).getMetadata(Document.FOLDER_ARCHIVISTIC_STATUS).setEssentialInSummary(true);
		}
	}
}
