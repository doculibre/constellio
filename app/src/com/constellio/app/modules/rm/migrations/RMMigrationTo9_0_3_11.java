package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.validators.metadatas.IllegalCharactersValidator;

public class RMMigrationTo9_0_3_11 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.3.11";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_0_3_11(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0_3_11 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_0_3_11(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentSchema = typesBuilder.getDefaultSchema(Document.SCHEMA_TYPE);
			documentSchema.get(Schemas.TITLE).addValidator(IllegalCharactersValidator.class);

			MetadataSchemaBuilder folderSchema = typesBuilder.getDefaultSchema(Folder.SCHEMA_TYPE);
			folderSchema.get(Schemas.TITLE).addValidator(IllegalCharactersValidator.class);

			MetadataSchemaBuilder adminUnitSchema = typesBuilder.getDefaultSchema(AdministrativeUnit.SCHEMA_TYPE);
			adminUnitSchema.get(Schemas.TITLE).addValidator(IllegalCharactersValidator.class);
			adminUnitSchema.get(Schemas.CODE).addValidator(IllegalCharactersValidator.class);
			adminUnitSchema.get(Schemas.ABBREVIATION).addValidator(IllegalCharactersValidator.class);

			MetadataSchemaBuilder categorySchema = typesBuilder.getDefaultSchema(Category.SCHEMA_TYPE);
			categorySchema.get(Schemas.TITLE).addValidator(IllegalCharactersValidator.class);
			categorySchema.get(Schemas.CODE).addValidator(IllegalCharactersValidator.class);
			categorySchema.get(Schemas.ABBREVIATION).addValidator(IllegalCharactersValidator.class);
		}
	}
}
