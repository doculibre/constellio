package com.constellio.sdk.tests;

import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
@MainTest
public class InitialStateSaverAcceptTest extends ConstellioTest {

	@Test
	public void saveCurrentInitialState()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule();

		getSaveStateFeature().saveCurrentStateToInitialStatesFolder();
	}

	@Test
	public void saveModifiedState()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_manual_modifications");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void saveWithEnterpriseSearchModule()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioESModule().withAllTestUsers();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_enterprise_search_module");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void saveModifiedStateStartingFromNewSystem()
			throws Exception {

		givenTransactionLogIsEnabled();
		//		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		//
		getSaveStateFeature().saveStateAfterTestWithTitle("from_new_system");

		newWebDriver();
		waitUntilICloseTheBrowsers();

	}

	@Test
	public void saveStateWithTestRecords()
			throws Exception {

		givenTransactionLogIsEnabled();
		givenCollection(zeCollection).withConstellioRMModule().withAllTestUsers();
		RMTestRecords records = new RMTestRecords(zeCollection);
		records.setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();

		getSaveStateFeature().saveStateAfterTestWithTitle("with_unfinished_batch_processes");

		newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		getModelLayerFactory().getBatchProcessesController().close();

		getModelLayerFactory().getBatchProcessesController().close();
		waitUntilICloseTheBrowsers();

	}

}
