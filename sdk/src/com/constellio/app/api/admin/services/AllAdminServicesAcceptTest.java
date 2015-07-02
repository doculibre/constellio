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
package com.constellio.app.api.admin.services;

import org.junit.After;
import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.DriverTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
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

	@Test
	public void whenCreateCollectionThenItIsCreated()
			throws Exception {
		runCollectionsManagementAcceptTest().whenCreateCollectionThenItIsCreated();
	}

	@Test
	public void whenUsingDriverThenCanConfigureSchemaTypes()
			throws Exception {
		runSchemaServicesAcceptTest().whenUsingDriverThenCanConfigureSchemaTypes();
	}

	@Test
	public void whenUsingSecurityServicesDriverThenValid()
			throws Exception {
		runSecurityManagementAcceptTest().test();
	}

	@Test
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
