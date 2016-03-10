package com.constellio.app.modules.rm.migrations;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo6_1_3_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenOldVersionWhenMigrateTo5_1_9ThenTableConfigurationOk()
			throws Exception {

		givenSystemAtVersion6_1_2();
		waitForBatchProcess();

		// TODO Fetch folder 'Test child incoherence' (id: 00000000400) and check that it's fixed (categoryEntered null)

		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		Folder testFolder = rm.wrapFolder(getModelLayerFactory().newRecordServices().getDocumentById("00000000400"));

		assertThat(testFolder.getCategoryEntered()).isNull();
	}

	private void givenSystemAtVersion6_1_2() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_6.1.2_with_tasks,rm_modules_with_manual_modifications.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}
}
