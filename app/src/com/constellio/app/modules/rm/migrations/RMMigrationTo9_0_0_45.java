package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0_0_45 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.0.45";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_0_45_step1_setEssentialInSummary(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_0_45_step1_setEssentialInSummary extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_0_45_step1_setEssentialInSummary(String collection,
																		  MigrationResourcesProvider migrationResourcesProvider,
																		  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataSchemaBuilder metadataSchemaBuilder : typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getAllSchemas()) {

				//These cache insertions are necessary to avoid queries with document add/updates
				metadataSchemaBuilder.get(Folder.OPENING_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.CLOSING_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.ACTUAL_DEPOSIT_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.ACTUAL_DESTRUCTION_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.ACTUAL_TRANSFER_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.EXPECTED_DESTRUCTION_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.EXPECTED_TRANSFER_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Folder.EXPECTED_DEPOSIT_DATE).setEnabled(true);
				metadataSchemaBuilder.get(Schemas.CAPTION).setEnabled(true);
				metadataSchemaBuilder.get(Schemas.SCHEMA_AUTOCOMPLETE_FIELD).setEnabled(true);
				metadataSchemaBuilder.get(Folder.MAIN_COPY_RULE).setEnabled(true);
				metadataSchemaBuilder.get(Schemas.PATH).setEnabled(true);
				metadataSchemaBuilder.get(Schemas.ALL_REMOVED_AUTHS).setEnabled(true);
				metadataSchemaBuilder.get(Schemas.ATTACHED_ANCESTORS).setEnabled(true);
			}

			MetadataSchemaBuilder defaultSchemaaBuilder = typesBuilder.getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();

			//These cache insertions are necessary to avoid queries with document add/updates
			defaultSchemaaBuilder.get(Folder.OPENING_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.CLOSING_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.ACTUAL_DEPOSIT_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.ACTUAL_DESTRUCTION_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.ACTUAL_TRANSFER_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.EXPECTED_DESTRUCTION_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.EXPECTED_TRANSFER_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.EXPECTED_DEPOSIT_DATE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Schemas.CAPTION).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Schemas.SCHEMA_AUTOCOMPLETE_FIELD).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Folder.MAIN_COPY_RULE).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Schemas.PATH).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Schemas.ALL_REMOVED_AUTHS).setEssentialInSummary(true);
			defaultSchemaaBuilder.get(Schemas.ATTACHED_ANCESTORS).setEssentialInSummary(true);
		}
	}
}
