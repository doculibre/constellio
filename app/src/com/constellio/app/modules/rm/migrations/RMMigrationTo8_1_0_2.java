package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDepositDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDestructionDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator2;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_1_0_2 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.0.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new RMMigrationTo8_1_0_2.SchemaAlterationFor8_1_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor8_1_0_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);

			folderSchema.get(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).defineDataEntry()
					.asCalculated(FolderCopyRulesExpectedTransferDatesCalculator2.class);
			folderSchema.get(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).defineDataEntry()
					.asCalculated(FolderCopyRulesExpectedDepositDatesCalculator2.class);
			folderSchema.get(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).defineDataEntry()
					.asCalculated(FolderCopyRulesExpectedDestructionDatesCalculator2.class);
		}
	}
}
