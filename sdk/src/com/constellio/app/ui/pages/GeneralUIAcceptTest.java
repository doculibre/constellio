package com.constellio.app.ui.pages;

import com.constellio.app.modules.rm.navigation.RMNavigationConfiguration;
import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;
import com.constellio.sdk.tests.selenium.adapters.constellio.ConstellioWebDriver;

@UiTest
public class GeneralUIAcceptTest extends ConstellioTest {
	ConstellioWebDriver driver;
	RMSchemasRecordsServices schemas;
	RMTestRecords rm = new RMTestRecords(zeCollection);

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule().withRMTest(rm)
						.withFoldersAndContainersOfEveryStatus().withDocumentsHavingContent(),
				withCollection("otherCollection").withConstellioRMModule().withAllTestUsers()
		);

		schemas = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

	}

	@Test
	@InDevelopmentTest
	public void navigateToDisplayFolder() {
		driver.navigateTo().url(RMNavigationConfiguration.DISPLAY_FOLDER + "/C30");
		waitUntilICloseTheBrowsers();
	}
}