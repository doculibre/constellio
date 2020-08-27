package com.constellio.app.ui;

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
import org.junit.Before;
import org.junit.Test;

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
		users.setUp(getModelLayerFactory().newUserServices(), zeCollection);


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
