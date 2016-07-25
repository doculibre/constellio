package com.constellio.app.modules.rm.configScripts;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.CalculatorWithManualMetadataChoice;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.sdk.tests.ConstellioTest;

public class EnableOrDisableCalculatorsManualMetadataScriptAcceptanceTest extends ConstellioTest {
	Folder folderWithManualArchivisticMetadataNotNull;
	LocalDate expectedDepositDate, transferDate, destructionDate;

	RMSchemasRecordsServices rm;

	private DemoTestRecords records = new DemoTestRecords(zeCollection);


	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule().withRMTest(records)
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		folderWithManualArchivisticMetadataNotNull = rm.getFolder(records.folder_A01);
		FolderStatus status = folderWithManualArchivisticMetadataNotNull
				.getArchivisticStatus();
		expectedDepositDate = folderWithManualArchivisticMetadataNotNull.getExpectedDepositDate();
		transferDate = folderWithManualArchivisticMetadataNotNull.getExpectedTransferDate();
		destructionDate = folderWithManualArchivisticMetadataNotNull.getExpectedDestructionDate();
	}

	@Test
	public void whenEnablingCalculatorWithManualMetadataDuringImportThenFolderManualArchivisticMetadataAreEnabled()
			throws Exception {
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.ENABLE_DURING_IMPORT);
		assertThatArchivisticManualMetadataAreEnabled();
	}

	@Test
	public void whenEnablingCalculatorWithManualMetadataThenFolderManualArchivisticMetadataAreEnabled()
			throws Exception {
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.ENABLE);
		assertThatArchivisticManualMetadataAreEnabled();
	}

	@Test
	public void whenDisablingCalculatorWithManualMetadataThenFolderManualArchivisticMetadataAreDisabled()
			throws Exception {
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.DISABLE);
		assertThatArchivisticManualMetadataAreDisabled();
	}

	@Test
	public void givenCalculatorWithManualMetadataChoiceIsEnabledWhenDisablingItThenFolderManualArchivisticMetadataAreDisabled()
			throws Exception {
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.ENABLE_DURING_IMPORT);
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.DISABLE);
		assertThatArchivisticManualMetadataAreDisabled();
	}

	@Test
	public void givenCalculatorWithManualMetadataChoiceIsDisabledWhenEnablingItThenFolderManualArchivisticMetadataAreEnabled()
			throws Exception {
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.DISABLE);
		givenConfig(RMConfigs.ARCHIVISTIC_CALCULATORS_WITH_MANUAL_METADATA, CalculatorWithManualMetadataChoice.ENABLE_DURING_IMPORT);
		assertThatArchivisticManualMetadataAreEnabled();

	}

	private void assertThatArchivisticManualMetadataAreEnabled() {
		assertThatArchivisticManualMetadataEnabledIs(true);
	}

	private void assertThatArchivisticManualMetadataAreDisabled() {
		assertThatArchivisticManualMetadataEnabledIs(false);
	}

	private void assertThatArchivisticManualMetadataEnabledIs(boolean enabled) {
		MetadataSchema folderDefaultSchema = getModelLayerFactory()
				.getMetadataSchemasManager()
				.getSchemaTypes(zeCollection).getDefaultSchema(Folder.SCHEMA_TYPE);

		Metadata manualExpectedTransferDate = folderDefaultSchema.getMetadata(Folder.MANUAL_EXPECTED_TRANSFER_DATE);
		assertThat(manualExpectedTransferDate.isEnabled()).isEqualTo(enabled);

		Metadata manualExpectedDepositDate = folderDefaultSchema.getMetadata(Folder.MANUAL_EXPECTED_DEPOSIT_DATE);
		assertThat(manualExpectedDepositDate.isEnabled()).isEqualTo(enabled);

		Metadata manualExpectedDestructionDate = folderDefaultSchema.getMetadata(Folder.MANUAL_EXPECTED_DESTRIUCTION_DATE);
		assertThat(manualExpectedDestructionDate.isEnabled()).isEqualTo(enabled);

		Metadata manualArchivisticStatus = folderDefaultSchema.getMetadata(Folder.MANUAL_ARCHIVISTIC_STATUS);
		assertThat(manualArchivisticStatus.isEnabled()).isEqualTo(enabled);

	}
}
