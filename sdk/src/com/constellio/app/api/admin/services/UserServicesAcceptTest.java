package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.GlobalGroupResource;
import com.constellio.app.client.entities.UserResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.client.services.UserServicesClient;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.users.UserServices;
import com.constellio.model.services.users.UserServicesRuntimeException;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static com.constellio.sdk.tests.TestUtils.assertThatException;
import static com.constellio.sdk.tests.TestUtils.instanceOf;

public class UserServicesAcceptTest extends ConstellioTest {

	String alicePassword = "p1";
	String bobPassword = "p2";

	String aliceServiceKey;
	String bobServiceKey;

	Users users = new Users();
	UserServices userServices;
	AuthenticationService authService;
	RecordServices recordServices;

	UserServicesClient userServicesClient;
	AdminServicesSession bobSession;
	UserResource bobCredentials;
	UserResource bobCredentialsRefreshed;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection(), withCollection("collection1"));

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();
		recordServices = getModelLayerFactory().newRecordServices();

		users.setUp(userServices, zeCollection);

		userServices.givenSystemAdminPermissionsToUser(users.alice());
		userServices.givenSystemAdminPermissionsToUser(users.bob());

		aliceServiceKey = userServices.giveNewServiceKey(users.alice().getUsername());
		bobServiceKey = userServices.giveNewServiceKey(users.bob().getUsername());

		authService.changePassword(users.alice().getUsername(), alicePassword);
		authService.changePassword(users.bob().getUsername(), bobPassword);

		bobSession = newRestClient(bobServiceKey, users.bob().getUsername(), bobPassword);
		userServicesClient = bobSession.newUserServices();
		bobCredentials = bobSession.schema();
	}

	@Test
	public void whenRemoveAdminFromCollectionThenException() {
		try {
			userServices.execute(userServices.addUpdate(User.ADMIN).removeFromCollection(zeCollection));
			fail();
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin e) {
			// Success
		} catch (Throwable t) {
			fail();
		}
		try {
			userServices.execute(userServices.addUpdate(User.ADMIN).removeFromCollections(zeCollection));
			fail();
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin e) {
			// Success
		} catch (Throwable t) {
			fail();
		}
		try {
			userServices.execute(userServices.addUpdate(User.ADMIN).removeFromAllCollections());
			fail();
		} catch (UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin e) {
			// Success
		} catch (Throwable t) {
			fail();
		}
	}

	@Test
	public void whenSetAdminAnythingButActiveThenException() {
		assertThatException(() -> {
			userServices.execute(userServices.addUpdate(User.ADMIN).setStatusForCollection(UserCredentialStatus.DISABLED, zeCollection));
		}).is(instanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin.class));

		assertThatException(() -> {
			userServices.execute(userServices.addUpdate(User.ADMIN).setStatusForCollection(UserCredentialStatus.PENDING, zeCollection));
		}).is(instanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin.class));

		assertThatException(() -> {
			userServices.execute(userServices.addUpdate(User.ADMIN).setStatusForCollection(UserCredentialStatus.SUSPENDED, zeCollection));
		}).is(instanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin.class));

		assertThatException(() -> {
			userServices.execute(userServices.addUpdate(User.ADMIN).setStatusForAllCollections(UserCredentialStatus.DISABLED));
		}).is(instanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin.class));

		assertThatException(() -> {
			userServices.execute(userServices.addUpdate(User.ADMIN).setStatusForAllCollections(UserCredentialStatus.PENDING));
		}).is(instanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin.class));

		assertThatException(() -> {
			userServices.execute(userServices.addUpdate(User.ADMIN).setStatusForAllCollections(UserCredentialStatus.SUSPENDED));
		}).is(instanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin.class));
	}

	@Test
	public void whenSetBobAnythingButActiveThenNoException() {
		userServices.execute(userServices.addUpdate(bob).setStatusForCollection(UserCredentialStatus.DISABLED, zeCollection));
		
		User zeBob = userServices.getUserInCollection(bob, zeCollection);
		assertThat(zeBob.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);

		userServices.execute(userServices.addUpdate(bob).setStatusForCollection(UserCredentialStatus.PENDING, zeCollection));
		zeBob = userServices.getUserInCollection(bob, zeCollection);
		assertThat(zeBob.getStatus()).isEqualTo(UserCredentialStatus.PENDING);

		userServices.execute(userServices.addUpdate(bob).setStatusForCollection(UserCredentialStatus.SUSPENDED, zeCollection));
		zeBob = userServices.getUserInCollection(bob, zeCollection);
		assertThat(zeBob.getStatus()).isEqualTo(UserCredentialStatus.SUSPENDED);

		userServices.execute(userServices.addUpdate(bob).setStatusForAllCollections(UserCredentialStatus.DISABLED));
		zeBob = userServices.getUserInCollection(bob, zeCollection);
		assertThat(zeBob.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);

		userServices.execute(userServices.addUpdate(bob).setStatusForAllCollections(UserCredentialStatus.PENDING));
		zeBob = userServices.getUserInCollection(bob, zeCollection);
		assertThat(zeBob.getStatus()).isEqualTo(UserCredentialStatus.PENDING);

		userServices.execute(userServices.addUpdate(bob).setStatusForAllCollections(UserCredentialStatus.SUSPENDED));
		zeBob = userServices.getUserInCollection(bob, zeCollection);
		assertThat(zeBob.getStatus()).isEqualTo(UserCredentialStatus.SUSPENDED);
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

		assertThat(resource.getCode()).isEqualTo(userServices.getGroup("heroes").getCode());
		assertThat(resource.getName()).isEqualTo(userServices.getGroup("heroes").getName());
		assertThat(resource.getUsersAutomaticallyAddedToCollections()).isEqualTo(
				userServices.getGroup("heroes").getCollections());
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
