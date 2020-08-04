package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.AuthorizationResource;
import com.constellio.app.client.entities.GroupCollectionPermissionsResource;
import com.constellio.app.client.entities.UserCollectionPermissionsResource;
import com.constellio.app.client.entities.UserResource;
import com.constellio.app.client.services.AdminServicesSession;
import com.constellio.app.client.services.SecurityManagementDriver;
import com.constellio.app.client.services.UserServicesClient;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordDeleteServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.ContentPermissions;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.FolderSchema;
import com.constellio.model.services.security.SecurityAcceptanceTestSetup.Records;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.junit.Before;

import java.util.Arrays;
import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityManagementAcceptTest extends ConstellioTest {

	String alicePassword = "p1";
	String bobPassword = "p2";
	String newAlicePassword = "p3";

	String aliceServiceKey;
	String bobServiceKey;

	UserServices userServices;
	AuthenticationService authService;
	RecordServices recordServices;

	SecurityManagementDriver securityManagementDriver;
	AdminServicesSession bobSession;
	UserServicesClient userServicesClient;
	UserResource bobCredentials;

	RecordDeleteServices recordDeleteServices;
	SecurityAcceptanceTestSetup schemas = new SecurityAcceptanceTestSetup(zeCollection);
	FolderSchema folderSchema = schemas.new FolderSchema();
	MetadataSchemasManager schemasManager;
	SearchServices searchServices;
	TaxonomiesManager taxonomiesManager;
	CollectionsListManager collectionsListManager;
	AuthorizationsServices authorizationsServices;

	Records records;
	Users usersRecords = new Users();
	RolesManager roleManager;

	User bob, alice;

	SystemWideUserInfos userCredentialBob, userCredentialAlice;

	String authId;

	Calendar calendarStartDate = Calendar.getInstance();
	Calendar calendarEndDate = Calendar.getInstance();

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection().withAllTest(usersRecords));

		userServices = getModelLayerFactory().newUserServices();
		authService = getModelLayerFactory().newAuthenticationService();
		recordServices = getModelLayerFactory().newRecordServices();
		recordServices = getModelLayerFactory().newRecordServices();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		authorizationsServices = getModelLayerFactory().newAuthorizationsServices();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		roleManager = getModelLayerFactory().getRolesManager();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		userServices = getModelLayerFactory().newUserServices();

		defineSchemasManager().using(schemas);
		taxonomiesManager.addTaxonomy(schemas.getTaxonomy1(), schemasManager);
		taxonomiesManager.addTaxonomy(schemas.getTaxonomy2(), schemasManager);
		taxonomiesManager.setPrincipalTaxonomy(schemas.getTaxonomy1(), schemasManager);
		records = schemas.givenRecords(recordServices);

		usersRecords.setUp(userServices);
		bob = usersRecords.bobIn(zeCollection);
		alice = usersRecords.aliceIn(zeCollection);
		userCredentialBob = usersRecords.bob();
		userCredentialAlice = usersRecords.alice();
		userServices.execute(userCredentialAlice.getUsername(), (req) -> req.addToCollection(zeCollection));

		userServices.givenSystemAdminPermissionsToUser(userCredentialBob);
		bobServiceKey = userServices.giveNewServiceKey(userCredentialBob.getUsername());

		authService.changePassword(userCredentialBob.getUsername(), bobPassword);

		bobSession = newRestClient(bobServiceKey, userCredentialBob.getUsername(), bobPassword);
		securityManagementDriver = bobSession.manageCollectionSecurity(zeCollection);
		userServicesClient = bobSession.newUserServices();
		bobCredentials = bobSession.schema();
		//
		calendarStartDate.set(2014, Calendar.JANUARY, 01, 0, 0, 0);
		calendarEndDate.set(2020, Calendar.JANUARY, 01, 0, 0, 0);

	}

	//This test is runned by AllAdminServicesAcceptTest
	public void test()
			throws Exception {

		whenChangePasswordThenOk();
		whenChangeOldPasswordThenOk();
		whenAddAuthorizationThenCanGetIt();
		whenRemoveAuthorizationOnRecordThenItIsRemoved();
		whenModifyAuthorizationThenItIsModified();
		givenAuthorizationToAliceInFolder2WhenHasPermissionThenOk();
		whenHasDeletePermissionOnHierarchyThenOk();
		whenCanReadCanWriteCanDeleteThenOk();
		givenDeleteRecordWhenHasRestaurationPermissionOnHierarchyThenOk();
		givenAuthorizationToAliceInFond1WhenHasDeletePermissionOnPrincipalConceptHierarchyThenOk();
		whenResetThenOk();
		givenCollectionPermissionToBobWhenGetUserCollectionPermissionsThenReturnIt();
		givenGroupInCollectionAndCollectionPermissionToLegendsWhenGetGroupCollectionPermissionsThenReturnIt();

	}

	private void givenGroupInCollectionAndCollectionPermissionToLegendsWhenGetGroupCollectionPermissionsThenReturnIt() {

		GroupAddUpdateRequest globalGroup = userServices.createGlobalGroup(
				"legends", "legends", Arrays.asList(zeCollection), null, GlobalGroupStatus.ACTIVE, true);
		userServices.execute(globalGroup);

		GroupCollectionPermissionsResource resource = new GroupCollectionPermissionsResource();
		resource.setGroupCode("legends");
		resource.setName("legends Name");
		resource.setRoles(Arrays.asList("zeWriteRole"));
		securityManagementDriver.setGroupCollectionPermissions(resource);

		GroupCollectionPermissionsResource retrievedResource = securityManagementDriver.getGroupCollectionPermissions("legends");

		assertThat(retrievedResource.getGroupCode()).isEqualTo("legends");
		assertThat(retrievedResource.getName()).isEqualTo("legends Name");
		assertThat(retrievedResource.getRoles()).containsOnly("zeWriteRole");
	}

	private void givenCollectionPermissionToBobWhenGetUserCollectionPermissionsThenReturnIt()
			throws RecordServicesException {

		UserCollectionPermissionsResource resource = new UserCollectionPermissionsResource();
		resource.setDeleteAccess(true);
		resource.setReadAccess(true);
		resource.setWriteAccess(true);
		resource.setUsername(bob.getUsername());
		resource.setRoles(Arrays.asList("zeWriteRole"));
		securityManagementDriver.setUserCollectionPermissions(resource);

		UserCollectionPermissionsResource aliceCollectionPermissionsResource = securityManagementDriver
				.getUserCollectionPermissions(
						alice
								.getUsername());
		UserCollectionPermissionsResource bobCollectionPermissionsResource = securityManagementDriver
				.getUserCollectionPermissions(
						bob
								.getUsername());

		assertThat(aliceCollectionPermissionsResource.isReadAccess()).isFalse();
		assertThat(aliceCollectionPermissionsResource.isWriteAccess()).isFalse();
		assertThat(aliceCollectionPermissionsResource.isDeleteAccess()).isFalse();
		assertThat(aliceCollectionPermissionsResource.getRoles()).isEmpty();

		assertThat(bobCollectionPermissionsResource.isReadAccess()).isTrue();
		assertThat(bobCollectionPermissionsResource.isWriteAccess()).isTrue();
		assertThat(bobCollectionPermissionsResource.isDeleteAccess()).isTrue();
		assertThat(bobCollectionPermissionsResource.getRoles()).containsOnly("zeWriteRole");
	}

	private void whenCanReadCanWriteCanDeleteThenOk() {
		assertThat(securityManagementDriver.canRead(alice.getUsername(), records.folder1().getId())).isFalse();
		assertThat(securityManagementDriver.canWrite(alice.getUsername(), records.folder1().getId())).isFalse();
		assertThat(securityManagementDriver.canDelete(alice.getUsername(), records.folder1().getId())).isFalse();

		assertThat(securityManagementDriver.canRead(alice.getUsername(), records.folder2().getId())).isTrue();
		assertThat(securityManagementDriver.canWrite(alice.getUsername(), records.folder2().getId())).isTrue();
		assertThat(securityManagementDriver.canDelete(alice.getUsername(), records.folder2().getId())).isTrue();
	}

	private void whenChangeOldPasswordThenOk() {
		securityManagementDriver.changePassword(userCredentialAlice.getUsername(), alicePassword, newAlicePassword);

		assertThat(authService.authenticate(userCredentialAlice.getUsername(), alicePassword)).isFalse();
		assertThat(authService.authenticate(userCredentialAlice.getUsername(), newAlicePassword)).isTrue();
	}

	private void whenChangePasswordThenOk() {

		assertThat(authService.authenticate(userCredentialAlice.getUsername(), alicePassword)).isFalse();

		securityManagementDriver.changePassword(userCredentialAlice.getUsername(), alicePassword);

		assertThat(authService.authenticate(userCredentialAlice.getUsername(), alicePassword)).isTrue();
	}
	//
	//	private void whenAddRoleThenItIsAdded() {
	//		securityManagementDriver.addRole("zeWriteRole", "zeWriteRoleTitle", Arrays.asList("write"));
	//
	//		assertThat(securityManagementDriver.getRoles()).contains("zeWriteRole");
	//		RoleResource roleResource = securityManagementDriver.getRole("zeWriteRole");
	//		assertThat(roleResource.getId()).isEqualTo("zeWriteRole");
	//		assertThat(roleResource.getName()).isEqualTo("zeWriteRoleTitle");
	//		assertThat(roleResource.getPermissions()).containsOnly("write");
	//	}

	private void whenResetThenOk()
			throws InterruptedException {
		assertThat(securityManagementDriver.getRecordAuthorizationCodes(records.taxo1_fond1().getId())).isNotEmpty();

		securityManagementDriver.reset(records.taxo1_fond1().getId());
		waitForBatchProcess();
		recordServices.refresh(usersRecords.aliceIn(zeCollection).getWrappedRecord(), records.taxo1_fond1());

		assertThat(securityManagementDriver.getRecordAuthorizationCodes(records.taxo1_fond1().getId())).isEmpty();
	}

	private void givenDeleteRecordWhenHasRestaurationPermissionOnHierarchyThenOk()
			throws InterruptedException {

		recordServices.logicallyDelete(records.folder2(), alice);
		waitForBatchProcess();
		recordServices.refresh(usersRecords.aliceIn(zeCollection).getWrappedRecord(), records.taxo1_fond1());

		assertThat(securityManagementDriver.hasRestaurationPermissionOnHierarchy(alice.getUsername(), records.folder1().getId()))
				.isFalse();
		assertThat(securityManagementDriver.hasRestaurationPermissionOnHierarchy(alice.getUsername(), records.folder2().getId()))
				.isTrue();
	}

	private void whenHasDeletePermissionOnHierarchyThenOk() {
		assertThat(securityManagementDriver.hasDeletePermissionOnHierarchy(alice.getUsername(), records.folder1().getId()))
				.isFalse();
		assertThat(securityManagementDriver.hasDeletePermissionOnHierarchy(alice.getUsername(), records.folder2().getId()))
				.isTrue();
	}

	private void givenAuthorizationToAliceInFond1WhenHasDeletePermissionOnPrincipalConceptHierarchyThenOk()
			throws InterruptedException {
		AuthorizationResource authorizationResource = new AuthorizationResource();
		authorizationResource.setPrincipalIds(Arrays.asList(alice.getId()));
		authorizationResource.setRecordIds(Arrays.asList(records.taxo1_fond1().getId()));
		authorizationResource.setRoleIds(Arrays.asList(Role.WRITE, Role.DELETE));
		authorizationResource.setStartDate(calendarStartDate.getTime());
		authorizationResource.setEndDate(calendarEndDate.getTime());
		securityManagementDriver.addAuthorization(authorizationResource, true);
		waitForBatchProcess();
		recordServices.refresh(usersRecords.aliceIn(zeCollection).getWrappedRecord(), records.taxo1_fond1());

		assertThat(
				securityManagementDriver.hasDeletePermissionOnPrincipalConceptHierarchyAndIncludedRecords(bob.getUsername(),
						records.taxo1_fond1().getId())).isFalse();
		assertThat(
				securityManagementDriver.hasDeletePermissionOnPrincipalConceptHierarchyAndIncludedRecords(alice.getUsername(),
						records.taxo1_fond1().getId())).isTrue();
	}

	private void givenAuthorizationToAliceInFolder2WhenHasPermissionThenOk() {
		// Bob folder1
		assertThat(
				securityManagementDriver
						.hasPermission(bob.getUsername(), records.folder1().getId(), ContentPermissions.WRITE.getCode()))
				.isFalse();
		assertThat(
				securityManagementDriver
						.hasPermission(bob.getUsername(), records.folder1().getId(), ContentPermissions.DELETE.getCode()))
				.isFalse();
		assertThat(securityManagementDriver
				.hasPermission(bob.getUsername(), records.folder1().getId(), ContentPermissions.READ.getCode()))
				.isFalse();
		// Bob folder2
		assertThat(
				securityManagementDriver
						.hasPermission(bob.getUsername(), records.folder2().getId(), ContentPermissions.WRITE.getCode()))
				.isFalse();
		assertThat(
				securityManagementDriver
						.hasPermission(bob.getUsername(), records.folder2().getId(), ContentPermissions.DELETE.getCode()))
				.isFalse();
		assertThat(securityManagementDriver
				.hasPermission(bob.getUsername(), records.folder2().getId(), ContentPermissions.READ.getCode()))
				.isFalse();
		// Alice folder1
		assertThat(
				securityManagementDriver
						.hasPermission(alice.getUsername(), records.folder1().getId(), ContentPermissions.WRITE.getCode()))
				.isFalse();
		assertThat(
				securityManagementDriver
						.hasPermission(alice.getUsername(), records.folder1().getId(), ContentPermissions.DELETE.getCode()))
				.isFalse();
		assertThat(
				securityManagementDriver
						.hasPermission(alice.getUsername(), records.folder1().getId(), ContentPermissions.READ.getCode()))
				.isFalse();
		// Alice folder2
		assertThat(
				securityManagementDriver
						.hasPermission(alice.getUsername(), records.folder2().getId(), ContentPermissions.WRITE.getCode()))
				.isTrue();
		assertThat(
				securityManagementDriver
						.hasPermission(alice.getUsername(), records.folder2().getId(), ContentPermissions.DELETE.getCode()))
				.isTrue();
		assertThat(
				securityManagementDriver
						.hasPermission(alice.getUsername(), records.folder2().getId(), ContentPermissions.READ.getCode()))
				.isTrue();
	}

	private void whenModifyAuthorizationThenItIsModified() {
		AuthorizationResource authorizationResource = new AuthorizationResource();
		authorizationResource.setPrincipalIds(Arrays.asList(alice.getId()));
		authorizationResource.setRecordIds(Arrays.asList(records.folder2().getId()));
		authorizationResource.setRoleIds(Arrays.asList(Role.WRITE, Role.DELETE));
		securityManagementDriver.modify(authId, authorizationResource, true);

		AuthorizationResource retrievedAuthorizationResource = securityManagementDriver.getAuthorization(authId);
		assertThat(retrievedAuthorizationResource.getPrincipalIds().get(0)).isEqualTo(alice.getId());
		assertThat(retrievedAuthorizationResource.getRecordIds().get(0)).isEqualTo(records.folder2().getId());
		try {
			waitForBatchProcess();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void whenRemoveAuthorizationOnRecordThenItIsRemoved()
			throws InterruptedException, RolesManagerRuntimeException {

		securityManagementDriver.removeAuthorizationOnRecord(authId, records.folder1().getId(), true);
		Thread.sleep(1000);
		waitForBatchProcess();
		recordServices.refresh(usersRecords.bobIn(zeCollection).getWrappedRecord(), records.folder1());

		AuthorizationResource retrievedAuthorizationResource = securityManagementDriver.getAuthorization(authId);
		assertThat(retrievedAuthorizationResource.getPrincipalIds().get(0)).isEqualTo(bob.getId());
		assertThat(retrievedAuthorizationResource.getRecordIds()).isEmpty();
		assertThat(retrievedAuthorizationResource.getStartDate().toString()).isEqualTo(calendarStartDate.getTime().toString());
		assertThat(retrievedAuthorizationResource.getEndDate().toString()).isEqualTo(calendarEndDate.getTime().toString());
		assertThat(retrievedAuthorizationResource.getRoleIds()).containsOnly("WRITE", "DELETE");
	}

	private void whenAddAuthorizationThenCanGetIt()
			throws InterruptedException, RolesManagerRuntimeException {

		assertThat(bob.hasWriteAccess().on(records.folder1())).isFalse();
		assertThat(bob.hasReadAccess().on(records.folder1())).isFalse();
		assertThat(bob.hasDeleteAccess().on(records.folder1())).isFalse();

		assertThat(authorizationsServices.canRead(bob, records.folder1())).isFalse();
		assertThat(authorizationsServices.canWrite(bob, records.folder1())).isFalse();
		assertThat(authorizationsServices.canDelete(bob, records.folder1())).isFalse();

		AuthorizationResource authorizationResource = new AuthorizationResource();
		authorizationResource.setPrincipalIds(Arrays.asList(bob.getId()));
		authorizationResource.setRecordIds(Arrays.asList(records.folder1().getId()));
		authorizationResource.setRoleIds(Arrays.asList(Role.WRITE, Role.DELETE));
		authorizationResource.setStartDate(calendarStartDate.getTime());
		authorizationResource.setEndDate(calendarEndDate.getTime());
		authId = securityManagementDriver.addAuthorization(authorizationResource, true);
		Thread.sleep(1000);
		waitForBatchProcess();
		recordServices.refresh(usersRecords.bobIn(zeCollection).getWrappedRecord(), records.folder1());

		AuthorizationResource retrievedAuthorizationResource = securityManagementDriver.getAuthorization(authId);
		assertThat(retrievedAuthorizationResource.getPrincipalIds().get(0)).isEqualTo(bob.getId());
		assertThat(retrievedAuthorizationResource.getRecordIds().get(0)).isEqualTo(records.folder1().getId());
		assertThat(retrievedAuthorizationResource.getStartDate().toString()).isEqualTo(calendarStartDate.getTime().toString());
		assertThat(retrievedAuthorizationResource.getEndDate().toString()).isEqualTo(calendarEndDate.getTime().toString());
		assertThat(retrievedAuthorizationResource.getRoleIds()).containsOnly("WRITE", "DELETE");

	}

	// ---------

}
