package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_0_33 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.33";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {

		new SchemaAlterationFor9_1_0_33(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_1_0_33 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_1_0_33(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

			MetadataSchemaBuilder defaultAdminUnitSchema = builder.getDefaultSchema(AdministrativeUnit.SCHEMA_TYPE);
			if (!defaultAdminUnitSchema.hasMetadata(AdministrativeUnit.ABBREVIATION)) {
				defaultAdminUnitSchema.createUndeletable(AdministrativeUnit.ABBREVIATION).setType(MetadataValueType.STRING);

				displayManager.saveSchema(displayManager.getSchema(collection, AdministrativeUnit.DEFAULT_SCHEMA)
						.withNewFormAndDisplayMetadatas(AdministrativeUnit.DEFAULT_SCHEMA + "_" + AdministrativeUnit.ABBREVIATION));
			}

			MetadataSchemaBuilder defaultCategorySchema = builder.getDefaultSchema(Category.SCHEMA_TYPE);
			if (!defaultCategorySchema.hasMetadata(Category.ABBREVIATION)) {
				defaultCategorySchema.createUndeletable(Category.ABBREVIATION).setType(MetadataValueType.STRING);

				displayManager.saveSchema(displayManager.getSchema(collection, Category.DEFAULT_SCHEMA)
						.withNewFormAndDisplayMetadatas(Category.DEFAULT_SCHEMA + "_" + Category.ABBREVIATION));
			}
		}

	}
}