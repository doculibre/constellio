package com.constellio.model.services.security.roles;

import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.extensions.plugins.ConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.migrations.CoreRoles;
import com.constellio.data.events.SDKEventBusSendingService;
import com.constellio.data.io.services.facades.OpenedResourcesWatcher;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException.RolesManagerRuntimeException_Validation;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.setups.Users;
import org.assertj.core.api.ListAssert;
import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;

public class RolesManagerAcceptanceTest extends ConstellioTest {

	CollectionsManager collectionsManager;
	CollectionsManager collectionsManagerOfOtherInstance;
	private Role validRole;
	private Role validRole2;
	private Role validRole3;
	private Role invalidRoleWithoutCode;
	private Role invalidRoleWithCode;
	private RolesManager manager;
	private RolesManager managerOfOtherInstance;
	private ConstellioPluginManager pluginManager;
	private SchemasRecordsServices schemas;
	private UserServices userServices;
	private String anotherCollection = "anotherCollection";
	private SDKEventBusSendingService zeInstanceEventBus;

	Users users = new Users();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTest(users),
				withCollection(anotherCollection).withAllTestUsers()
		);
		pluginManager = getAppLayerFactory().getPluginManager();
		collectionsManager = getAppLayerFactory().getCollectionsManager();

		AppLayerFactory otherInstanceAppLayerFactory = getAppLayerFactory("otherInstance");
		collectionsManagerOfOtherInstance = otherInstanceAppLayerFactory.getCollectionsManager();
		zeInstanceEventBus = linkEventBus(getDataLayerFactory(),
				otherInstanceAppLayerFactory.getModelLayerFactory().getDataLayerFactory());

		validRole = new Role(zeCollection, "uniqueCode", "zeValidRole", asList("operation1", "operation2"));
		validRole2 = new Role(zeCollection, "uniqueCode2", "zeValidRole2", asList("operation3", "operation4"));
		validRole3 = new Role(zeCollection, "uniqueCode3", "zeValidRole3", asList("operation5", "operation6"));

		invalidRoleWithoutCode = new Role(zeCollection, "", "", asList("operation"));
		invalidRoleWithCode = new Role(zeCollection, "zeInvalidRole", "", asList("operation"));

		userServices = getModelLayerFactory().newUserServices();
		users = new Users().setUp(userServices);
		manager = getModelLayerFactory().getRolesManager();
		managerOfOtherInstance = otherInstanceAppLayerFactory.getModelLayerFactory().getRolesManager();
		schemas = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
	}

	@Test
	public void givenRolesInMultipleCollectionsThenAllIndependent()
			throws Exception {

		givenCollection("collection1");
		givenCollection("collection2");

		assertThatEventsReceivedOnZeInstance().containsOnly(
				tuple("configUpdated", "/legacyConstellioIdsMapping.properties")
		);
		assertThatEventsSentFromZeInstance().isNotEmpty();

		assertThat(manager.getAllRoles("collection1")).hasSize(1);
		assertThat(manager.getAllRoles("collection2")).hasSize(1);
		assertThat(managerOfOtherInstance.getAllRoles("collection1")).hasSize(1);
		assertThat(managerOfOtherInstance.getAllRoles("collection2")).hasSize(1);

		Role role1 = new Role("collection1", "uniqueCode", "zeValidRole", asList("operation"));
		Role role2 = new Role("collection2", "uniqueCode", "zeValidRole", asList("operation"));
		Role role3 = new Role("collection2", "otherRole", "zeValidRole", asList("operation"));

		manager.addRole(role1);
		managerOfOtherInstance.addRole(role2);
		manager.addRole(role3);

		assertThatEventsReceivedOnZeInstance().containsExactly(
				tuple("configUpdated", "/collection2/roles.xml")
		);
		assertThatEventsSentFromZeInstance().containsExactly(
				tuple("configUpdated", "/collection1/roles.xml"),
				tuple("configUpdated", "/collection2/roles.xml")
		);

		assertThat(manager.getAllRoles("collection1")).hasSize(2);
		assertThat(manager.getAllRoles("collection2")).hasSize(3);
		assertThat(managerOfOtherInstance.getAllRoles("collection1")).hasSize(2);
		assertThat(managerOfOtherInstance.getAllRoles("collection2")).hasSize(3);
		assertThat(manager.getRole("collection1", role1.getCode()).getCollection()).isEqualTo("collection1");
		assertThat(manager.getRole("collection2", role2.getCode()).getCollection()).isEqualTo("collection2");
		assertThat(manager.getRole("collection2", role3.getCode()).getCollection()).isEqualTo("collection2");

		assertThat(manager.getAllRoles("collection1")).hasSize(2);
		assertThat(manager.getAllRoles("collection2")).hasSize(3);
	}

	@Test
	public void givenUserHasMultipleRolesThenHasValidPermissions()
			throws Exception {

		manager.addRole(validRole);
		manager.addRole(validRole2);
		manager.addRole(validRole3);

		getModelLayerFactory().newRecordServices().update(
				users.sasquatchIn(zeCollection).setUserRoles(asList("uniqueCode", "uniqueCode2", "uniqueCode3")));

		User sasquatchInZeCollection = users.sasquatchIn(zeCollection);
		User sasquatchInAnotherCollection = users.sasquatchIn(anotherCollection);

		assertThat(sasquatchInZeCollection.has("otherPermission").globally()).isFalse();
		assertThat(sasquatchInZeCollection.has("operation1").globally()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation2").globally()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation3").globally()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation4").globally()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation5").globally()).isTrue();
		assertThat(sasquatchInZeCollection.has("operation6").globally()).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation2").globally()).isTrue();
		assertThat(sasquatchInZeCollection.hasAll("operation1", "operation2", "otherPermission").globally()).isFalse();
		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation2").globally()).isTrue();
		assertThat(sasquatchInZeCollection.hasAny("operation1", "operation2", "otherPermission").globally()).isTrue();

		assertThat(sasquatchInAnotherCollection.hasAny("operation1", "operation2", "operation3", "operation4",
				"operation5", "operation6", "otherPermission").globally()).isFalse();

		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("operation1")).isTrue();
		assertThat(userServices.has(sasquatch).globalPermissionInAnyCollection("otherPermission")).isFalse();
		assertThat(userServices.has(sasquatch).anyGlobalPermissionInAnyCollection("operation1", "operation2")).isTrue();
		assertThat(userServices.has(sasquatch).anyGlobalPermissionInAnyCollection("operation1", "otherPermission")).isTrue();
		assertThat(userServices.has(sasquatch).anyGlobalPermissionInAnyCollection("otherPermission")).isFalse();
		assertThat(userServices.has(sasquatch).allGlobalPermissionsInAnyCollection("operation1", "operation2")).isTrue();
		assertThat(userServices.has(sasquatch).allGlobalPermissionsInAnyCollection("operation1", "otherPermission")).isFalse();
		assertThat(userServices.has(sasquatch).allGlobalPermissionsInAnyCollection("otherPermission")).isFalse();

	}

	@Test
	public void givenUserHasMultipleRolesAndGroupWithRolesThenHasValidPermissions()
			throws Exception {

		manager.addRole(validRole);
		manager.addRole(validRole2);
		manager.addRole(validRole3);

		Group group1 = schemas.newGroup().setRoles(asList("uniqueCode"));
		getModelLayerFactory().newRecordServices().add(group1);

		Group group2 = schemas.newGroup().setRoles(asList("uniqueCode2"));
		getModelLayerFactory().newRecordServices().add(group2);

		User user = schemas.newUser().setUserGroups(asList(group1.getId(), group2.getId())).setUserRoles(
				asList("uniqueCode3"));
		getModelLayerFactory().newRecordServices().add(user);

		assertThat(user.has("otherPermission").globally()).isFalse();
		assertThat(user.has("operation1").globally()).isTrue();
		assertThat(user.has("operation2").globally()).isTrue();
		assertThat(user.has("operation3").globally()).isTrue();
		assertThat(user.has("operation4").globally()).isTrue();
		assertThat(user.has("operation5").globally()).isTrue();
		assertThat(user.has("operation6").globally()).isTrue();
		assertThat(user.hasAll("operation1", "operation2").globally()).isTrue();
		assertThat(user.hasAll("operation1", "operation2", "otherPermission").globally()).isFalse();
		assertThat(user.hasAny("operation1", "operation2").globally()).isTrue();
		assertThat(user.hasAny("operation1", "operation2", "otherPermission").globally()).isTrue();
	}

	@Test
	public void givenXMLAlreadyExistingAndARoleThenManagerLoadRoles()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);

		RolesManager newManager = new RolesManager(getModelLayerFactory());
		newManager.initialize();
		assertThat(newManager.getAllRoles(zeCollection)).hasSize(2);
	}

	@Test
	public void givenMultipleCorrectRoleThenManagerLoadAll()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.addRole(validRole2);
		manager.addRole(validRole3);

		List<Role> loaded = manager.getAllRoles(zeCollection);

		assertThat(loaded).extracting("code").containsOnly(
				CoreRoles.ADMINISTRATOR, validRole.getCode(), validRole2.getCode(), validRole3.getCode());
	}

	@Test
	public void givenCorrectRoleThenManagerSaveIt()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);

		Role loaded = manager.getRole(zeCollection, validRole.getCode());

		assertThat(loaded.getCode()).isEqualTo(validRole.getCode());
		assertThat(loaded.getTitle()).isEqualTo(validRole.getTitle());
		assertThat(loaded.getCollection()).isEqualTo(validRole.getCollection());
	}

	@Test
	public void givenCorrectCodeAndUpdatedTitleThenTitleIsUpdated()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.updateRole(validRole.withTitle("newTitle").withPermissions(asList("operation42", "operation666")));

		Role loaded = manager.getRole(zeCollection, validRole.getCode());

		assertThat(loaded.getTitle()).isEqualTo("newTitle");
		assertThat(loaded.getOperationPermissions()).containsOnlyOnce("operation42", "operation666");
	}

	@Test
	public void givenMultipleCorrectCodeAndUpdatedTitleThenOnlyRightTitleIsUpdated()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.addRole(validRole2);
		manager.addRole(validRole3);

		manager.updateRole(validRole.withTitle("newTitle").withPermissions(asList("operation42", "operation666")));

		Role loaded = manager.getRole(zeCollection, validRole.getCode());

		assertThat(loaded.getTitle()).isEqualTo("newTitle");
		assertThat(loaded.getOperationPermissions()).containsOnlyOnce("operation42", "operation666");
	}

	@Test
	public void givenCorrectCodeAndDeleteThenRoleDeleted()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.deleteRole(validRole);

		assertThat(manager.getAllRoles(zeCollection)).hasSize(1);
	}

	@Test
	public void givenValidCollectionRolePermissionAndRoleHasPermissionThenReturnTrue()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		assertThat(manager.hasPermission(zeCollection, validRole.getCode(), "operation1")).isTrue();
	}

	@Test(expected = Exception.class)
	public void givenInvalidAndValidCollectionRolePermissionAndRoleHasPermissionThenException()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.hasPermission("zeInvalidCollection", validRole.getCode(), "operation1");
		fail();
	}

	@Test
	public void givenValidCollectionPermissionAndInvalidRoleAndRoleHasPermissionThenFalse()
			throws RolesManagerRuntimeException {
		assertThat(manager.hasPermission(zeCollection, invalidRoleWithCode.getCode(), "operation1")).isFalse();
	}

	@Test
	public void givenValidCollectionRolePermissionAndRoleDontHavePermissionThenReturnFalse() {
		assertThat(manager.hasPermission(zeCollection, validRole2.getCode(), "operation1")).isFalse();
	}

	@Test
	public void givenMultipleCorrectCodeAndDeleteThenOnlyRightOneDeleted()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.addRole(validRole2);
		manager.addRole(validRole3);

		manager.deleteRole(validRole);

		assertThat(manager.getAllRoles(zeCollection)).hasSize(3).extracting("code").doesNotContain(validRole.getCode());
	}

	//@Test
	public void givenMultipleInstancesWhenAddingRoleThenAvailableInAllInstances()
			throws RolesManagerRuntimeException {
		OpenedResourcesWatcher.enabled = false;
		RolesManager managerCreatedBeforeNewRole = getModelLayerFactory("createdBeforeNewRole").getRolesManager();
		manager.addRole(validRole);
		RolesManager managerCreatedAfterNewRole = getModelLayerFactory("createdAfterNewRole").getRolesManager();

		assertThat(manager.getAllRoles(zeCollection)).hasSize(2);
		assertThat(managerCreatedAfterNewRole.getAllRoles(zeCollection)).hasSize(2);

		//TODO Maxime
		assertThat(managerCreatedBeforeNewRole.getAllRoles(zeCollection)).hasSize(1);
	}

	@Test(expected = RolesManagerRuntimeException_Validation.class)
	public void givenMultipleRoleWithSameCodeAndAddThenExceptionThrown()
			throws RolesManagerRuntimeException {
		manager.addRole(validRole);
		manager.addRole(validRole);
	}

	@Test(expected = RolesManagerRuntimeException_Validation.class)
	public void givenRoleWithEmptyCodeThenExceptionThrown()
			throws RolesManagerRuntimeException {
		manager.addRole(invalidRoleWithoutCode);
	}

	@Test(expected = RolesManagerRuntimeException_Validation.class)
	public void givenInvalidCodeAndGetThenExceptionThrown()
			throws RolesManagerRuntimeException {
		manager.getRole(zeCollection, invalidRoleWithCode.getCode());
	}

	@Test(expected = RolesManagerRuntimeException_Validation.class)
	public void givenInvalidCodeAndDeleteThenExceptionThrown()
			throws RolesManagerRuntimeException {
		manager.deleteRole(invalidRoleWithCode);
	}

	@Test(expected = RolesManagerRuntimeException_Validation.class)
	public void givenEmptyCodeAndDeleteThenExceptionThrown()
			throws RolesManagerRuntimeException {
		manager.deleteRole(invalidRoleWithoutCode);
	}

	private ListAssert<Tuple> assertThatEventsReceivedOnZeInstance() {
		return assertThat(
				zeInstanceEventBus.newReceivedEventsOnBus("configManager"))
				.extracting("type", "data");
	}

	private ListAssert<Tuple> assertThatEventsSentFromZeInstance() {
		return assertThat(
				zeInstanceEventBus.newSentEventsOnBus("configManager"))
				.extracting("type", "data");
	}

}
