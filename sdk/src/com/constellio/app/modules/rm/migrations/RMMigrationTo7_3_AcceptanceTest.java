package com.constellio.app.modules.rm.migrations;

import static com.constellio.sdk.tests.TestUtils.assertThatRecord;
import static java.util.Arrays.asList;

import java.io.File;

import org.assertj.core.api.ListAssert;
import org.junit.Test;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;

public class RMMigrationTo7_3_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemMigratedButBackgroundScriptsHaveNotBeenExecutedThenContainersAreUnmodified()
			throws Exception {
		givenSystemWithContainers();

		assertThatContainerAdmUnits("00000000078").isEqualTo(asList("u1", asList()));
		assertThatContainerAdmUnits("00000000081").isEqualTo(asList("u2", asList("u3")));
		assertThatContainerAdmUnits("00000000084").isEqualTo(asList(null, asList()));
		assertThatContainerAdmUnits("00000000086").isEqualTo(asList("u2", asList("u1", "u2", "u3")));
	}

	@Test
	public void givenSystemMigratedAndBackgroundScriptsHaveBeenExecutedThenContainersModified()
			throws Exception {
		givenSystemWithContainers();

		givenBackgroundThreadsEnabled();
		waitForBatchProcess();

		assertThatContainerAdmUnits("00000000078").isEqualTo(asList(null, asList("u1")));
		assertThatContainerAdmUnits("00000000081").isEqualTo(asList(null, asList("u3", "u2")));
		assertThatContainerAdmUnits("00000000084").isEqualTo(asList(null, asList()));
		assertThatContainerAdmUnits("00000000086").isEqualTo(asList(null, asList("u1", "u2", "u3")));
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
