package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_4_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.4.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor5_1_4_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor5_1_4_1 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_1_4_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = typesBuilder.getSchema(Folder.DEFAULT_SCHEMA);
			folderSchema.getMetadata(Folder.ARCHIVISTIC_STATUS).setEssentialInSummary(true);

			MetadataSchemaBuilder documentSchema = typesBuilder.getSchema(Document.DEFAULT_SCHEMA);
			documentSchema.getMetadata(Document.CONTENT).setEssentialInSummary(true);
		}
	}

}
