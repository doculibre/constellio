package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_46 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.46";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_46(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_46 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_46(String collection, MigrationResourcesProvider migrationResourcesProvider,
								  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();
			folderSchema.get(Schemas.ATTACHED_ANCESTORS).setEssentialInSummary(false);
			folderSchema.get(Schemas.DESCRIPTION_TEXT).setEssentialInSummary(false);
			folderSchema.get(Schemas.PATH_PARTS).setEssentialInSummary(false).setCacheIndex(false);

			//TODO : Should not exist!
			folderSchema.get(Folder.ACTIVE_RETENTION_CODE).setEssentialInSummary(false);
			folderSchema.get(Folder.SEMIACTIVE_RETENTION_CODE).setEssentialInSummary(false);

			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			documentSchema.get(Schemas.ATTACHED_ANCESTORS).setEssentialInSummary(false);
			documentSchema.get(Document.FOLDER_CATEGORY).setCacheIndex(false);
			documentSchema.get(Schemas.PATH_PARTS).setEssentialInSummary(false).setCacheIndex(false);

		}
	}
}
