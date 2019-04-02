package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_2_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.2.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_2_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_2_3 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder defaultFolderSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			defaultFolderSchema.createUndeletable(Folder.IS_MODEL).setType(MetadataValueType.BOOLEAN).setSystemReserved(true)
					.setDefaultValue(false);

			MetadataSchemaBuilder defaultDocumentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			defaultDocumentSchema.createUndeletable(Document.IS_MODEL).setType(MetadataValueType.BOOLEAN).setSystemReserved(true)
					.setDefaultValue(false);
		}
	}
}
