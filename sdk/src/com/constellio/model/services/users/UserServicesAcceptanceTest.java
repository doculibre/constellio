package com.constellio.model.services.users;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.usernamesOf;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.constants.RMPermissionsTo;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.model.services.encrypt.EncryptionKeyFactory;
import com.constellio.model.services.encrypt.EncryptionServices;
import com.constellio.model.services.factories.ModelLayerFactoryUtils;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotExcuteTransaction;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUserNameOrPassword;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import com.constellio.sdk.tests.annotations.SlowTest;

public class UserServicesAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = LocalDateTime.now();

	CollectionsManager collectionsManager;
	UserServices userServices;
	RecordServices recordServices;
	SearchServices searchServices;

	List<String> noCollections = Collections.emptyList();
	List<String> andNoCollections = Collections.emptyList();
	List<String> noGroups = Collections.emptyList();

	String legends, heroes;

	List<String> allCollections;
	String collection1, collection2, collection3;
	UserCredential user, anotherUser, thirdUser;
	UserCredentialsManager userCredentialsManager;
	GlobalGroupsManager globalGroupsManager;
	@Mock UserCredential userWithNoAccessToDeleteCollection;
	@Mock Factory<EncryptionServices> encryptionServicesFactory;
	AuthenticationService authenticationService;
	List<String> msExchDelegateListBL = new ArrayList<>();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection().withConstellioRMModule().withConstellioESModule()
				.withAllTestUsers());
		givenBackgroundThreadsEnabled();
		withSpiedServices(ModelLayerConfiguration.class);
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				org.joda.time.Duration fourSeconds = org.joda.time.Duration.standardSeconds(4);
				org.joda.time.Duration oneSecond = org.joda.time.Duration.standardSeconds(1);

				configuration.setTokenDuration(fourSeconds);
				configuration.setTokenRemovalThreadDelayBetweenChecks(oneSecond);
			}
		});
	}

	@Test
	public void givenGlobalGroupInACollectionWhenRemovingFromCollectionAndAddingUsersThenOK()
			throws Exception {

		givenCollection1And2();
		givenHeroesGroup();
		givenLegendsGroup();
		//userServices.removeGroupFromCollections();
	}

	@Test
	@SlowTest
	public void whenEveryoneGetsInHereThenStillNotLetal()
			throws Exception {
		onlyWhen(Toggle.NEW_USERCREDENTIAL_SERVICES).isEnabled();
		givenCollection1And2();

		for (int i = 0; i < 10000; i++) {
			user = userServices
					.createUserCredential("grimPatron" + i, "Grim", "Patron", "grim.patron." + i + "@doculibre.com", noGroups,
							noCollections, UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null)
					.withSystemAdminPermission();
			userServices.addUpdateUserCredential(user);
		}

		for (int i = 0; i < 10000; i++) {
			assertThat(userServices.getUser("grimPatron" + i)).isNotNull();
			//assertThat(userServices.getUserInCollection("grimPatron" + i, zeCollection)).isNotNull();
		}
	}

	@Test
	public void given2GlobalGroupsAnd2CustomGroupsWhenGetAllGroupsThenCanObtainCustomGlobalAndAllGroups()
			throws Exception {
		givenCollection1And2();
		givenHeroesGroup();
		givenLegendsGroup();
		userServices.createCustomGroupInCollectionWithCodeAndName("collection1", "A", "Group A");
		userServices.createCustomGroupInCollectionWithCodeAndName("collection1", "B", "Group B");
		userServices.createCustomGroupInCollectionWithCodeAndName("collection2", "C", "Group C");

		Group collection1Legends = userServices.getGroupInCollection("legends", "collection1");
		Group collection1Heroes = userServices.getGroupInCollection("heroes", "collection1");
		Group collection1GroupA = userServices.getGroupInCollection("A", "collection1");
		Group collection1GroupB = userServices.getGroupInCollection("B", "collection1");
		Group collection2GroupC = userServices.getGroupInCollection("C", "collection2");

		assertThat(collection1Legends).isNotNull();
		assertThat(collection1Heroes).isNotNull();
		assertThat(collection1GroupA).isNotNull();
		assertThat(collection1GroupB).isNotNull();
		assertThat(collection2GroupC).isNotNull();

		assertThat(userServices.getAllGroupsInCollections("collection1"))
				.containsOnly(collection1Heroes, collection1Legends, collection1GroupA, collection1GroupB);
		assertThat(userServices.getCollectionGroups("collection1")).containsOnly(collection1GroupA, collection1GroupB);
		assertThat(userServices.getGlobalGroupsInCollections("collection1")).containsOnly(collection1Heroes, collection1Legends);
	}

	@Test
	public void whenCreatingUserThenCredentialObtainable()
			throws Exception {

		setupAfterCollectionCreation();

		givenUserWith(noGroups, noCollections);

		assertThat(userServices.getUser(user.getUsername()).getUsername()).isEqualTo(chuckNorris);

	}

	@Test
	public void whenCreatingUserInCollectionThenIsInCollection()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, and(collection1));

		assertThatUserIsOnlyInCollections(user, collection1);

	}

	@Test
	public void whenCreatingUserInInvalidCollectionThenNotSetted()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, and(collection1, "invalidCollection"));

		assertThatUserIsOnlyInCollections(user, collection1);

	}

	@Test
	public void givenExistingUserWhenUpdatingWithCollectionsThenAdded()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, andNoCollections);
		assertThatUserIsOnlyInCollections(user);

		givenUserWith(noGroups, and(collection1));
		assertThatUserIsOnlyInCollections(user, collection1);

	}

	@Test
	public void givenExistingUserWhenAddingToCollectionThenAdded()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, andNoCollections);
		assertThatUserIsOnlyInCollections(user);

		userServices.addUserToCollection(user, collection1);
		assertThatUserIsOnlyInCollections(user, collection1);

	}

	@Test
	public void givenUserInCollectionWhenRemovingFromCollectionThenHasDisabledStatus()
			throws Exception {

		givenCollection1And2();
		givenUserWith(noGroups, and(collection1, collection2));

		userServices.removeUserFromCollection(user, collection1);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection1)
				.where(userServices.usernameMetadata(collection1)).is(user.getUsername());
		Record userCredentialRecord = searchServices.searchSingleResult(condition);
		assertThat(userCredentialRecord.isActive()).isFalse();
		assertThatUserIsOnlyInCollections(user, collection2);
	}

	@Test
	public void givenUserInCollectionsWhenRemovingHimThenRemoveFromAllCollectionsAndChangeStatus()
			throws Exception {

		givenCollection1And2();
		givenUserWith(noGroups, and(collection1, collection2));

		userServices.removeUserCredentialAndUser(user);

		Map<String, User> userInCollection = new HashMap<>();
		for (String collection : Arrays.asList(collection1, collection2)) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection)
					.where(userServices.usernameMetadata(collection)).is(user.getUsername());
			Record userCredentialRecord = searchServices.searchSingleResult(condition);
			User userRecord = new User(userCredentialRecord,
					getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection),
					getModelLayerFactory().getRolesManager().getCollectionRoles(collection));
			userInCollection.put(collection, userRecord);
		}
		assertThat(userInCollection.get(collection1).getWrappedRecord().isActive()).isFalse();
		assertThat(userInCollection.get(collection2).getWrappedRecord().isActive()).isFalse();
		assertThat(userInCollection.get(collection1).getStatus()).isEqualTo(UserCredentialStatus.DELETED);
		assertThat(userInCollection.get(collection2).getStatus()).isEqualTo(UserCredentialStatus.DELETED);
		assertThat(userServices.getUser(user.getUsername()).getStatus()).isEqualTo(UserCredentialStatus.DELETED);
		assertThat(userServices.getAllUserCredentials()).hasSize(2);
		assertThat(userServices.getActiveUserCredentials()).hasSize(1);
		assertThat(userServices.getActiveUserCredentials().get(0).getUsername()).isEqualTo("admin");
	}

	@Test
	public void givenUserInCollectionsWhenSetStatusPedingApprovalThenRemoveFromAllCollectionsAndChangeStatus()
			throws Exception {

		givenCollection1And2();
		givenUserWith(noGroups, and(collection1, collection2));

		userServices.setUserCredentialAndUserStatusPendingApproval(user);

		Map<String, User> userInCollection = new HashMap<>();
		for (String collection : Arrays.asList(collection1, collection2)) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection)
					.where(userServices.usernameMetadata(collection1)).is(user.getUsername());
			Record userCredentialRecord = searchServices.searchSingleResult(condition);
			User userRecord = new User(userCredentialRecord,
					getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection),
					getModelLayerFactory().getRolesManager().getCollectionRoles(collection));
			userInCollection.put(collection, userRecord);
		}
		assertThat(userInCollection.get(collection1).getWrappedRecord().isActive()).isFalse();
		assertThat(userInCollection.get(collection2).getWrappedRecord().isActive()).isFalse();
		assertThat(userInCollection.get(collection1).getStatus()).isEqualTo(UserCredentialStatus.PENDING);
		assertThat(userInCollection.get(collection2).getStatus()).isEqualTo(UserCredentialStatus.PENDING);
		assertThat(userServices.getUser(user.getUsername()).getStatus()).isEqualTo(UserCredentialStatus.PENDING);
	}

	@Test
	public void givenUserInCollectionsWhenSuspendHimThenRemoveFromAllCollectionsAndChangeStatus()
			throws Exception {

		givenCollection1And2();
		givenUserWith(noGroups, and(collection1, collection2));

		userServices.suspendUserCredentialAndUser(user);

		Map<String, User> userInCollection = new HashMap<>();
		for (String collection : Arrays.asList(collection1, collection2)) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection)
					.where(userServices.usernameMetadata(collection1)).is(user.getUsername());
			Record userCredentialRecord = searchServices.searchSingleResult(condition);
			User userRecord = new User(userCredentialRecord,
					getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection),
					getModelLayerFactory().getRolesManager().getCollectionRoles(collection));
			userInCollection.put(collection, userRecord);
		}
		assertThat(userInCollection.get(collection1).getWrappedRecord().isActive()).isFalse();
		assertThat(userInCollection.get(collection2).getWrappedRecord().isActive()).isFalse();
		assertThat(userInCollection.get(collection1).getStatus()).isEqualTo(UserCredentialStatus.SUSPENDED);
		assertThat(userInCollection.get(collection2).getStatus()).isEqualTo(UserCredentialStatus.SUSPENDED);
		assertThat(userServices.getUser(user.getUsername()).getStatus()).isEqualTo(UserCredentialStatus.SUSPENDED);
	}

	@Test
	public void givenInactiveUserWhenActiveHimThenActiveInAllCollectionsUserAndChangeStatus()
			throws Exception {

		givenCollection1And2();
		givenUserWith(noGroups, and(collection1, collection2));
		userServices.removeUserCredentialAndUser(user);

		userServices.activeUserCredentialAndUser(user);

		Map<String, User> userInCollection = new HashMap<>();
		for (String collection : Arrays.asList(collection1, collection2)) {
			LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection)
					.where(userServices.usernameMetadata(collection1)).is(user.getUsername());
			Record userCredentialRecord = searchServices.searchSingleResult(condition);
			User userRecord = new User(userCredentialRecord,
					getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection),
					getModelLayerFactory().getRolesManager().getCollectionRoles(collection));
			userInCollection.put(collection, userRecord);
		}
		assertThat(userInCollection.get(collection1).getWrappedRecord().isActive()).isTrue();
		assertThat(userInCollection.get(collection2).getWrappedRecord().isActive()).isTrue();
		assertThat(userInCollection.get(collection1).getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
		assertThat(userInCollection.get(collection2).getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
		assertThat(userServices.getUser(user.getUsername()).getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	@Test
	public void givenGroupInCollectionWhenRemovingFromCollectionThenRemoveUsersToo()
			throws Exception {

		givenCollection1And2();
		givenLegendsGroupWithAllUsersInCollections(collection1, collection2);
		givenHeroesGroupWithAllUsersInCollections(collection1, collection2);
		givenUserWith(asList(legends), and(collection1, collection2));
		givenAnotherUserWith(asList(legends), and(collection1, collection2));
		givenAThirdUserWith(asList(heroes), and(collection1, collection2));
		GlobalGroup legendsGroup = userServices.getGroup(legends);

		Record recordLegendsCollection1 = getRecordGroupInCollection(legendsGroup, collection1);
		Record recordLegendsCollection2 = getRecordGroupInCollection(legendsGroup, collection2);
		assertThat(userServices.getAllGroupsInCollections(collection1)).hasSize(2);
		assertThat(userServices.getAllGroupsInCollections(collection2)).hasSize(2);
		List<Record> recordsInLegendsCollection1 = getRecordsInGroupInCollection(recordLegendsCollection1, collection1);
		List<Record> recordsInLegendsCollection2 = getRecordsInGroupInCollection(recordLegendsCollection2, collection2);
		assertThat(recordsInLegendsCollection1).isNotEmpty();
		assertThat(recordsInLegendsCollection2).isNotEmpty();

		userServices.logicallyRemoveGroupHierarchy(user, legendsGroup);

		recordsInLegendsCollection1 = getRecordsInGroupInCollection(recordLegendsCollection1, collection1);
		recordsInLegendsCollection2 = getRecordsInGroupInCollection(recordLegendsCollection2, collection2);
		recordLegendsCollection1 = getRecordGroupInCollection(legendsGroup, collection1);
		recordLegendsCollection2 = getRecordGroupInCollection(legendsGroup, collection2);
		assertThat(userCredentialsManager.getUserCredentialsInGlobalGroup(legendsGroup.getCode())).isEmpty();
		assertThat(globalGroupsManager.getActiveGroups()).hasSize(1);
		assertThat(recordLegendsCollection1.isActive()).isFalse();
		assertThat(recordLegendsCollection2.isActive()).isFalse();
		assertThat(recordsInLegendsCollection1).isEmpty();
		assertThat(recordsInLegendsCollection2).isEmpty();
		assertThat(userServices.getAllGroupsInCollections(collection1)).hasSize(1);
		assertThat(userServices.getAllGroupsInCollections(collection2)).hasSize(1);
	}

	@Test
	public void whenCreatingGlobalGroupThenObtainable()
			throws Exception {

		setupAfterCollectionCreation();
		givenLegendsGroup();

		assertThat(userServices.getGroup(legends).getCode()).isEqualTo(legends);

	}

	@Test
	public void whenCreatingGlobalGroupThenObtainableInCollections()
			throws Exception {

		givenCollection1();
		givenLegendsGroup();

		assertThat(userServices.getGroupInCollection(legends, collection1).getCode()).isEqualTo(legends);
	}

	@Test
	public void whenCreatingCollectionThenAddGlobalGroups()
			throws Exception {

		setupAfterCollectionCreation();
		givenLegendsGroup();
		collection1 = "collection1";
		givenCollection(collection1);

		assertThat(userServices.getGroupInCollection(legends, collection1).getCode()).isEqualTo(legends);

	}

	@Test
	public void whenCreatingUserWithGroupWithAutomaticCollectionMembershipThenInCollection()
			throws Exception {

		givenCollection1And2And3();
		givenLegendsGroupWithAllUsersInCollections(collection1);
		givenHeroesGroup();
		givenUserWith(groups(legends, heroes), and(collection2));

		assertThatUserIsOnlyInCollections(user, collection1, collection2);

	}

	@Test
	public void whenAddingAutomaticCollectionMembershipToGroupThenAddUsersThenInCollection()
			throws Exception {

		givenCollection1And2And3();
		givenLegendsGroup();
		givenHeroesGroup();
		givenUserWith(groups(legends, heroes), and(collection2));
		givenAnotherUserWith(groups(legends, heroes), andNoCollections);
		assertThatUserIsOnlyInCollections(user, collection2);
		assertThatUserIsOnlyInCollections(anotherUser);

		givenLegendsGroupWithAllUsersInCollections(collection1);

		assertThatUserIsOnlyInCollections(user, collection1, collection2);
		assertThatUserIsOnlyInCollections(anotherUser, collection1);

	}

	@Test
	public void givenMultipleUsersThenAllObtainedCorrectly()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, and(collection1));
		givenAnotherUserWith(noGroups, and(collection1));

		assertThat(userServices.getUser(user.getUsername()).getUsername()).contains(user.getUsername());
		assertThat(userServices.getUser(anotherUser.getUsername()).getUsername()).contains(anotherUser.getUsername());
		assertThat(userServices.getUserInCollection(user.getUsername(), collection1).getUsername()).contains(user.getUsername());
		assertThat(userServices.getUserInCollection(anotherUser.getUsername(), collection1).getUsername())
				.contains(anotherUser.getUsername());
	}

	@Test
	public void givenUserInGroupThenIsInGroupUsers()
			throws Exception {

		givenCollection1();
		givenLegendsGroup();
		givenHeroesGroup();
		givenUserWith(groups(legends), andNoCollections);
		givenAnotherUserWith(groups(legends), andNoCollections);
		givenAThirdUserWith(noGroups, andNoCollections);

		assertThat(userServices.getGlobalGroupActifUsers(legends)).containsOnly(anotherUser, user);
		assertThat(userServices.getGlobalGroupActifUsers(heroes)).isEmpty();
	}

	@Test
	public void whenModifyingUsersAddingAndRemovingGroupThenAddedAndRemovedFromGroupList()
			throws Exception {

		givenCollection1();
		givenLegendsGroup();
		givenHeroesGroup();
		givenUserWith(noGroups, andNoCollections);
		givenAnotherUserWith(groups(legends), andNoCollections);
		givenAThirdUserWith(noGroups, andNoCollections);

		givenAnotherUserWith(groups(heroes), andNoCollections);
		givenAThirdUserWith(groups(heroes), andNoCollections);

		assertThat(userServices.getGlobalGroupActifUsers(legends)).isEmpty();
		assertThat(userServices.getGlobalGroupActifUsers(heroes)).containsOnly(thirdUser, anotherUser);
		assertThat(userServices.getUser(thirdUser.getUsername()).getGlobalGroups()).contains(heroes);
		assertThat(userServices.getUser(anotherUser.getUsername()).getGlobalGroups()).contains(heroes);
	}

	@Test
	public void whenSetGroupUserListThenListAndUserNewlyInGroupWithAutomaticMembershipAddedToCollections()
			throws Exception {

		givenCollection1And2();
		givenLegendsGroupWithAllUsersInCollections(collection1);
		givenHeroesGroup();
		givenUserWith(noGroups, andNoCollections);
		givenAnotherUserWith(noGroups, and(collection2));
		givenAThirdUserWith(noGroups, andNoCollections);

		userServices.setGlobalGroupUsers(legends, asList(user, anotherUser));
		userServices.setGlobalGroupUsers(heroes, asList(user, thirdUser));

		assertThat(usernamesOf(userServices.getGlobalGroupActifUsers(legends))).containsOnly(user.getUsername(),
				anotherUser.getUsername());
		assertThat(usernamesOf(userServices.getGlobalGroupActifUsers(heroes))).containsOnly(user.getUsername(),
				thirdUser.getUsername());
		assertThat(userServices.getUser(user.getUsername()).getGlobalGroups()).contains(heroes, legends);
		assertThat(userServices.getUser(anotherUser.getUsername()).getGlobalGroups()).contains(legends);
		assertThat(userServices.getUser(thirdUser.getUsername()).getGlobalGroups()).contains(heroes);
		assertThatUserIsOnlyInCollections(user, collection1);
		assertThatUserIsOnlyInCollections(anotherUser, collection1, collection2);
		assertThatUserIsOnlyInCollections(thirdUser);

	}

	@Test
	public void givenUserWhenAddOrUpdateThenTitleIsFirstPlusLastName()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, and(collection1));
		assertThat(user.getTitle()).isEqualTo(user.getFirstName() + " " + user.getLastName());

	}

	@Test
	public void givenUserCredentialIsModifiedThenChangesSynchedCorrectly()
			throws Exception {

		givenCollection1And2();

		SchemasRecordsServices collection1Schemas = new SchemasRecordsServices(collection1, getModelLayerFactory());

		Role role1 = new Role(collection1, "role1", "Ze role1",
				asList(RMPermissionsTo.CREATE_DOCUMENTS, RMPermissionsTo.CREATE_FOLDERS));

		Role role2 = new Role(collection1, "role2", "Ze role2",
				asList(RMPermissionsTo.CREATE_SUB_FOLDERS, RMPermissionsTo.CREATE_INACTIVE_DOCUMENT));

		Role role3 = new Role(collection1, "role3", "Ze role3",
				asList(RMPermissionsTo.MANAGE_DOCUMENT_AUTHORIZATIONS, RMPermissionsTo.MANAGE_FOLDER_AUTHORIZATIONS));

		RolesManager rolesManager = getModelLayerFactory().getRolesManager();
		rolesManager.addRole(role1);
		rolesManager.addRole(role2);
		rolesManager.addRole(role3);

		givenLegendsGroup();
		givenUserWith(asList(legends), and(collection1));

		Transaction transaction = new Transaction();
		transaction.add(userServices.getUserInCollection(chuckNorris, collection1).setUserRoles(asList("role1", "role2")));
		transaction.add(userServices.getGroupInCollection(legends, collection1).setRoles(asList("role3")));
		recordServices.execute(transaction);

		User user = userServices.getUserInCollection(chuckNorris, collection1);
		assertThat(searchServices.getResultsCount(from(collection1Schemas.userSchemaType()).returnAll())).isEqualTo(1);
		assertThat(user.getTitle()).isEqualTo("Chuck Norris");
		assertThat(user.getEmail()).isEqualTo("chuck.norris@doculibre.com");
		assertThat(userServices.getUserInCollection(chuckNorris, collection1).getAllRoles())
				.containsOnlyOnce("role1", "role2", "role3");

		userServices.addUpdateUserCredential(userServices.getUser(chuckNorris).withFirstName("CHUCK").withLastName("NORRIS")
				.withEmail("chuck@norris.com"));

		user = userServices.getUserInCollection(chuckNorris, collection1);
		assertThat(user.getTitle()).isEqualTo("CHUCK NORRIS");
		assertThat(user.getEmail()).isEqualTo("chuck@norris.com");
		assertThat(userServices.getUserInCollection(chuckNorris, collection1).getAllRoles())
				.containsOnlyOnce("role1", "role2", "role3");
		assertThat(searchServices.getResultsCount(from(collection1Schemas.userSchemaType()).returnAll())).isEqualTo(1);
	}

	@Test
	public void whenAddingUserToCollectionGivenTheUserRecordWriteFailsThenRollbackTheGlobalConfig()
			throws Exception {
		givenCollection1();
		givenUserAndPassword();

		userServices = spy(userServices);
		doThrow(new UserServicesRuntimeException_CannotExcuteTransaction(new RuntimeException()))
				.when(userServices).sync(any(XmlUserCredential.class));

		try {
			userServices.addUserToCollection(user, collection1);
		} catch (UserServicesRuntimeException_CannotExcuteTransaction e) {
			assertThat(userServices.getUser(user.getUsername()).getCollections()).isEmpty();
			return;
		}

		fail("Expected exception not thrown");
	}

	@Test
	public void givenTwoUsersWithSameEmailThenOk()
			throws Exception {
		givenCollection1();
		givenUserAndPassword();

		UserCredential user2 = userServices.createUserCredential(
				chuckNorris + "Other", "Chuck", "Norris", "chuck.norris@doculibre.com", new ArrayList<String>(),
				new ArrayList<String>(),
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);
		userServices.addUpdateUserCredential(user2);

		assertThat(userServices.getUser(chuckNorris + "Other").getEmail()).isEqualTo(user.getEmail());

	}
	// ---- Exception tests

	@Test(expected = UserServicesRuntimeException_NoSuchUser.class)
	public void whenGetInexistentUserThenException()
			throws Exception {

		givenCollection1();
		userServices.getUser("nobody");
	}

	@Test(expected = UserServicesRuntimeException_NoSuchUser.class)
	public void whenGetInexistentUserInExistentCollectionThenException()
			throws Exception {

		givenCollection1();
		userServices.getUserInCollection("nobody", collection1);
	}

	@Test(expected = UserServicesRuntimeException_UserIsNotInCollection.class)
	public void whenCreatingUserInNoCollectionsThenIsInNoCollections()
			throws Exception {

		givenCollection1();
		givenUserWith(noGroups, andNoCollections);

		userServices.getUserInCollection(user.getUsername(), collection1);

	}

	@Test
	public void givenUserInGroupWhenRemoveUserFromGroupThenItIsRemoved()
			throws Exception {

		givenCollection1();
		givenLegendsGroupWithAllUsersInCollections(collection1);
		givenUserWith(asList(legends), asList(collection1));
		assertThat(userServices.getUser(user.getUsername()).getGlobalGroups()).contains(legends);

		userServices.removeUserFromGlobalGroup(user.getUsername(), legends);

		assertThat(userServices.getUser(user.getUsername()).getGlobalGroups()).doesNotContain(legends);
	}

	@Test(expected = UserServicesRuntimeException_NoSuchGroup.class)
	public void givenUserInGroupWhenRemoveUserFromInexistentGroupThenException()
			throws Exception {
		givenCollection1();
		givenLegendsGroupWithAllUsersInCollections(collection1);
		givenUserWith(asList(legends), asList(collection1));

		userServices.removeUserFromGlobalGroup(user.getUsername(), "inexistentGroup");
	}

	@Test
	public void givenUserAndPasswordWhenGetTokenThenItsReturned()
			throws Exception {
		givenCollection(zeCollection);
		setupAfterCollectionCreation();

		givenUserAndPassword();
		String serviceKey = userServices.giveNewServiceToken(user);

		String token = userServices.getToken(serviceKey, user.getUsername(), "1qaz2wsx");

		user = userServices.getUser(user.getUsername());
		assertThat(user.getAccessTokens()).containsKey(token);
	}

	@Test
	@SlowTest
	public void givenTokenIsGivenToUserThenExpiresAutomatically()
			throws Exception {
		givenCollection(zeCollection);
		setupAfterCollectionCreation();

		givenUserAndPassword();
		String serviceKey = userServices.giveNewServiceToken(user);
		String token = userServices.getToken(serviceKey, user.getUsername(), "1qaz2wsx");

		user = userServices.getUser(user.getUsername());
		for (int i = 0; i < 2000 && !userServices.getUser(user.getUsername()).getAccessTokens().isEmpty(); i++) {
			Thread.sleep(50);
		}

		assertThat(userServices.getUser(user.getUsername()).getAccessTokens()).isEmpty();
	}

	//@Test
	public void whenGeneratingALotOfTokensOnlyKeepLastFive()
			throws Exception {
		givenCollection(zeCollection);
		setupAfterCollectionCreation();
		givenUserAndPassword();

		for (int i = 0; i < 10; i++) {
			userServices.generateToken(user.getUsername());
		}

		List<String> last50Tokens = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			last50Tokens.add(userServices.generateToken(user.getUsername()));
		}

		assertThat(userServices.getUser(user.getUsername()).getTokenKeys()).containsOnly(last50Tokens.toArray(new String[0]));
	}

	@Test(expected = UserServicesRuntimeException_InvalidUserNameOrPassword.class)
	public void givenUserAndPasswordWhenGetTokenThenException()
			throws Exception {

		setupAfterCollectionCreation();

		givenUserAndPassword();
		String serviceKey = userServices.giveNewServiceToken(user);

		userServices.getToken(serviceKey, user.getUsername(), "wrongPassword");
	}

	@Test(expected = UserServicesRuntimeException_CannotRemoveAdmin.class)
	public void givenAdminUserWhenRemovingHimThenException()
			throws Exception {

		setupAfterCollectionCreation();
		UserCredential admin = userServices.getUserCredential("admin");
		try {
			userServices.removeUserCredentialAndUser(admin);
		} finally {
			assertThat(userServices.getUserCredential("admin")).isNotNull();
			assertThat(userServices.getUserCredential("admin").getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
		}
	}

	@Test(expected = UserServicesRuntimeException_CannotRemoveAdmin.class)
	public void givenAdminUserWhenChangeStatusThenException()
			throws Exception {

		setupAfterCollectionCreation();
		UserCredential admin = userServices.getUserCredential("admin");
		admin = admin.withStatus(UserCredentialStatus.DELETED);
		try {
			userServices.addUpdateUserCredential(admin);
		} finally {
			assertThat(userServices.getUserCredential("admin")).isNotNull();
			assertThat(userServices.getUserCredential("admin").getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
		}
	}
	//---------

	@Test
	public void whenGetServiceKeyByTokenThenReturnIt()
			throws Exception {
		givenCollection(zeCollection);
		setupAfterCollectionCreation();

		givenUserAndPassword();
		String serviceKey = userServices.giveNewServiceToken(user);
		String token = userServices.getToken(serviceKey, user.getUsername(), "1qaz2wsx");
		user = userServices.getUser(user.getUsername());

		String newToken = userServices.getToken(serviceKey, token);
		user = userServices.getUser(user.getUsername());
		assertThat(user.getAccessTokens()).containsKey(newToken);
		assertThat(user.getAccessTokens()).doesNotContainKey(token);

	}

	@Test
	public void givenWrongTokenWhenGetServiceKeyByTokenThenException()
			throws Exception {
		givenCollection(zeCollection);
		setupAfterCollectionCreation();

		givenUserAndPassword();
		String serviceKey = userServices.giveNewServiceToken(user);
		String token = userServices.getToken(serviceKey, user.getUsername(), "1qaz2wsx");
		user = userServices.getUser(user.getUsername());

		try {
			userServices.getToken(serviceKey, "wrongToken");
			fail();
		} catch (Exception e) {
			assertThat(user.getAccessTokens()).containsKey(token);
		}
	}

	@Test
	public void whenGetTokenUserThenReturnUsername()
			throws Exception {

		givenCollection(zeCollection);
		setupAfterCollectionCreation();

		givenUserAndPassword();
		String serviceKey = userServices.giveNewServiceToken(user);
		String token = userServices.getToken(serviceKey, user.getUsername(), "1qaz2wsx");
		user = userServices.getUser(user.getUsername());

		String username = userServices.getTokenUser(serviceKey, token);

		assertThat(user.getUsername()).isEqualTo(username);

	}

	@Test
	public void whenCreatingGroupsWithHierarchyThenOk()
			throws Exception {
		givenCollection1();

		GlobalGroup group1 = userServices.createGlobalGroup("group1", "group1", asList(""), null, GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1 = userServices.createGlobalGroup(
				"group1_1", "group1_1", asList(""), "group1", GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1_1 = userServices.createGlobalGroup(
				"group1_1_1", "group1_1_1", asList(""), "group1_1", GlobalGroupStatus.ACTIVE, true);

		userServices.addUpdateGlobalGroup(group1);
		userServices.addUpdateGlobalGroup(group1_1);
		userServices.addUpdateGlobalGroup(group1_1_1);

		assertThat(userServices.getChildrenOfGroupInCollection("group1", "collection1")).hasSize(1);
		assertThat(userServices.getChildrenOfGroupInCollection("group1", "collection1").get(0).getCode()).isEqualTo("group1_1");
		assertThat(userServices.getChildrenOfGroupInCollection("group1_1", "collection1")).hasSize(1);
		assertThat(userServices.getChildrenOfGroupInCollection("group1_1", "collection1").get(0).getCode())
				.isEqualTo("group1_1_1");
		assertThat(userServices.getChildrenOfGroupInCollection("group1_1_1", "collection1")).isEmpty();
	}

	@Test
	public void givenGroupHierarchyWhenRemoveGroupFromCollectionThenRemoveHierarchy()
			throws Exception {
		givenCollection1();
		GlobalGroup group1 = userServices.createGlobalGroup("group1", "group1", asList(""), null, GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1 = userServices.createGlobalGroup(
				"group1_1", "group1_1", asList(""), "group1", GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1_1 = userServices.createGlobalGroup(
				"group1_1_1", "group1_1_1", asList(""), "group1_1", GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(group1);
		userServices.addUpdateGlobalGroup(group1_1);
		userServices.addUpdateGlobalGroup(group1_1_1);

		UserCredential admin = userServices.getUserCredential("admin");
		userServices.removeGroupFromCollections(admin, "group1", Arrays.asList("collection1"));

		assertThat(userServices.getGroupInCollection("group1", "collection1").getWrappedRecord()
				.get(Schemas.LOGICALLY_DELETED_STATUS)).isEqualTo(true);
		assertThat(userServices.getChildrenOfGroupInCollection("group1", "collection1")).isEmpty();
		assertThat(userServices.getChildrenOfGroupInCollection("group1_1", "collection1")).isEmpty();
		assertThat(userServices.getChildrenOfGroupInCollection("group1_1_1", "collection1")).isEmpty();
	}

	@Test
	public void givenGroupHierarchyWhenRemoveGroupThenRemoveHierarchy()
			throws Exception {
		givenCollection1();

		List emptyList = new ArrayList<>();
		GlobalGroup group1 = userServices.createGlobalGroup("group1", "group1", emptyList, null, GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1 = userServices.createGlobalGroup(
				"group1_1", "group1_1", emptyList, "group1", GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1_1 = userServices.createGlobalGroup(
				"group1_1_1", "group1_1_1", emptyList, "group1_1", GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(group1);
		userServices.addUpdateGlobalGroup(group1_1);
		userServices.addUpdateGlobalGroup(group1_1_1);

		UserCredential admin = userServices.getUserCredential("admin");
		userServices.logicallyRemoveGroupHierarchy(admin, group1);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection1)
				.where(userServices.groupCodeMetadata(collection1)).isIn(Arrays.asList(group1.getCode(), group1_1.getCode(),
						group1_1_1.getCode()));
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> groupRecords = searchServices.search(query);

		assertThat(groupRecords.get(0).isActive()).isFalse();
		assertThat(groupRecords.get(1).isActive()).isFalse();
		assertThat(groupRecords.get(2).isActive()).isFalse();
		assertThat(globalGroupsManager.getAllGroups()).hasSize(3);
		assertThat(globalGroupsManager.getActiveGroups()).isEmpty();
		assertThat(userServices.getGroup(group1.getCode()).getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
		try {
			userServices.getActiveGroup(group1.getCode());
		} catch (Exception e) {
			assertThat(e.getMessage()).isEqualTo("No such group 'group1'");
		}

	}

	@Test
	public void givenGroupHierarchyWhenActiveGroupThenActiveHierarchy1()
			throws Exception {
		givenCollection1();

		List emptyList = new ArrayList<>();
		GlobalGroup group1 = userServices.createGlobalGroup("group1", "group1", emptyList, null, GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1 = userServices.createGlobalGroup(
				"group1_1", "group1_1", emptyList, "group1", GlobalGroupStatus.ACTIVE, true);
		GlobalGroup group1_1_1 = userServices.createGlobalGroup(
				"group1_1_1", "group1_1_1", emptyList, "group1_1", GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(group1);
		userServices.addUpdateGlobalGroup(group1_1);
		userServices.addUpdateGlobalGroup(group1_1_1);
		UserCredential admin = userServices.getUserCredential("admin");
		userServices.logicallyRemoveGroupHierarchy(admin, group1);

		userServices.activateGlobalGroupHierarchy(admin, group1);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection1)
				.where(userServices.groupCodeMetadata(collection1)).isIn(Arrays.asList(group1.getCode(), group1_1.getCode(),
						group1_1_1.getCode()));
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> groupRecords = searchServices.search(query);

		assertThat(groupRecords.get(0).isActive()).isTrue();
		assertThat(groupRecords.get(1).isActive()).isTrue();
		assertThat(groupRecords.get(2).isActive()).isTrue();
		assertThat(globalGroupsManager.getAllGroups()).hasSize(3);
		assertThat(globalGroupsManager.getActiveGroups()).hasSize(3);
	}

	@Test
	public void tryingToPhysicallyDeleteUser() throws RecordServicesException {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		Users users = new Users();
		users.setUp(getModelLayerFactory().newUserServices());
		User chuck = users.chuckNorrisIn(zeCollection);
		Cart c = rm.getOrCreateUserCart(chuck);
		Transaction t = new Transaction();
		t.add(c);
		recordServices.execute(t);
		userServices = getModelLayerFactory().newUserServices();
		List<String> aliceCollection = users.alice().getCollections();
		try {
			userServices.safePhysicalDeleteUser(users.alice().getUsername());
			int compteur = 0;
			for (String collection : aliceCollection) {
				try {
					userServices.getUserInCollection(users.alice().getUsername(), collection);
				} catch (Exception e) {
					assertThat(e).isInstanceOf(UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser.class);
					compteur++;
				}
			}
			assertThat(compteur).isEqualTo(aliceCollection.size());
			userServices.safePhysicalDeleteUser(chuck.getUsername());
			fail();
		} catch (Exception e) {
			assertThat(e).isInstanceOf(UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically.class);
			e.printStackTrace();
		}
	}

	@Test
	public void tryingToDeleteAllUser() throws Exception {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());
		recordServices = getModelLayerFactory().newRecordServices();
		Users users = new Users();
		users.setUp(getModelLayerFactory().newUserServices());
		User chuck = users.chuckNorrisIn(zeCollection);
		Cart c = rm.getOrCreateUserCart(chuck);
		Transaction t = new Transaction();
		t.add(c);
		recordServices.execute(t);
		userServices = getModelLayerFactory().newUserServices();
		for (UserCredential user : users.getAllUsers()) {
			if (!user.isSystemAdmin()) {
				for (String collection : user.getCollections()) {
					if (!collection.equals("_system_")) {
						userServices.getUserInCollection(user.getUsername(), collection).setStatus(UserCredentialStatus.DELETED);
					}
				}
				user.withStatus(UserCredentialStatus.DELETED);
				userServices.addUpdateUserCredential(user);
			}
		}

		assertThat(userServices.safePhysicalDeleteAllUnusedUser()).extracting("username").containsExactly(chuck.getUsername());
	}


	// ----- Utils methods

	private List<Record> getRecordsInGroupInCollection(Record groupRecord, String collection) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection)
				.where(userServices.userGroupsMetadata(collection)).isEqualTo(groupRecord.getId());
		query.setCondition(condition);
		List<Record> recordsInGroupInCollection = searchServices.search(query);
		return recordsInGroupInCollection;
	}

	private Record getRecordGroupInCollection(GlobalGroup legendsGroup, String collection) {
		LogicalSearchCondition condition;
		condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection).where(userServices.groupCodeMetadata(collection))
				.isEqualTo(legendsGroup.getCode());
		Record recordGroupInCollection = searchServices.searchSingleResult(condition);
		return recordGroupInCollection;
	}

	// ----- Setup methods

	private void assertThatUserIsOnlyInCollections(UserCredential user, String... collections) {
		List<String> expectedCollections = asList(collections);
		for (String collection : allCollections) {
			if (expectedCollections.contains(collection)) {
				assertThat(userServices.getUserInCollection(user.getUsername(), collection).getUsername())
						.isEqualTo(user.getUsername());
			} else {
				try {
					userServices.getUserInCollection(user.getUsername(), collection).getUsername();
					fail("User '" + user + "' should not be in collection '" + collection + "'");
				} catch (UserServicesRuntimeException_UserIsNotInCollection e) {
					// OK
				}
			}
		}
		assertThat(userServices.getUser(user.getUsername()).getCollections()).containsOnly(collections);
	}

	private void givenHeroesGroup() {
		givenHeroesGroupWithAllUsersInCollections();
	}

	private void givenHeroesGroupWithAllUsersInCollections(String... collections) {
		heroes = "heroes";
		GlobalGroup globalGroup = userServices.createGlobalGroup(
				heroes, heroes, asList(collections), null, GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(globalGroup);
	}

	private void givenLegendsGroup() {
		givenLegendsGroupWithAllUsersInCollections();
	}

	private void givenLegendsGroupWithAllUsersInCollections(String... collections) {
		legends = "legends";
		GlobalGroup globalGroup = userServices.createGlobalGroup(
				legends, legends, asList(collections), null, GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(globalGroup);
	}

	private void givenCollection1() {
		collection1 = "collection1";
		prepareSystemWithoutHyperTurbo(withCollection(collection1));
		setupAfterCollectionCreation();
	}

	private void givenCollection1And2() {
		collection1 = "collection1";
		collection2 = "collection2";
		prepareSystemWithoutHyperTurbo(withCollection(collection1), withCollection(collection2));
		setupAfterCollectionCreation();
	}

	private void givenCollection1And2And3() {
		collection1 = "collection1";
		collection2 = "collection2";
		prepareSystemWithoutHyperTurbo(withCollection(collection1), withCollection(collection2));
		collection3 = "collection3";
		givenCollection(collection3);
		setupAfterCollectionCreation();
	}

	private void setupAfterCollectionCreation() {

		allCollections = new ArrayList<>();
		collectionsManager = getAppLayerFactory().getCollectionsManager();
		userServices = getModelLayerFactory().newUserServices();
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		userCredentialsManager = getModelLayerFactory().getUserCredentialsManager();
		globalGroupsManager = getModelLayerFactory().getGlobalGroupsManager();
		authenticationService = getModelLayerFactory().newAuthenticationService();

		Key key = EncryptionKeyFactory.newApplicationKey("zePassword", "zeUltimateSalt");
		ModelLayerFactoryUtils.setApplicationEncryptionKey(getModelLayerFactory(), key);
	}

	private List<String> groups(String... groups) {
		return asList(groups);
	}

	private List<String> and(String... collections) {
		return asList(collections);
	}

	private void givenUserAndPassword() {
		user = userServices.createUserCredential(
				chuckNorris, "Chuck", "Norris", "chuck.norris@doculibre.com", new ArrayList<String>(), new ArrayList<String>(),
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);
		userServices.addUpdateUserCredential(user);
		authenticationService.changePassword(user.getUsername(), "1qaz2wsx");
		user = userServices.getUser(user.getUsername());
	}

	private void givenUserWith(List<String> groups, List<String> collections) {
		user = userServices.createUserCredential(
				chuckNorris, "Chuck", "Norris", "chuck.norris@doculibre.com", groups, collections, UserCredentialStatus.ACTIVE,
				"domain", msExchDelegateListBL, null).withSystemAdminPermission();
		userServices.addUpdateUserCredential(user);
	}

	private void givenAnotherUserWith(List<String> groups, List<String> collections) {
		anotherUser = userServices.createUserCredential(
				"gandalf.leblanc", "Gandalf", "Leblanc", "gandalf.leblanc@doculibre.com", groups, collections,
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);
		userServices.addUpdateUserCredential(anotherUser);
	}

	private void givenAThirdUserWith(List<String> groups, List<String> collections) {
		thirdUser = userServices.createUserCredential(
				"edouard.lechat", "Edouard", "Lechat", "edouard.lechat@doculibre.com", groups, collections,
				UserCredentialStatus.ACTIVE, "domain", msExchDelegateListBL, null);
		userServices.addUpdateUserCredential(thirdUser);
	}
}
