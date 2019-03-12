package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.folder.FolderActualDepositDateCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderActualDestructionDateCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderActualTransferDateCalculator;
import com.constellio.app.modules.rm.model.calculators.folder.FolderOpeningDateCalculator;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_0 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_0 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor9_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder defaultFolderSchema = builder.getDefaultSchema(Folder.SCHEMA_TYPE);

			defaultFolderSchema.get(Folder.OPENING_DATE).defineDataEntry()
					.asCalculated(FolderOpeningDateCalculator.class);
			defaultFolderSchema.get(Folder.ACTUAL_TRANSFER_DATE).defineDataEntry()
					.asCalculated(FolderActualTransferDateCalculator.class);
			defaultFolderSchema.get(Folder.ACTUAL_DEPOSIT_DATE).defineDataEntry()
					.asCalculated(FolderActualDepositDateCalculator.class);
			defaultFolderSchema.get(Folder.ACTUAL_DESTRUCTION_DATE).defineDataEntry()
					.asCalculated(FolderActualDestructionDateCalculator.class);
		}
	}
}
