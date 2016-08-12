package com.constellio.app.modules.rm.configScripts;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.enums.AllowModificationOfArchivisticStatusAndExpectedDatesChoice;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.sdk.tests.ConstellioTest;

public class EnableOrDisableCalculatorsManualMetadataScriptAcceptanceTest extends ConstellioTest {

	RMSchemasRecordsServices rm;


	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);
		rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
	}

	@Test
	public void whenEnablingCalculatorWithManualMetadataDuringImportThenFolderManualArchivisticMetadataAreEnabled()
			throws Exception {
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED_FOR_IMPORTED_RECORDS);
		assertThatArchivisticManualMetadataAreEnabled();
	}

	@Test
	public void whenEnablingCalculatorWithManualMetadataThenFolderManualArchivisticMetadataAreEnabled()
			throws Exception {
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES, AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED);
		assertThatArchivisticManualMetadataAreEnabled();
	}

	@Test
	public void whenDisablingCalculatorWithManualMetadataThenFolderManualArchivisticMetadataAreDisabled()
			throws Exception {
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES, AllowModificationOfArchivisticStatusAndExpectedDatesChoice.DISABLED);
		assertThatArchivisticManualMetadataAreDisabled();
	}

	@Test
	public void givenCalculatorWithManualMetadataChoiceIsEnabledWhenDisablingItThenFolderManualArchivisticMetadataAreDisabled()
			throws Exception {
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED_FOR_IMPORTED_RECORDS);
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES, AllowModificationOfArchivisticStatusAndExpectedDatesChoice.DISABLED);
		assertThatArchivisticManualMetadataAreDisabled();
	}

	@Test
	public void givenCalculatorWithManualMetadataChoiceIsDisabledWhenEnablingItThenFolderManualArchivisticMetadataAreEnabled()
			throws Exception {
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES, AllowModificationOfArchivisticStatusAndExpectedDatesChoice.DISABLED);
		givenConfig(RMConfigs.ALLOW_MODIFICATION_OF_ARCHIVISTIC_STATUS_AND_EXPECTED_DATES,
				AllowModificationOfArchivisticStatusAndExpectedDatesChoice.ENABLED_FOR_IMPORTED_RECORDS);
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
