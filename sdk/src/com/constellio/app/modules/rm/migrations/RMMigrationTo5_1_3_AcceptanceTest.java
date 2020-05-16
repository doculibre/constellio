package com.constellio.app.modules.rm.migrations;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;

import java.io.File;

// Confirm @SlowTest
public class RMMigrationTo5_1_3_AcceptanceTest extends ConstellioTest {

	//	@Test
	//	public void givenSystemWithUnfinishedBAtchProcessesThenMigratedToNewFrameworkAndAllBatchProcessFilesDeleted()
	//			throws Exception {
	//
	//		givenSystemAtVersion5_1_2WithUnfinishedBatchProcesses();
	//		waitForBatchProcess();
	//
	//		BatchProcessesManager batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
	//
	//		RMSchemasRecordsServices zeCollection = new RMSchemasRecordsServices("zeCollection", getAppLayerFactory());
	//		RMSchemasRecordsServices anotherCollection = new RMSchemasRecordsServices("anotherCollection", getAppLayerFactory());
	//
	//		assertThat(zeCollection.getFolder("A16").getExpectedDepositDate()).isEqualTo(new LocalDate(2027, 10, 31));
	//		assertThat(anotherCollection.getFolder("0000004330").getExpectedDestructionDate()).isEqualTo(new LocalDate(2013, 10, 31));
	//
	//		// TODO Il faut supprimer tous les configs du dossier de batch processes, sauf list.xml
	//		//	assertThat(getDataLayerFactory().getConfigManager().list("batchProcesses")).containsOnly("list.xml");
	//
	//	}

	private void givenSystemAtVersion5_1_2WithUnfinishedBatchProcesses() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "veryOlds");
		File state = new File(statesFolder, "given_system_in_5.1.2.2_with_tasks,rm_modules__with_unfinished_batch_processes.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
