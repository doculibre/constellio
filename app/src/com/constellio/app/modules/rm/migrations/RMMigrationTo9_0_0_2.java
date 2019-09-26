package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;

public class RMMigrationTo9_0_0_2 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			typesBuilder.getSchemaType(Document.SCHEMA_TYPE).setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.SUB_FOLDERS_TOKENS).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.DOCUMENTS_TOKENS).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Schemas.TOKENS_OF_HIERARCHY.getLocalCode()).setEssentialInSummary(true);
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Schemas.TOKENS_OF_HIERARCHY.getLocalCode()).setEssentialInSummary(true);

			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.FOLDER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE).get(Folder.PARENT_FOLDER).setCacheIndex(true);
			typesBuilder.getDefaultSchema(DecommissioningList.SCHEMA_TYPE).get(DecommissioningList.FOLDERS).setCacheIndex(true);
			typesBuilder.getDefaultSchema(DecommissioningList.SCHEMA_TYPE).get(DecommissioningList.DOCUMENTS).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE).get(Document.FOLDER_CATEGORY).setCacheIndex(true);

			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).get(RMTask.LINKED_FOLDERS).setCacheIndex(true);
			typesBuilder.getDefaultSchema(Task.SCHEMA_TYPE).get(RMTask.LINKED_DOCUMENTS).setCacheIndex(true);
		}
	}
}
