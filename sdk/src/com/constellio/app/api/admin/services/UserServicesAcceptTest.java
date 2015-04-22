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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;

import com.constellio.app.client.entities.GlobalGroupResource;
import com.constellio.app.client.entities.UserResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.client.services.UserServicesClient;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;

public class UserServicesAcceptTest extends ConstellioTest {

	String alicePassword = "p1";
	String bobPassword = "p2";

	String aliceServiceKey;
	String bobServiceKey;

	Users users = new Users();
	UserServices userServices;
	AuthenticationService authService;
	GlobalGroupsManager globalGroupsManager;
	RecordServices recordServices;

	UserServicesClient userServicesClient;
	AdminServicesSession bobSession;
	UserResource bobCredentials;
	UserResource bobCredentialsRefreshed;

	@Before
	public void setUp()
			throws Exception {

		givenCollection("zeCollection");
		givenCollection("collection1");

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();
		globalGroupsManager = getModelLayerFactory().getGlobalGroupsManager();
		recordServices = getModelLayerFactory().newRecordServices();

		users.setUp(userServices);

		userServices.givenSystemAdminPermissionsToUser(users.alice());
		userServices.givenSystemAdminPermissionsToUser(users.bob());

		aliceServiceKey = userServices.giveNewServiceToken(users.alice());
		bobServiceKey = userServices.giveNewServiceToken(users.bob());

		authService.changePassword(users.alice().getUsername(), alicePassword);
		authService.changePassword(users.bob().getUsername(), bobPassword);

		bobSession = newRestClient(bobServiceKey, users.bob().getUsername(), bobPassword);
		userServicesClient = bobSession.newUserServices();
		bobCredentials = bobSession.schema();
	}

	//This test is runned by AllAdminServicesAcceptTest
	public void givenBobSession()
			throws Exception {

		whenAddUpdateUserThenOk();
		whenAddUserToCollectionThenHeIsAddedToCollection();
		whenGenerateServiceKeyAndCreateNewSessionThenOldSessionIsInvalidAndNewSessionIsOk();
		whenAddUpdateGlobalGroupsThenTheyAreAdded();
		whenSetGlobalGroupUsersThenOk();
		whenGetGlobalGroupUsersThenReturnUsernames();
		whenGetGlobalGroupThenReturnIt();
		givenUserInCollectionwhenRemoveUserFromItThenHeIsRemoved();
		whenRemoveGroupThenItIsRemoved();
		whenCreateCollectionGroupThenIsIsCreated();
		whenGetCustomGroupsThenTheyAreReturned();
		whenRemoveCollectionGroupThenIsIsRemoved();
		whenRemoveUserFromGroupThenIsIsRemoved();
	}

	private void whenRemoveUserFromGroupThenIsIsRemoved() {
		assertThat(bobCredentialsRefreshed.getGlobalGroups()).contains("newGroupCode");// newGroupCode

		userServicesClient.removeUserFromGlobalGroup(bobCredentials.getUsername(), "newGroupCode");
		refreshBobCredentials();

		assertThat(bobCredentialsRefreshed.getGlobalGroups()).doesNotContain("newGroupCode");
	}

	private void whenRemoveCollectionGroupThenIsIsRemoved() {
		List<String> groupsInZeCollection = userServicesClient.getCollectionGroups(zeCollection);
		assertThat(groupsInZeCollection).containsOnly("newCustomGroupCode");

		userServicesClient.removeCollectionGroup(zeCollection, "newCustomGroupCode");

		groupsInZeCollection = userServicesClient.getCollectionGroups(zeCollection);
		assertThat(groupsInZeCollection).isEmpty();
	}

	private void whenAddUpdateUserThenOk() {
		bobCredentials.setEmail("bob@gmail.com");
		bobCredentials.setFirstName("bob1");
		bobCredentials.setLastName("Gratton1");
		bobCredentials.setGlobalGroups(Arrays.asList("heroes"));
		bobCredentials.setCollections(Arrays.asList("collection1"));
		bobCredentials.setStatus(UserCredentialStatus.ACTIVE);
		userServicesClient.addUpdateUserCredential(bobCredentials);
		refreshBobCredentials();
		assertThat(bobCredentialsRefreshed.getEmail()).isEqualTo(bobCredentials.getEmail());
		assertThat(bobCredentialsRefreshed.getFirstName()).isEqualTo(bobCredentials.getFirstName());
		assertThat(bobCredentialsRefreshed.getLastName()).isEqualTo(bobCredentials.getLastName());
		assertThat(bobCredentialsRefreshed.getGlobalGroups()).isEqualTo(bobCredentials.getGlobalGroups());
		assertThat(bobCredentialsRefreshed.getServiceKey()).isEqualTo(bobCredentials.getServiceKey()).isNotNull();
		assertThat(bobCredentialsRefreshed.getCollections()).isEqualTo(bobCredentials.getCollections()).isNotNull();
	}

	private void whenAddUserToCollectionThenHeIsAddedToCollection() {
		assertThat(bobCredentialsRefreshed.getCollections()).doesNotContain(zeCollection);

		userServicesClient.addUserToCollection(bobCredentialsRefreshed.getUsername(), zeCollection);
		refreshBobCredentials();

		assertThat(bobCredentialsRefreshed.getCollections()).contains(zeCollection);
	}

	private void whenGenerateServiceKeyAndCreateNewSessionThenOldSessionIsInvalidAndNewSessionIsOk() {
		String oldServiceKey = bobCredentials.getServiceKey();

		String serviceKey = userServicesClient.generateServiceKeyForUser(bobCredentials.getUsername());

		AdminServicesSession bobSession2 = newRestClient(serviceKey, users.bob().getUsername(), bobPassword);
		userServicesClient = bobSession2.newUserServices();
		bobCredentialsRefreshed = userServicesClient.getUser(users.bob().getUsername());
		assertThat(oldServiceKey).isNotEqualTo(serviceKey);
		assertThat(bobCredentialsRefreshed.getServiceKey()).isEqualTo(serviceKey);
		try {
			bobSession.schema();
			fail("Session not killed");
		} catch (Exception e) {
			// Ok
		}
	}

	private void whenAddUpdateGlobalGroupsThenTheyAreAdded() {
		GlobalGroupResource globalGroupResource = new GlobalGroupResource();
		globalGroupResource.setCode("newGroupCode");
		globalGroupResource.setName("newGroupName");
		globalGroupResource.setUsersAutomaticallyAddedToCollections(Arrays.asList(zeCollection));
		globalGroupResource.setStatus(GlobalGroupStatus.ACTIVE);
		userServicesClient.addUpdateGlobalGroup(globalGroupResource);

		GlobalGroupResource retrievedGlobalGroupResource = userServicesClient.getGlobalGroup("newGroupCode");

		assertThat(retrievedGlobalGroupResource.getCode()).isEqualTo("newGroupCode");
		assertThat(retrievedGlobalGroupResource.getName()).isEqualTo("newGroupName");
		assertThat(retrievedGlobalGroupResource.getUsersAutomaticallyAddedToCollections()).isEqualTo(Arrays.asList(zeCollection));
	}

	private void whenSetGlobalGroupUsersThenOk() {
		assertThat(bobCredentials.getGlobalGroups()).doesNotContain("newGroupCode");
		assertThat(bobCredentialsRefreshed.getGlobalGroups()).doesNotContain("newGroupCode");

		userServicesClient.setGlobalGroupUsers("newGroupCode", Arrays.asList(bobCredentials.getUsername()));
		refreshBobCredentials();

		assertThat(bobCredentialsRefreshed.getGlobalGroups()).contains("newGroupCode");
	}

	private void whenGetGlobalGroupUsersThenReturnUsernames() {
		List<String> globalGroupUserNames = userServicesClient.getGlobalGroupUsers("heroes");
		assertThat(globalGroupUserNames).containsOnly(charlesFrancoisXavier, gandalf, dakota, bobGratton);
	}

	private void whenGetGlobalGroupThenReturnIt() {
		GlobalGroupResource resource = userServicesClient.getGlobalGroup("heroes");

		assertThat(resource.getCode()).isEqualTo(globalGroupsManager.getGlobalGroupWithCode("heroes").getCode());
		assertThat(resource.getName()).isEqualTo(globalGroupsManager.getGlobalGroupWithCode("heroes").getName());
		assertThat(resource.getUsersAutomaticallyAddedToCollections()).isEqualTo(
				globalGroupsManager.getGlobalGroupWithCode("heroes").getUsersAutomaticallyAddedToCollections());
	}

	private void givenUserInCollectionwhenRemoveUserFromItThenHeIsRemoved() {
		assertThat(bobCredentialsRefreshed.getCollections()).contains(zeCollection);

		userServicesClient.removeUserFromCollection(users.bob().getUsername(), zeCollection);
		refreshBobCredentials();

		assertThat(bobCredentialsRefreshed.getCollections()).doesNotContain(zeCollection);
	}

	private void whenRemoveGroupThenItIsRemoved() {
		userServicesClient.removeGlobalGroup("heroes");

		try {
			userServicesClient.getGlobalGroup("heroes");
			fail("Group not deleted!");
		} catch (Exception e) {
			// Ok
		}
	}

	private void whenCreateCollectionGroupThenIsIsCreated() {
		userServicesClient.createCollectionGroup(zeCollection, "newCustomGroupCode", "newCustomGroupName");

		assertThat(userServices.getCollectionGroups(zeCollection)).hasSize(1);
		assertThat(userServices.getCollectionGroups(zeCollection).get(0).getCode()).isEqualTo("newCustomGroupCode");
	}

	private void whenGetCustomGroupsThenTheyAreReturned() {
		List<String> groupsInZeCollection = userServicesClient.getCollectionGroups(zeCollection);
		assertThat(groupsInZeCollection).containsOnly("newCustomGroupCode");
	}

	// ---------

	private void refreshBobCredentials() {
		bobCredentialsRefreshed = userServicesClient.getUser(users.bob().getUsername());
	}
}
