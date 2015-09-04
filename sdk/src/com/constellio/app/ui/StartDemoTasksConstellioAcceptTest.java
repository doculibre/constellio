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
package com.constellio.app.ui;

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
import com.constellio.sdk.tests.setups.Users;

@UiTest
@MainTest
public class StartDemoTasksConstellioAcceptTest extends ConstellioTest {

	RecordServices recordServices;
	ConstellioWebDriver driver;
	RMTestRecords records = new RMTestRecords(zeCollection);
	DemoTestRecords records2 = new DemoTestRecords("LaCollectionDeRida");
	RMSchemasRecordsServices schemas;
	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		givenCollection(zeCollection).withTaskModule().withAllTestUsers();
		users.setUp(getModelLayerFactory().newUserServices());
		//FIXME si actiV schema tache non visible.withConstellioRMModule();

		recordServices = getModelLayerFactory().newRecordServices();

	}

	@Test
	@MainTestDefaultStart
	public void startOnHomePageAsAdmin()
			throws Exception {
		users.adminIn(zeCollection).setCollectionAllAccess(true);
		driver = newWebDriver(loggedAsUserInCollection(admin, zeCollection));
		//driver.navigateTo().appManagement();
		waitUntilICloseTheBrowsers();
	}

}
