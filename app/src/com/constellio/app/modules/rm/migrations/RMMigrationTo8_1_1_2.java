package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDepositDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedDestructionDatesCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator;
import com.constellio.app.modules.rm.model.calculators.FolderCopyRulesExpectedTransferDatesCalculator2;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_1_1_2 extends MigrationHelper implements MigrationScript {
	private static final String FOLDER_DECOMMISSIONING_DATE = "decommissioningDate";

	@Override
	public String getVersion() {
		return "8.1.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor8_1_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor8_1_1_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1_1_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);

			if (folderSchema.hasMetadata(FOLDER_DECOMMISSIONING_DATE)) {
				folderSchema.get(FOLDER_DECOMMISSIONING_DATE).setEssential(false).setEnabled(false)
						.defineDataEntry().asManual();
			}

			boolean defaultDateCalculators = ((CalculatedDataEntry) folderSchema
					.get(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).getDataEntry()).getCalculator()
					instanceof FolderCopyRulesExpectedTransferDatesCalculator;

			if (defaultDateCalculators) {
				folderSchema.get(Folder.COPY_RULES_EXPECTED_TRANSFER_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedTransferDatesCalculator2.class);
				folderSchema.get(Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedDepositDatesCalculator2.class);
				folderSchema.get(Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES).defineDataEntry()
						.asCalculated(FolderCopyRulesExpectedDestructionDatesCalculator2.class);
			}
		}
	}
}