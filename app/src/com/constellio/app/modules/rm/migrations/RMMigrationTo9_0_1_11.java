package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_1_11 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.11";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_1_11(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_11 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_11(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			folderSchema.get(Folder.TITLE).setCacheIndex(true);

			MetadataSchemaBuilder documentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			documentSchema.get(Document.TITLE).setCacheIndex(true);
			documentSchema.get(Document.CONTENT_CHECKED_OUT_BY).setCacheIndex(true);
		}
	}
}
