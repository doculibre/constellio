package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_5_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.5";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_5_1(collection, provider, factory).migrate();

	}

	public static class SchemaAlterationsFor6_5_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_1(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderDefaultSchema = types().getDefaultSchema(Folder.SCHEMA_TYPE);
			changeFolderExpectedTransfertDateCalculator(folderDefaultSchema, typesBuilder);
			changeFolderExpectedDepositDateCalculator(folderDefaultSchema, typesBuilder);
			changeFolderExpectedDestructionDateCalculator(folderDefaultSchema, typesBuilder);
			changeFolderArchivisticStatusCalculator(folderDefaultSchema, typesBuilder);
		}

		private void changeFolderArchivisticStatusCalculator(MetadataSchemaBuilder folderDefaultSchema,
				MetadataSchemaTypesBuilder typesBuilder) {
			//TODO
			folderDefaultSchema.createUndeletable(Folder.MANUAL_ARCHIVISTIC_STATUS).setType(DATE);
		}

		private void changeFolderExpectedDestructionDateCalculator(MetadataSchemaBuilder folderDefaultSchema,
				MetadataSchemaTypesBuilder typesBuilder) {
			//TODO
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_DESTRIUCTION_DATE).setType(DATE);
		}

		private void changeFolderExpectedDepositDateCalculator(MetadataSchemaBuilder folderDefaultSchema,
				MetadataSchemaTypesBuilder typesBuilder) {
			//TODO
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_DEPOSIT_DATE).setType(DATE);
		}

		private void changeFolderExpectedTransfertDateCalculator(MetadataSchemaBuilder folderDefaultSchema,
				MetadataSchemaTypesBuilder typesBuilder) {
			//TODO
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_TRANSFER_DATE).setType(DATE);
		}
	}
}
