/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.pages;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.ui.application.NavigatorConfigurationService;
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

		schemas = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());

		driver = newWebDriver(loggedAsUserInCollection("admin", zeCollection));

	}

	@Test
	@InDevelopmentTest
	public void navigateToDisplayFolder() {
		driver.navigateTo().url(NavigatorConfigurationService.DISPLAY_FOLDER + "/C30");
		waitUntilICloseTheBrowsers();
	}
}