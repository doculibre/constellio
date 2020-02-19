package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_0_47 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.0.0.47";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_0_47(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_0_47 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_0_47(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder folderSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder documentTypeSchema = typesBuilder.getSchemaType(DocumentType.SCHEMA_TYPE).getDefaultSchema();
			folderSchema.create(Document.PUBLISHED_START_DATE)
					.setType(MetadataValueType.DATE);
			folderSchema.create(Document.PUBLISHED_EXPIRATION_DATE)
					.setType(MetadataValueType.DATE);
			MetadataSchemaBuilder documentSchema = typesBuilder.getSchemaType(Document.SCHEMA_TYPE).getDefaultSchema();
		}
	}
}
