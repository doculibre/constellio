package com.constellio.app.modules.rm.migrations;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo6_1_3_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenOldVersionWhenMigrateTo5_1_9ThenTableConfigurationOk()
			throws Exception {

		givenSystemAtVersion6_1_2();
		waitForBatchProcess();

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		Folder testFolder = rm.wrapFolder(getModelLayerFactory().newRecordServices().getDocumentById("00000000400"));

		assertThat(testFolder.getCategoryEntered()).isNull();
		assertThat(getAppLayerFactory().getSystemGlobalConfigsManager().isReindexingRequired()).isTrue();
	}

	private void givenSystemAtVersion6_1_2() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_6.1.2_with_tasks,rm_modules_with_manual_modifications.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
