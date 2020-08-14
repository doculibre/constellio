package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.metadatas.IllegalCharactersValidator;

public class RMMigrationTo9_0_3_17 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.3.17";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		new SchemaAlterationFor9_0_3_17(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_3_17 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_3_17(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

			MetadataSchemaBuilder defaultFolderSchema = builder.getDefaultSchema(Folder.SCHEMA_TYPE);
			if (!defaultFolderSchema.hasMetadata(Folder.ABBREVIATION)) {
				defaultFolderSchema.createUndeletable(Folder.ABBREVIATION).setType(MetadataValueType.STRING);

				displayManager.saveSchema(displayManager.getSchema(collection, Folder.DEFAULT_SCHEMA)
						.withNewFormAndDisplayMetadatas(Folder.DEFAULT_SCHEMA + "_" + Folder.ABBREVIATION));
			}

			defaultFolderSchema.get(Schemas.ABBREVIATION).addValidator(IllegalCharactersValidator.class);
		}

	}
}