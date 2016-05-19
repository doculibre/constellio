package com.constellio.app.ui;

import static java.util.Arrays.asList;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.DemoTestRecords;
import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.MainTest;
import com.constellio.sdk.tests.annotations.MainTestDefaultStart;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
@MainTest
public class StartDemoRMConstellioInEnglishAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records;
	RMSchemasRecordsServices schemas;

	@Before
	public void setUp()
			throws Exception {
		givenSystemLanguageIs("en");
		givenTransactionLogIsEnabled();
		givenCollectionWithTitle(zeCollection, asList("en"), "Collection de test").withConstellioRMModule().withAllTestUsers();
		givenCollectionWithTitle("LaCollectionDeRida", asList("en"), "Collection d'entreprise").withConstellioRMModule()
				.withAllTestUsers();

		recordServices = getModelLayerFactory().newRecordServices();

		records = new RMTestRecords(zeCollection).setup(getAppLayerFactory())
				.withFoldersAndContainersOfEveryStatus();//				.withEvents();
		new DemoTestRecords("LaCollectionDeRida").setup(getModelLayerFactory()).withFoldersAndContainersOfEveryStatus();
	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		//driver.navigateTo().appManagement();
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsChuckNorris()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(chuckNorris, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsDakota()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(dakota, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsRida()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(admin, "LaCollectionDeRida"));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsGandalf()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(gandalf, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsBob()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(bobGratton, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsCharles()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(charlesFrancoisXavier, zeCollection));
		waitUntilICloseTheBrowsers();
	}

	@Test
	public void startOnHomePageAsEdouard()
			throws Exception {
		driver = newWebDriver(loggedAsUserInCollection(edouard, zeCollection));
		waitUntilICloseTheBrowsers();
	}
}
