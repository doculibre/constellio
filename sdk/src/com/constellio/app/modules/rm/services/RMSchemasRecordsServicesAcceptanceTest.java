package com.constellio.app.modules.rm.services;

import org.junit.Before;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class RMSchemasRecordsServicesAcceptanceTest extends ConstellioTest {

	RMTestRecords records = new RMTestRecords(zeCollection);
	Users users = new Users();

	RMSchemasRecordsServices rm;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withConstellioRMModule().withRMTest(records).withAllTest(users));

		rm = new RMSchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

}
