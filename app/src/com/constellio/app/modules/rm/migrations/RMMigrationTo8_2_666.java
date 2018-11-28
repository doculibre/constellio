package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_2_666 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.666";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_666(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_666 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_666(String collection, MigrationResourcesProvider migrationResourcesProvider,
										AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder defaultFolderSchema = builder.getDefaultSchema(Folder.SCHEMA_TYPE);
			defaultFolderSchema.getMetadata(Folder.MEDIA_TYPE).setEssentialInSummary(true);
		}
	}

}
