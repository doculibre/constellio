package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.model.enums.DecommissioningType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.assertj.core.api.ListAssert;
import org.junit.Test;

import java.io.File;

import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo7_3_AcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);

	@Test
	public void givenNewSystemWithRMModuleThenNoAdministrativeUnitMetadata()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioRMModule()
		);

		givenBackgroundThreadsEnabled();
		waitForBatchProcess();
		getConstellioFactories().getModelLayerFactory().getRecordMigrationsManager().checkScriptsToFinish();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(rm.containerRecord.schema().hasMetadataWithCode("administrativeUnit")).isFalse();
	}

	@Test
	public void givenSystemWithESModuleWhenInstallingRMModuleThenNoAdministrativeUnitMetadata()
			throws Exception {

		prepareSystem(
				withZeCollection().withConstellioESModule().withConstellioRMModule().withFoldersAndContainersOfEveryStatus().withRMTest(records)
		);

		getAppLayerFactory().getModulesManager().installValidModuleAndGetInvalidOnes(new ConstellioRMModule(),
				getModelLayerFactory().getCollectionsListManager());
		getAppLayerFactory().getModulesManager().enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioRMModule());

		givenBackgroundThreadsEnabled();
		waitForBatchProcess();
		getConstellioFactories().getModelLayerFactory().getRecordMigrationsManager().checkScriptsToFinish();
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		assertThat(rm.containerRecord.schema().hasMetadataWithCode("administrativeUnit")).isFalse();

		ContainerRecordType containerRecordType = rm.newContainerRecordType().setCode("test").setTitle("test");
		rm.getModelLayerFactory().newRecordServices().add(containerRecordType);

		ContainerRecord record = rm.newContainerRecord().setIdentifier("identifier").setType(containerRecordType)
				.setDecommissioningType(DecommissioningType.DEPOSIT).setAdministrativeUnits(asList(records.unitId_10a));
		getModelLayerFactory().newRecordServices().add(record);
		assertThat(record.<Double>get(Schemas.MIGRATION_DATA_VERSION)).isGreaterThan(0.0);
	}

	private ListAssert<Object> assertThatContainerAdmUnits(String id) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		ContainerRecord container = rm.getContainerRecord(id);
		return assertThatRecord(container).extracting("administrativeUnit.code", "administrativeUnits.code");
	}

	private void givenSystemWithContainers() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_7.2.0.4_with_tasks,rm_modules__with_manual_modifications.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

}
