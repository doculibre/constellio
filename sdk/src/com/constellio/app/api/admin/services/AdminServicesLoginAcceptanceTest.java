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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import javax.ws.rs.NotAuthorizedException;

import org.junit.Before;

import com.constellio.app.client.entities.UserResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class AdminServicesLoginAcceptanceTest extends ConstellioTest {

	String alicePassword = "p1";
	String bobPassword = "p2";

	String aliceServiceKey;
	String bobServiceKey;

	Users users = new Users();
	UserServices userServices;
	AuthenticationService authService;

	@Before
	public void setUp()
			throws Exception {

		givenCollection("zeCollection");

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();

		users.setUp(userServices);

		userServices.givenSystemAdminPermissionsToUser(users.alice());
		userServices.givenSystemAdminPermissionsToUser(users.bob());

		aliceServiceKey = userServices.giveNewServiceToken(users.alice());
		bobServiceKey = userServices.giveNewServiceToken(users.bob());

		authService.changePassword(users.alice().getUsername(), alicePassword);
		authService.changePassword(users.bob().getUsername(), bobPassword);
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenMultipleSessionsThenDifferentUsers()
			throws Exception {

		AdminServicesSession aliceSession = newRestClient(aliceServiceKey, users.alice().getUsername(), alicePassword);
		AdminServicesSession bobSession = newRestClient(bobServiceKey, users.bob().getUsername(), bobPassword);

		UserResource aliceCredentials = aliceSession.schema();
		UserResource bobCredentials = bobSession.schema();

		assertThat(aliceCredentials.getUsername()).isEqualTo(users.alice().getUsername());
		assertThat(aliceCredentials.getEmail()).isEqualTo(users.alice().getEmail());
		assertThat(aliceCredentials.getFirstName()).isEqualTo(users.alice().getFirstName());
		assertThat(aliceCredentials.getGlobalGroups()).isEqualTo(users.alice().getGlobalGroups());
		assertThat(aliceCredentials.getLastName()).isEqualTo(users.alice().getLastName());
		assertThat(aliceCredentials.getServiceKey()).isEqualTo(users.alice().getServiceKey());
		assertThat(aliceCredentials.getCollections()).isEqualTo(users.alice().getCollections());
		assertThat(aliceCredentials.isSystemAdmin()).isEqualTo(users.alice().isSystemAdmin());

		assertThat(bobCredentials.getUsername()).isEqualTo(users.bob().getUsername());
		assertThat(bobCredentials.getEmail()).isEqualTo(users.bob().getEmail());
		assertThat(bobCredentials.getFirstName()).isEqualTo(users.bob().getFirstName());
		assertThat(bobCredentials.getGlobalGroups()).isEqualTo(users.bob().getGlobalGroups());
		assertThat(bobCredentials.getLastName()).isEqualTo(users.bob().getLastName());
		assertThat(bobCredentials.getServiceKey()).isEqualTo(users.bob().getServiceKey());
		assertThat(bobCredentials.getCollections()).isEqualTo(users.bob().getCollections());
		assertThat(bobCredentials.isSystemAdmin()).isEqualTo(users.bob().isSystemAdmin());
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenCreatingSessionWithInvalidPasswordThenException()
			throws Exception {

		try {
			newRestClient(aliceServiceKey, users.alice().getUsername(), bobPassword);
			fail("NotAuthorizedException expected");
		} catch (NotAuthorizedException e) {
			// OK
		}

	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenClosingSessionThenNewTokenIsNowInvalid()
			throws Exception {

		AdminServicesSession aliceSession = newRestClient(aliceServiceKey, users.alice().getUsername(), alicePassword);
		AdminServicesSession bobSession = newRestClient(bobServiceKey, users.bob().getUsername(), bobPassword);

		UserResource bobCredentials = aliceSession.schema();
		aliceSession.removeToken();

		try {
			aliceSession.schema();
			fail("NotAuthorizedException expected");
		} catch (NotAuthorizedException e) {
			// OK
		}
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenCreatingSessionWithInvalidServiceKeyThenException()
			throws Exception {

		try {
			newRestClient(bobServiceKey, users.alice().getUsername(), alicePassword);
			fail("NotAuthorizedException expected");
		} catch (NotAuthorizedException e) {
			// OK
		}
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void whenCreatingSessionWithNonAdminUserThenException()
			throws Exception {
		String dakotaPassword = "feufeujolifeu";
		String dakotaServiceKey = userServices.giveNewServiceToken(users.dakotaLIndien());
		authService.changePassword(users.alice().getUsername(), dakotaPassword);

		try {
			newRestClient(dakotaServiceKey, users.dakotaLIndien().getUsername(), dakotaPassword);
			fail("NotAuthorizedException expected");
		} catch (NotAuthorizedException e) {
			// OK
		}
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void givenTokenIsModifiedThenPreviousTokensDoNotWorkAnymore()
			throws Exception {

		AdminServicesSession aliceSession = newRestClient(aliceServiceKey, users.alice().getUsername(), alicePassword);
		newRestClient(aliceServiceKey, users.alice().getUsername(), alicePassword);

		try {
			aliceSession.schema();
			fail("NotAuthorizedException expected");
		} catch (NotAuthorizedException e) {
			// OK
		}
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void givenServiceKeyIsModifiedThenPreviousServiceKeyDoNotWorkAnymore()
			throws Exception {

		AdminServicesSession aliceSession = newRestClient(aliceServiceKey, users.alice().getUsername(), alicePassword);
		aliceSession.newUserServices().generateServiceKeyForUser(users.alice().getUsername());

		try {
			UserResource aliceCredentials = aliceSession.schema();
			fail("NotAuthorizedException expected");
		} catch (NotAuthorizedException e) {
			// OK
		}
	}
}
