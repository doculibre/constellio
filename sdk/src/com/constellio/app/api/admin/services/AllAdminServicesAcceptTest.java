package com.constellio.app.api.admin.services;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import org.junit.After;
import org.junit.Test;

// Comment to test commit
// Confirm @SlowTest
@DriverTest
public class AllAdminServicesAcceptTest extends ConstellioTest {

	ConstellioTest test;

	@Test
	public void whenMultipleSessionsThenDifferentUsers()
			throws Exception {
		runAdminServicesLoginAcceptanceTest().whenMultipleSessionsThenDifferentUsers();
	}

	@Test
	public void whenClosingSessionThenNewTokenIsNowInvalid()
			throws Exception {
		runAdminServicesLoginAcceptanceTest().whenClosingSessionThenNewTokenIsNowInvalid();
	}

	@Test
	public void whenCreatingSessionWithInvalidPasswordThenException()
			throws Exception {
		runAdminServicesLoginAcceptanceTest().whenCreatingSessionWithInvalidPasswordThenException();
	}

	@Test
	public void whenCreatingSessionWithInvalidServiceKeyThenException()
			throws Exception {
		runAdminServicesLoginAcceptanceTest().whenCreatingSessionWithInvalidServiceKeyThenException();
	}

	@Test
	public void whenCreatingSessionWithNonAdminUserThenException()
			throws Exception {
		runAdminServicesLoginAcceptanceTest().whenCreatingSessionWithNonAdminUserThenException();
	}

	@Test
	public void givenServiceKeyIsModifiedThenPreviousServiceKeyDoNotWorkAnymore()
			throws Exception {
		runAdminServicesLoginAcceptanceTest().givenServiceKeyIsModifiedThenPreviousServiceKeyDoNotWorkAnymore();
	}

	//TODO Broken @Test
	public void whenUsingDriverThenCanConfigureSchemaTypes()
			throws Exception {
		runSchemaServicesAcceptTest().whenUsingDriverThenCanConfigureSchemaTypes();
	}

	//TODO Broken @Test
	public void whenUsingSecurityServicesDriverThenValid()
			throws Exception {
		runSecurityManagementAcceptTest().test();
	}


	//TODO Broken @Test
	public void whenUsingUsersServicesDriverThenValid()
			throws Exception {
		runUserServicesAcceptTest().givenBobSession();
	}

	public AdminServicesLoginAcceptanceTest runAdminServicesLoginAcceptanceTest() {
		AdminServicesLoginAcceptanceTest theTest = new AdminServicesLoginAcceptanceTest();
		test = theTest;
		theTest.beforeConstellioTest();
		try {
			theTest.setUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return theTest;
	}

	public CollectionsManagementAcceptTest runCollectionsManagementAcceptTest() {
		CollectionsManagementAcceptTest theTest = new CollectionsManagementAcceptTest();
		test = theTest;
		theTest.beforeConstellioTest();
		try {
			theTest.setUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return theTest;
	}

	public SchemaServicesAcceptTest runSchemaServicesAcceptTest() {
		SchemaServicesAcceptTest theTest = new SchemaServicesAcceptTest();
		test = theTest;
		theTest.beforeConstellioTest();
		try {
			theTest.setUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return theTest;
	}

	public SecurityManagementAcceptTest runSecurityManagementAcceptTest() {
		SecurityManagementAcceptTest theTest = new SecurityManagementAcceptTest();
		test = theTest;
		theTest.beforeConstellioTest();
		try {
			theTest.setUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return theTest;
	}

	public UserServicesAcceptTest runUserServicesAcceptTest() {
		UserServicesAcceptTest theTest = new UserServicesAcceptTest();
		theTest.beforeConstellioTest();
		try {
			theTest.setUp();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		test = theTest;
		return theTest;
	}

	@After
	public void after() {
		test.afterTest(true);
	}

}
