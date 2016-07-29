package com.constellio.app.modules.rm.migrations;

import static com.constellio.model.entities.schemas.MetadataValueType.DATE;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.model.calculators.FolderArchivisticStatusCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDepositDateCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedDestructionDateCalculator2;
import com.constellio.app.modules.rm.model.calculators.FolderExpectedTransferDateCalculator2;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo6_5_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.5.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		new SchemaAlterationsFor6_5_1(collection, provider, factory).migrate();
		modifyArchivisticCalculators(collection, factory);

		makeArchivisticDatesBasedOnNumbers(collection, factory);
	}

	private void makeArchivisticDatesBasedOnNumbers(String collection, AppLayerFactory factory) {
		deleteIfPossibleFolderCalendarYearMetadata();
		deleteIfPossibleFolderCalendarYearEntredMetadata();
		deleteIfPossibleDocumentCalendarYearMetadata();
		deleteIfPossibleDocuemntCalendarYearEntredMetadata();

		//change calculators
	}

	private void deleteIfPossibleDocuemntCalendarYearEntredMetadata() {

	}

	private void deleteIfPossibleDocumentCalendarYearMetadata() {
	}

	private void deleteIfPossibleFolderCalendarYearEntredMetadata() {

	}

	private void deleteIfPossibleFolderCalendarYearMetadata() {
		//TODO
		Metadata metadata = null;
		if(!isUsed(metadata)){
			//folderDefaultSchema.deleteMetadataWithoutValidation(metadata.getOriginalMetadata());
		}

	}

	private boolean isUsed(Metadata metadata) {
		return false;
	}

	private void modifyArchivisticCalculators(String collection, AppLayerFactory factory) {
		factory.getModelLayerFactory().getMetadataSchemasManager().modify(collection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder folderSchemaBuilder = types.getDefaultSchema(Folder.SCHEMA_TYPE);
				folderSchemaBuilder.getMetadata(Folder.EXPECTED_TRANSFER_DATE).defineDataEntry()
						.asCalculated(FolderExpectedTransferDateCalculator2.class);
				folderSchemaBuilder.getMetadata(Folder.EXPECTED_DEPOSIT_DATE).defineDataEntry()
						.asCalculated(FolderExpectedDepositDateCalculator2.class);
				folderSchemaBuilder.getMetadata(Folder.EXPECTED_DESTRUCTION_DATE).defineDataEntry()
						.asCalculated(FolderExpectedDestructionDateCalculator2.class);
				folderSchemaBuilder.getMetadata(Folder.ARCHIVISTIC_STATUS).defineDataEntry()
						.asCalculated(FolderArchivisticStatusCalculator2.class);
			}
		});

	}

	public static class SchemaAlterationsFor6_5_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationsFor6_5_1(String collection, MigrationResourcesProvider provider, AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder folderDefaultSchema = types().getDefaultSchema(Folder.SCHEMA_TYPE);
			MetadataSchemaBuilder documentDefaultSchema = types().getDefaultSchema(Document.SCHEMA_TYPE);
			createArchivisticManualMetadata(folderDefaultSchema);
			createFolderTimeRangeMetadata(folderDefaultSchema);
		}

		private void createFolderTimeRangeMetadata(MetadataSchemaBuilder folderDefaultSchema) {
			folderDefaultSchema.create(Folder.TIME_RANGE).setType(STRING)
					.setInputMask("9999-9999").setEnabled(false);
		}


		private void createArchivisticManualMetadata(MetadataSchemaBuilder folderDefaultSchema) {
			folderDefaultSchema.createUndeletable(Folder.MANUAL_ARCHIVISTIC_STATUS).setType(ENUM).defineAsEnum(FolderStatus.class)
					.setEnabled(false);
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_DESTRIUCTION_DATE).setType(DATE).setEnabled(false);
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_DEPOSIT_DATE).setType(DATE).setEnabled(false);
			folderDefaultSchema.createUndeletable(Folder.MANUAL_EXPECTED_TRANSFER_DATE).setType(DATE).setEnabled(false);
		}
	}
}
