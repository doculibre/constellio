package com.constellio.model.services.users;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_AtLeastOneCollectionRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotAssignUserToGroupsInOtherCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotChangeNameOfSyncedUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotChangeStatusOfSyncedGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotChangeStatusOfSyncedUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveSyncedGroupFromSyncedCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_EmailRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_FirstNameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidCollectionForUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUsername;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_LastNameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserAlreadyExists;
import com.constellio.sdk.tests.ConstellioTest;
import lombok.AllArgsConstructor;
import org.joda.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.constellio.model.conf.LDAPTestConfig.getGroupBaseContextList;
import static com.constellio.model.conf.LDAPTestConfig.getGroupFiler;
import static com.constellio.model.conf.LDAPTestConfig.getPassword;
import static com.constellio.model.conf.LDAPTestConfig.getScheduleTimeList;
import static com.constellio.model.conf.LDAPTestConfig.getUser;
import static com.constellio.model.conf.LDAPTestConfig.getUserFiler;
import static com.constellio.model.conf.LDAPTestConfig.getUserFilterGroupsList;
import static com.constellio.model.conf.LDAPTestConfig.getUsersWithoutGroupsBaseContextList;
import static com.constellio.model.conf.LDAPTestConfig.isMembershipAutomaticDerivationActivated;
import static com.constellio.model.entities.security.global.GlobalGroupStatus.INACTIVE;
import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static com.constellio.model.entities.security.global.UserCredentialStatus.DISABLED;
import static com.constellio.model.entities.security.global.UserCredentialStatus.PENDING;
import static com.constellio.model.entities.security.global.UserCredentialStatus.SUSPENDED;
import static com.constellio.model.entities.security.global.UserSyncMode.LOCALLY_CREATED;
import static com.constellio.model.entities.security.global.UserSyncMode.NOT_SYNCED;
import static com.constellio.model.entities.security.global.UserSyncMode.SYNCED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatException;
import static com.constellio.sdk.tests.TestUtils.instanceOf;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.when;

public class UserServicesRefactAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;
	SearchServices searchServices;
	UserServices services;

	String collection1 = "collection1";
	String collection2 = "collection2";
	String collection3 = "collection3";

	RMSchemasRecordsServices collection1Schemas;
	RMSchemasRecordsServices collection2Schemas;
	RMSchemasRecordsServices collection3Schemas;
	SchemasRecordsServices systemSchemas;

	@Mock LDAPConfigurationManager ldapConfigurationManagerMock;

	@Before
	public void setup() throws Exception {
		prepareSystem(withCollection(collection1).withConstellioRMModule(), withCollection(collection2).withConstellioRMModule(), withCollection(collection3).withConstellioRMModule());
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		services = getModelLayerFactory().newUserServices();

		collection1Schemas = new RMSchemasRecordsServices(collection1, getAppLayerFactory());
		collection2Schemas = new RMSchemasRecordsServices(collection2, getAppLayerFactory());
		collection3Schemas = new RMSchemasRecordsServices(collection3, getAppLayerFactory());
		systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, getModelLayerFactory());

		for (String collection : asList(collection1, collection2, collection3)) {
			AdministrativeUnit au = new RMSchemasRecordsServices(collection, getAppLayerFactory()).newAdministrativeUnit();
			recordServices.add(au.setCode("ze-unit").setTitle("Ze unit"));
		}
		Toggle.VALIDATE_USER_COLLECTIONS.enable();
	}

	@After
	public void validateIntegrity() {

		for (String collection : asList(collection1, collection2, collection3)) {
			RMSchemasRecordsServices schemas = new RMSchemasRecordsServices(collection, getAppLayerFactory());

			LogicalSearchQuery userQuery = new LogicalSearchQuery(from(schemas.user.schemaType()).returnAll());
			userQuery.addFieldFacet(schemas.user.username().getDataStoreCode());
			searchServices.query(userQuery).getFieldFacetValues(schemas.user.username().getDataStoreCode()).forEach(
					(facetValue -> assertThat(facetValue.getQuantity()).describedAs("User '" + facetValue.getValue() + "' is found twice").isEqualTo(1L))
			);

			LogicalSearchQuery groupQuery = new LogicalSearchQuery(from(schemas.group.schemaType()).returnAll());
			groupQuery.addFieldFacet(schemas.group.code().getDataStoreCode());
			searchServices.query(groupQuery).getFieldFacetValues(schemas.group.code().getDataStoreCode()).forEach(
					(facetValue -> assertThat(facetValue.getQuantity()).describedAs("Group '" + facetValue.getValue() + "' is found twice").isEqualTo(1L))
			);
		}

	}

	@Test
	public void whenCreatingUserThenValidateFieldsAndSaveTheUser() throws Exception {

		assertThatException(() -> services.createUser("andre geant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addToCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_InvalidUsername.class));

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com"))
		).is(instanceOf(UserServicesRuntimeException_AtLeastOneCollectionRequired.class));

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.addToCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_EmailRequired.class));

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setLastName("Le géant")
				.setEmail("andre@constellio.com").addToCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_FirstNameRequired.class));

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setFirstName("André")
				.setEmail("andre@constellio.com").addToCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_LastNameRequired.class));

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addToCollection("inexistingCollection"))
		).is(instanceOf(UserServicesRuntimeException_InvalidCollectionForUser.class));


		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addToCollection(collection1).addToGroupInEachCollection("inexistingGroup"))
		).is(instanceOf(UserServicesRuntimeException_InvalidGroup.class));

		services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addToCollections(collection1, collection2));

		SystemWideUserInfos userInfos = services.getUserInfos("andregeant");
		assertThat(userInfos.getUsername()).isEqualTo("andregeant");
		assertThat(userInfos.getEmail()).isEqualTo("andre@constellio.com");

		assertThatUser("andregeant").hasName("André", "Le géant").isInCollections(collection1, collection2);

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre2@constellio.com").addToCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_UserAlreadyExists.class));
		assertThat(user("andregeant", collection1).getEmail()).isEqualTo("andre@constellio.com");

		//Same request, using execute
		services.execute("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre2@constellio.com").addToCollection(collection1));
		assertThat(services.getUserInfos("andregeant").getEmail()).isEqualTo("andre2@constellio.com");
		assertThat(user("andregeant", collection1).getEmail()).isEqualTo("andre2@constellio.com");
		assertThat(user("andregeant", collection2).getEmail()).isEqualTo("andre2@constellio.com");


	}


	@Test
	public void givenExistingUserNameAndEmailThenCanModifyInfos() {
		services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addToCollections(collection1, collection2));

		//Possible to modify infos without passing name, email, etc.
		services.execute("andregeant", (req) -> req.setDn("andre"));
		assertThat(userInfos("andregeant").getDn()).isEqualTo("andre");

		//Also possible to change name and email
		services.execute("andregeant", (req) -> req.setName("Géant", "Ferré"));
		assertThat(userInfos("andregeant").getFirstName()).isEqualTo("Géant");
		assertThat(userInfos("andregeant").getLastName()).isEqualTo("Ferré");


		//TODO Rabab : Tester toutes les métadonnées (ex. jobTitle, phone, etc.) en ajout/modification
	}


	//	@Test
	//	public void whenAddUpdatingUserTrivialInfosThenSaved() {
	//		fail("TODO : Tester toutes les métadonnées (ex. jobTitle, phone, etc.) en ajout/modification");
	//	}


	@Test
	public void givenUsersInDifferentCollectionsWhenStreamingUsersThenAllReturnedWihoutDuplicates() {
		services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addToCollections(collection2, collection3));

		services.createUser("kane", (req) -> req.setName("Fake", "Diesel")
				.setEmail("kane@constellio.com").addToCollections(collection2));

		services.createUser("machoman", (req) -> req.setName("Macho", "Man")
				.setEmail("machoman@constellio.com").addToCollections(collection1, collection3));

		services.createUser("sting", (req) -> req.setName("Blade Runner", "Sting")
				.setEmail("sting@constellio.com").addToCollections(collection1, collection2, collection3));

		assertThat(services.streamUserInfos().filter((u -> !u.getUsername().equals("admin")))
				.map((SystemWideUserInfos::getUsername)).collect(toList()))
				.containsExactly("andregeant", "kane", "machoman", "sting");

		assertThat(services.streamUserInfos().filter((u -> !u.getUsername().equals("admin"))).findFirst().get().getCollections()).containsOnly(collection2, collection3);

		assertThat(services.streamUserInfos(collection1)
				.filter((u -> !u.getUsername().equals("admin"))).map((SystemWideUserInfos::getUsername)).collect(toList()))
				.containsExactly("machoman", "sting");

		assertThat(services.streamUserInfos(collection2)
				.filter((u -> !u.getUsername().equals("admin"))).map((SystemWideUserInfos::getUsername)).collect(toList()))
				.containsExactly("andregeant", "kane", "sting");

		assertThat(services.streamUserInfos(collection3)
				.filter((u -> !u.getUsername().equals("admin"))).map((SystemWideUserInfos::getUsername)).collect(toList()))
				.containsExactly("andregeant", "machoman", "sting");

		assertThat(services.streamUser(collection1).filter((u -> !u.getUsername().equals("admin"))).map((User::getUsername)).collect(toList()))
				.containsExactly("machoman", "sting");

		assertThat(services.streamUser(collection2).filter((u -> !u.getUsername().equals("admin"))).map((User::getUsername)).collect(toList()))
				.containsExactly("andregeant", "kane", "sting");

		assertThat(services.streamUser(collection3).filter((u -> !u.getUsername().equals("admin"))).map((User::getUsername)).collect(toList()))
				.containsExactly("andregeant", "machoman", "sting");

	}

	@Test
	public void whenDeletingUsersThenDeleteThemWhereverPossible() {

		services.createUser("rey", (req) -> req.setNameEmail("Rey", "Mysterio", "rey.mysterio@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("kane", (req) -> req.setNameEmail("Fake", "Diesel", "kane@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("ric", (req) -> req.setNameEmail("Ric", "Flair", "ric.flair@constellio.com")
				.addToCollections(collection1, collection2));
		givenUserCreatedRecordsInCollection("ric", collection2);

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1, collection2));
		givenUserCreatedUserFoldersInCollection("embalmer", collection1);

		services.createUser("randy", (req) -> req.setNameEmail("Randy", "Orton", "randy.orton@constellio.com")
				.addToCollections(collection1, collection2));
		givenUserCreatedRecordsInCollection("randy", collection1);

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection2));
		givenUserCreatedUserFoldersInCollection("undertaker", collection2);

		services.createUser("andre", (req) -> req.setNameEmail("André", "Le géant", "andre@constellio.com")
				.addToCollections(collection1, collection2));
		createAuthorisationGivingAccessToUserInCollection("andre", collection1);

		services.createUser("machoman", (req) -> req.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection2));
		createAuthorisationGivingAccessToUserInCollection("machoman", collection1);

		services.execute("rey", (req) -> req.removeFromAllCollections());
		services.execute("ric", (req) -> req.removeFromAllCollections());
		services.execute("embalmer", (req) -> req.removeFromAllCollections());
		services.execute("andre", (req) -> req.removeFromAllCollections());

		services.execute("kane", (req) -> req.removeFromCollection(collection1));
		services.execute("randy", (req) -> req.removeFromCollection(collection1));
		services.execute("undertaker", (req) -> req.removeFromCollection(collection1));
		services.execute("machoman", (req) -> req.removeFromCollection(collection1));

		assertThatUser("rey").doesNotExist();
		assertThatUser("ric").isPhysicicallyDeletedIn(collection1).hasStatusIn(DISABLED, collection2);
		assertThatUser("embalmer").hasStatusIn(DISABLED, collection1).isPhysicicallyDeletedIn(collection2);
		assertThatUser("andre").hasStatusIn(DISABLED, collection1).isPhysicicallyDeletedIn(collection2);

		assertThatUser("kane").isPhysicicallyDeletedIn(collection1).hasStatusIn(ACTIVE, collection2);
		assertThatUser("randy").hasStatusIn(DISABLED, collection1).hasStatusIn(ACTIVE, collection2);
		assertThatUser("undertaker").isPhysicicallyDeletedIn(collection1).hasStatusIn(ACTIVE, collection2);
		assertThatUser("machoman").hasStatusIn(DISABLED, collection1).hasStatusIn(ACTIVE, collection2);

	}

	@Test
	public void whenChangingStatusThenAppliedToSpecifiedCollectionsAndUpdateLogicallyDeletedMetadata() {

		services.createUser("randy", (req) -> req.setNameEmail("Randy", "Orton", "randy.orton@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("shawn", (req) -> req.setNameEmail("Shawn", "Michaels", "shawn.michaels@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("rey", (req) -> req.setNameEmail("Rey", "Mysterio", "rey.mysterio@constellio.com")
				.addToCollections(collection1, collection2));


		services.execute("randy", (req) -> req.setStatusForAllCollections(SUSPENDED));

		services.execute("undertaker", (req) -> req.setStatusForCollection(PENDING, collection2));

		services.execute("shawn", (req) -> req.setStatusForCollection(DISABLED, collection1));

		services.execute("rey", (req) -> req.setStatusForCollection(PENDING, collection1)
				.setStatusForCollection(SUSPENDED, collection2));

		assertThatUser("randy").hasStatusIn(SUSPENDED, collection1).hasStatusIn(SUSPENDED, collection2);
		assertThatUser("undertaker").hasStatusIn(ACTIVE, collection1).hasStatusIn(PENDING, collection2);
		assertThatUser("shawn").hasStatusIn(DISABLED, collection1).hasStatusIn(ACTIVE, collection2);
		assertThatUser("rey").hasStatusIn(PENDING, collection1).hasStatusIn(SUSPENDED, collection2);

		assertThat(userInfos("randy").hasStatusInAllCollection(SUSPENDED)).isTrue();
		assertThat(userInfos("randy").hasStatusInAnyCollection(SUSPENDED)).isTrue();
		assertThat(userInfos("randy").hasStatusInAllCollection(PENDING)).isFalse();
		assertThat(userInfos("randy").hasStatusInAnyCollection(PENDING)).isFalse();

		assertThat(userInfos("rey").hasStatusInAllCollection(PENDING)).isFalse();
		assertThat(userInfos("rey").hasStatusInAnyCollection(PENDING)).isTrue();
		assertThat(userInfos("rey").hasStatusInAllCollection(SUSPENDED)).isFalse();
		assertThat(userInfos("rey").hasStatusInAnyCollection(SUSPENDED)).isTrue();

		services.execute("rey", (req) -> req.setStatusForAllCollections(ACTIVE));
		assertThatUser("rey").hasStatusIn(ACTIVE, collection1).hasStatusIn(ACTIVE, collection2);
	}

	@Test
	public void whenAddingAndRemovingCollectionsThenApplied() {
		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1, collection2));
		givenUserCreatedUserFoldersInCollection("embalmer", collection2);

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection2));
		givenUserCreatedRecordsInCollection("undertaker", collection2);

		services.createUser("machoman", (req) -> req.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection3));

		services.execute("embalmer", (req) -> req.addToCollection(collection3).removeFromCollection(collection2));
		services.execute("undertaker", (req) -> req.addToCollection(collection3).removeFromCollection(collection2));
		services.execute("machoman", (req) -> req.addToCollection(collection3).removeFromCollection(collection2));

		assertThatUser("embalmer")
				.hasStatusIn(ACTIVE, collection1)
				.hasStatusIn(DISABLED, collection2)
				.hasStatusIn(ACTIVE, collection3);

		assertThatUser("undertaker")
				.hasStatusIn(ACTIVE, collection1)
				.hasStatusIn(DISABLED, collection2)
				.hasStatusIn(ACTIVE, collection3);

		assertThatUser("machoman")
				.hasStatusIn(ACTIVE, collection1)
				.isPhysicicallyDeletedIn(collection2)
				.hasStatusIn(ACTIVE, collection3);
	}

	@Test
	public void givenGroupsInDifferentCollectionsWhenStreamingGroupsThenAllReturnedWihoutDuplicates() {

		services.createGroup("andregeantGroup", (req) -> req.setName("Groupe d'André")
				.addCollections(collection2, collection3));

		services.createGroup("kaneGroup", (req) -> req.setName("Groupe de Kane")
				.addCollections(collection2));

		services.createGroup("machomanGroup", (req) -> req.setName("Groupe de Machoman")
				.addCollections(collection1, collection3));

		services.createGroup("stingGroup", (req) -> req.setName("Groupe de Sting")
				.addCollections(collection1, collection2, collection3));

		assertThat(services.streamGroupInfos()
				.map((SystemWideGroup::getCode)).collect(toList()))
				.containsExactly("andregeantGroup", "kaneGroup", "machomanGroup", "stingGroup");

		assertThat(services.streamGroupInfos().findFirst().get().getCollections()).containsOnly(collection2, collection3);

		assertThat(services.streamGroupInfos(collection1).map((SystemWideGroup::getCode)).collect(toList()))
				.containsExactly("machomanGroup", "stingGroup");

		assertThat(services.streamGroupInfos(collection2).map((SystemWideGroup::getCode)).collect(toList()))
				.containsExactly("andregeantGroup", "kaneGroup", "stingGroup");

		assertThat(services.streamGroupInfos(collection3).map((SystemWideGroup::getCode)).collect(toList()))
				.containsExactly("andregeantGroup", "machomanGroup", "stingGroup");

		assertThat(services.streamGroup(collection1).map((Group::getCode)).collect(toList()))
				.containsExactly("machomanGroup", "stingGroup");

		assertThat(services.streamGroup(collection2).map((Group::getCode)).collect(toList()))
				.containsExactly("andregeantGroup", "kaneGroup", "stingGroup");

		assertThat(services.streamGroup(collection3).map((Group::getCode)).collect(toList()))
				.containsExactly("andregeantGroup", "machomanGroup", "stingGroup");

	}

	@Test
	public void whenCreatingGroupsThenValidateFieldsAndSaveTheGroup() {

		assertThatException(() -> services.createGroup("g1", (req) -> req.setName(null).addCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_NameRequired.class));

		assertThatException(() -> services.createGroup("g1", (req) -> req.setName("G1"))
		).is(instanceOf(UserServicesRuntimeException_AtLeastOneCollectionRequired.class));

		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		assertThat(services.getGroup("g1").getName()).isEqualTo("Group 1");
		assertThatGroup("g1").isInCollections(collection1, collection2);

		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2));
		assertThatGroup("g2").hasCode("g2").hasTitle("Group 2").hasCaption("Group 1 / Group 2").isInCollections(collection1, collection2);
		assertThat(services.getGroup("g2").getAncestors()).containsOnly("g1");
		assertThat(group("g2", collection1).getAncestors()).containsOnly(group("g1", collection1).getId());
		assertThat(group("g2", collection2).getAncestors()).containsOnly(group("g1", collection2).getId());
		assertThat(group("g2", collection1).getParent()).isEqualTo(group("g1", collection1).getId());
		assertThat(group("g2", collection2).getParent()).isEqualTo(group("g1", collection2).getId());


		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g1").addCollections(collection1, collection2));
		assertThatGroup("g3").hasCode("g3").hasTitle("Group 3").hasCaption("Group 1 / Group 3").isInCollections(collection1, collection2);
		assertThat(services.getGroup("g3").getAncestors()).containsOnly("g1");
		assertThat(group("g3", collection1).getParent()).isEqualTo(group("g1", collection1).getId());
		assertThat(group("g3", collection1).getAncestors()).containsOnly(group("g1", collection1).getId());
		assertThat(group("g3", collection2).getParent()).isEqualTo(group("g1", collection2).getId());
		assertThat(group("g3", collection2).getAncestors()).containsOnly(group("g1", collection2).getId());

		services.execute(services.request("g3").setParent("g2"));
		assertThatGroup("g3").hasCode("g3").hasTitle("Group 3").hasCaption("Group 1 / Group 2 / Group 3").isInCollections(collection1, collection2);
		assertThat(group("g3", collection1).getParent()).isEqualTo(group("g2", collection1).getId());
		assertThat(group("g3", collection1).getAncestors()).containsOnly(
				group("g1", collection1).getId(), group("g2", collection1).getId());
		assertThat(group("g3", collection2).getParent()).isEqualTo(group("g2", collection2).getId());
		assertThat(group("g3", collection2).getAncestors()).containsOnly(
				group("g1", collection2).getId(), group("g2", collection2).getId());

	}

	//@Test
	public void whenCreatingGroupThenValidateFieldsAndSaveTheGroup() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		assertThat(services.getGroup("g1").getName()).isEqualTo("Group 1");
		assertThat(services.getGroup("g1").getCollections()).containsOnly(collection1, collection2);

		assertThatGroup("g1").isInCollections(collection1, collection2);
		String g1IdBeforeRemoveCollection = group("g1", collection2).getId();

		services.executeGroupRequest("g1", (req) -> req.removeCollection(collection2));

		assertThat(services.getGroup("g1").getCollections()).containsOnly(collection1);
		assertThatGroup("g1").isInCollections(collection1);

		services.createGroup("g1", (req) -> req.addCollection(collection2));
		assertThat(group("g1", collection2).getId()).isNotEqualTo(g1IdBeforeRemoveCollection);

		services.createGroup("g1", (req) -> req.removeCollections(collection1, collection2));
		assertThatGroup("g1").doesNotExist();
	}


	@Test
	public void whenCreatingOrModifyingGroupsThenAChildGroupIsNeverOrphanInACollection() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		assertThat(services.getGroup("g1").getName()).isEqualTo("Group 1");
		assertThat(services.getGroup("g1").getCollections()).containsOnly(collection1, collection2);

		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1")
				.addCollections(collection1));

		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2")
				.addCollections(collection1));

		services.createGroup("g4", (req) -> req.setName("Group 4").setParent("g3")
				.addCollections(collection2));

		assertThatGroup("g1").isInCollections(collection1, collection2);
		assertThatGroup("g2").isInCollections(collection1, collection2);
		assertThatGroup("g3").isInCollections(collection1, collection2);
		assertThatGroup("g4").isInCollections(collection2);

		services.executeGroupRequest("g3", (req) -> req.addCollection(collection3));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3);
		assertThatGroup("g2").isInCollections(collection1, collection2, collection3);
		assertThatGroup("g3").isInCollections(collection1, collection2, collection3);
		assertThatGroup("g4").isInCollections(collection2);
		String g4IdBeforeRemoveCollection = group("g4", collection2).getId();

		services.executeGroupRequest("g2", (req) -> req.removeCollection(collection2));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3);
		assertThatGroup("g2").isInCollections(collection1, collection3);
		assertThatGroup("g3").isInCollections(collection1, collection3);
		assertThatGroup("g4").doesNotExist();

		services.createGroup("g4", (req) -> req.addCollection(collection2));
		//assertThatGroup("g1").isInCollections(collection1, collection2, collection3);
		//assertThatGroup("g2").isInCollections(collection1, collection2, collection3);
		//assertThatGroup("g3").isInCollections(collection1, collection2, collection3);
		//assertThatGroup("g4").isInCollections(collection2);
		assertThat(group("g4", collection2).getId()).isNotEqualTo(g4IdBeforeRemoveCollection);
	}

	@Test
	public void givenGroupIsUsedWithAnAuthorisationWhenRemovingCollectionsThenDisabledInstead() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2, collection3));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2, collection3));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1, collection2, collection3));
		services.createGroup("g4", (req) -> req.setName("Group 4").setParent("g3").addCollections(collection1, collection2, collection3));

		createAuthorisationGivingAccessToGroupInCollection("g4", collection2);
		createAuthorisationGivingAccessToGroupInCollection("g3", collection3);

		UserAndGroupsAddUpdateResponse response = services.executeGroupRequest("g2", (req) -> req.removeCollections(collection1, collection2, collection3));

		assertThat(response.getErrors().size()).isEqualTo(5);
		assertThatResponse(response).containsGroupInCollection("g2", "collection2");
		assertThatResponse(response).containsGroupInCollection("g2", "collection3");
		assertThatResponse(response).containsGroupInCollection("g3", "collection2");
		assertThatResponse(response).containsGroupInCollection("g3", "collection3");
		assertThatResponse(response).containsGroupInCollection("g4", "collection2");

		assertThatResponse(response).hasExactlyErrorCodes(UserServices.GROUP_CANNOT_BE_DELETED);

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection2, collection3).isInactiveInAllItsCollections();
		assertThatGroup("g3").isInCollections(collection2, collection3).isInactiveInAllItsCollections();
		assertThatGroup("g4").isInCollections(collection2).isInactiveInAllItsCollections();

		services.executeGroupRequest("g2", (req) -> req.addCollections(collection1, collection2, collection3));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g3").isInCollections(collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g4").isInCollections(collection2).isActiveInAllItsCollections();
	}

	@Test
	public void givenGroupIsUsedWithAnAuthorisationWhenRemovingItThenDisabledInstead() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2, collection3));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2, collection3));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1, collection2, collection3));
		services.createGroup("g4", (req) -> req.setName("Group 4").setParent("g3").addCollections(collection1, collection2, collection3));

		createAuthorisationGivingAccessToGroupInCollection("g4", collection2);
		createAuthorisationGivingAccessToGroupInCollection("g3", collection3);

		services.executeGroupRequest("g2", (req) -> req.markForDeletionInAllCollections());

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection2, collection3).isInactiveInAllItsCollections();
		assertThatGroup("g3").isInCollections(collection2, collection3).isInactiveInAllItsCollections();
		assertThatGroup("g4").isInCollections(collection2).isInactiveInAllItsCollections();

		services.executeGroupRequest("g2", (req) -> req.addCollections(collection1, collection2, collection3));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g3").isInCollections(collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g4").isInCollections(collection2).isActiveInAllItsCollections();


		services.executeGroupRequest("g1", (req) -> req.markForDeletionInCollections(asList(collection2, collection3)));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isOnlyActiveIn(collection1);
		assertThatGroup("g2").isInCollections(collection1, collection2, collection3).isOnlyActiveIn(collection1);
		assertThatGroup("g3").isInCollections(collection2, collection3).isInactiveInAllItsCollections();
		assertThatGroup("g4").isInCollections(collection2).isInactiveInAllItsCollections();
	}

	@Test
	public void whenUpdatingGroupMetadatasThenUpdated() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection3));
		services.executeGroupRequest("g1", (req) -> req.setName("Group 1 modified"));

		assertThatGroup("g1").hasName("Group 1 modified");
	}

	@Test
	public void whenAddingAGroupToOtherCollectionsThenDoNotAddChildGroups() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection3));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1));

		services.executeGroupRequest("g2", (req) -> req.addCollections(collection2, collection3));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections()
				.hasCaption("Group 1 / Group 2");
		assertThatGroup("g3").isInCollections(collection1).isActiveInAllItsCollections()
				.hasCaption("Group 1 / Group 2 / Group 3");
	}

	@Test
	public void whenRemovingAGroupToOtherCollectionsThenDoNotAddChildGroups() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection3));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1));

		services.executeGroupRequest("g2", (req) -> req.addCollections(collection2, collection3));

		assertThatGroup("g1").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection1, collection2, collection3).isActiveInAllItsCollections();
		assertThatGroup("g3").isInCollections(collection1).isActiveInAllItsCollections();
	}

	@Test
	public void givenAnExistingGroupWhenHisParentIsChangedThenAppliedInEachCollections() {
		services.createGroup("oldParent", (req) -> req.setName("Old parent").addCollections(collection1));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("oldParent").addCollections(collection1));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1));

		services.createGroup("newParent", (req) -> req.setName("New parent").addCollections(collection2));

		services.executeGroupRequest("g2", (req) -> req.setParent("newParent"));

		assertThatGroup("oldParent").isInCollections(collection1).isActiveInAllItsCollections();
		assertThatGroup("newParent").isInCollections(collection1, collection2).isActiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection1).isActiveInAllItsCollections()
				.hasCaption("New parent / Group 2");
		assertThatGroup("g3").isInCollections(collection1).isActiveInAllItsCollections()
				.hasCaption("New parent / Group 2 / Group 3");

		services.executeGroupRequest("g2", (req) -> req.setParent(null));
		assertThatGroup("g2").isInCollections(collection1).isActiveInAllItsCollections().hasCaption("Group 2");
		assertThatGroup("g3").isInCollections(collection1).isActiveInAllItsCollections().hasCaption("Group 2 / Group 3");
		assertThatGroup("newParent").isInCollections(collection1, collection2);
	}

	@Test
	public void whenAChildGroupBecomeARootGroupThenUpdated() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("oldParent", (req) -> req.setName("Old Parent").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("oldParent").addCollections(collection1, collection2));

		assertThatGroup("g2").isInCollections(collection1, collection2).isActiveInAllItsCollections().hasCaption("Old Parent / Group 2");

		services.executeGroupRequest("g2", (req) -> req.setParent(null));

		assertThatGroup("g2").isInCollections(collection1, collection2).isActiveInAllItsCollections().hasCaption("Group 2");
	}

	@Test
	public void whenAssigningUserToAGroupInAllCollectionsThenAddedInMatchingCollections() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").addCollections(collection1, collection2));
		services.createGroup("g3", (req) -> req.setName("Group 3").addCollections(collection1, collection2));

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1, collection2).addToGroupsInEachCollection("g1", "g2"));

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection3).addToGroupsInEachCollection("g1", "g2"));

		assertThatUser("embalmer").isInCollections(collection1, collection2);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("embalmer", collection2).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");

		assertThatUser("undertaker").isInCollections(collection1, collection3);
		assertThat(user("undertaker", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("undertaker", collection3).getUserGroups().stream().map(idToCode).collect(toList()))
				.isEmpty();

		services.executeGroupRequest("g1", (req) -> req.addCollection(collection3));
		//Changes nothing

		assertThatUser("embalmer").isInCollections(collection1, collection2);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("embalmer", collection2).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");

		assertThatUser("undertaker").isInCollections(collection1, collection3);
		assertThat(user("undertaker", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("undertaker", collection3).getUserGroups().stream().map(idToCode).collect(toList()))
				.isEmpty();

		services.execute("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection3).addToGroupsInEachCollection("g1", "g2"));

		assertThatUser("undertaker").isInCollections(collection1, collection3);
		assertThat(user("undertaker", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("undertaker", collection3).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1");
	}

	@Test
	public void whenSetStatusToInactiveThenDeactivateGroupAndItsHierarchy() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1, collection2));

		services.executeGroupRequest("g1", (req) -> req.setStatusInAllCollections(INACTIVE));

		assertThatGroup("g1").isInCollections(collection1, collection2).isInactiveInAllItsCollections();
		assertThatGroup("g2").isInCollections(collection1, collection2).isInactiveInAllItsCollections();
		assertThatGroup("g3").isInCollections(collection1, collection2).isInactiveInAllItsCollections();
	}

	@Test
	public void whenAssigningUserToAGroupInSpecificCollectionsThenValidateGroupIsAvailable() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").addCollections(collection1, collection2));
		services.createGroup("g3", (req) -> req.setName("Group 3").addCollections(collection1, collection2));

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1, collection2)
				.addToGroupsInCollection(asList("g1", "g2"), collection1)
				.addToGroupsInCollection(asList("g1", "g2"), collection2));

		assertThatUser("embalmer").isInCollections(collection1, collection2);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("embalmer", collection2).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");

		assertThatException(() -> services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection3).addToGroupsInEachCollection("g1", "g2")
				.addToGroupsInCollection(asList("g1", "g2"), collection1)
				.addToGroupsInCollection(asList("g1", "g2"), collection3))
		).is(instanceOf(UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection.class));

		assertThatException(() -> services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection3).addToGroupsInEachCollection("g1", "g2")
				.addToGroupsInCollection(asList("invalid"), collection1))
		).is(instanceOf(UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection.class));


		assertThatException(() -> services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection3).addToGroupsInEachCollection("g1", "g2")
				.addToGroupsInCollection(asList("g1", "g2"), collection1)
				.addToGroupsInCollection(asList("g1", "g2"), collection2))
		).is(instanceOf(UserServicesRuntimeException_CannotAssignUserToGroupsInOtherCollection.class));

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addToCollections(collection1, collection3).addToGroupsInEachCollection("g1", "g2")
				.addToGroupsInCollection(asList("g1", "g2"), collection1));

		assertThatUser("undertaker").isInCollections(collection1, collection3);
		assertThat(user("undertaker", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThat(user("undertaker", collection3).getUserGroups().stream().map(idToCode).collect(toList()))
				.isEmpty();

	}


	@Test
	public void givenGroupIsAssignedToUsersWhenDeletingItThenDisabledInstead() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1, collection2));

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1)
				.addToGroupsInCollection(asList("g1", "g2"), collection1));
		//				.addToGroupsInCollection(asList("g1", "g2"), collection2));

		assertThatUser("embalmer").isInCollections(collection1);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThatGroup("g1").isInCollections(collection1, collection2).isActiveInAllItsCollections();

		services.executeGroupRequest("g1", (req) -> req.markForDeletionInAllCollections());

		//No changes to the user
		assertThatUser("embalmer").isInCollections(collection1);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");

		//Removed in collection 2, disabled in collection 1
		assertThatGroup("g1").isInCollections(collection1).isInactiveInAllItsCollections();
	}

	@Test
	public void givenGroupIsAssignedToUsersWhenRemovingItFromCollectionsThenDisabledInstead() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1, collection2));

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1)
				.addToGroupsInCollection(asList("g1", "g2"), collection1));
		//				.addToGroupsInCollection(asList("g1", "g2"), collection2));

		assertThatUser("embalmer").isInCollections(collection1);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");
		assertThatGroup("g1").isInCollections(collection1, collection2).isActiveInAllItsCollections();

		services.executeGroupRequest("g1", (req) -> req.removeCollections(collection1, collection2));

		//No changes to the user
		assertThatUser("embalmer").isInCollections(collection1);
		assertThat(user("embalmer", collection1).getUserGroups().stream().map(idToCode).collect(toList()))
				.containsOnly("g1", "g2");

		//Removed in collection 2, disabled in collection 1
		assertThatGroup("g1").isInCollections(collection1).isInactiveInAllItsCollections();
	}

	@Test
	public void whenAssigningAndUnassigningGroupsThenApplied() {
		services.createGroup("g1", (req) -> req.setName("Group 1").addCollections(collection1, collection2));
		services.createGroup("g2", (req) -> req.setName("Group 2").setParent("g1").addCollections(collection1, collection2));
		services.createGroup("g3", (req) -> req.setName("Group 3").setParent("g2").addCollections(collection1, collection2));

		services.createUser("andre", (req) -> req.setNameEmail("André", "Le géant", "andre@constellio.com")
				.addToCollections(collection1, collection2, collection3));

		services.execute("andre", (req) -> req
				.addToGroupsInCollection(asList("g1"), collection1)
				.addToGroupsInCollection(asList("g2"), collection2));

		assertThat(user("andre", collection1).getUserGroups().stream().map(idToCode).collect(toList())).containsOnly("g1");
		assertThat(user("andre", collection2).getUserGroups().stream().map(idToCode).collect(toList())).containsOnly("g2");
		assertThat(user("andre", collection3).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();

		services.execute("andre", (req) -> req.removeFromGroupOfEachCollection("g1"));

		assertThat(user("andre", collection1).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();
		assertThat(user("andre", collection2).getUserGroups().stream().map(idToCode).collect(toList())).containsOnly("g2");
		assertThat(user("andre", collection3).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();

		services.execute("andre", (req) -> req.removeFromGroupOfCollection("g2", collection1));

		assertThat(user("andre", collection1).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();
		assertThat(user("andre", collection2).getUserGroups().stream().map(idToCode).collect(toList())).containsOnly("g2");
		assertThat(user("andre", collection3).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();

		services.execute("andre", (req) -> req.removeFromGroupOfCollection("g2", collection2));

		assertThat(user("andre", collection1).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();
		assertThat(user("andre", collection2).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();
		assertThat(user("andre", collection3).getUserGroups().stream().map(idToCode).collect(toList())).isEmpty();
	}

	@Test
	public void whenSyncingThenUpdatesSyncModeOfFoundUsers() {

		services.createGroup("g1", req -> req.setName("Group 1").addCollections(collection1, collection3));
		services.createGroup("g2", req -> req.setName("Group 2").addCollections(collection1, collection3));

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "undertaker@constellio.com")
				.addToCollections(collection1, collection3));

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1, collection3));

		services.createUser("machoman", (req) -> req.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection3));


		assertThatUser("embalmer").hasSyncMode(LOCALLY_CREATED);
		assertThatUser("undertaker").hasSyncMode(LOCALLY_CREATED);
		assertThatUser("machoman").hasSyncMode(LOCALLY_CREATED);
		assertThatGroup("g1").isLocallyCreated();
		assertThatGroup("g2").isLocallyCreated();


		activateLDAPSyncOnCollections(collection1, collection2);
		services.executeGroupRequest("g1", req -> req.ldapSyncRequest());
		services.executeGroupRequest("g2", req -> req.ldapSyncRequest());
		services.execute("embalmer", req -> req.ldapSyncRequest());
		services.execute("undertaker", req -> req.ldapSyncRequest());

		assertThatUser("embalmer").hasSyncMode(SYNCED);
		assertThatUser("undertaker").hasSyncMode(SYNCED);
		assertThatUser("machoman").hasSyncMode(LOCALLY_CREATED);
		assertThatGroup("g1").isNotLocallyCreated();
		assertThatGroup("g2").isNotLocallyCreated();

		services.execute("undertaker", req -> req.stopSyncingLDAP());

		assertThatUser("embalmer").hasSyncMode(SYNCED);
		assertThatUser("undertaker").hasSyncMode(NOT_SYNCED);
		assertThatUser("machoman").hasSyncMode(LOCALLY_CREATED);
		assertThatGroup("g1").isNotLocallyCreated();
		assertThatGroup("g2").isNotLocallyCreated();

		services.execute("undertaker", req -> req.resumeSyncingLDAP());

		assertThatUser("embalmer").hasSyncMode(SYNCED);
		assertThatUser("undertaker").hasSyncMode(SYNCED);
		assertThatUser("machoman").hasSyncMode(LOCALLY_CREATED);
		assertThatGroup("g1").isNotLocallyCreated();
		assertThatGroup("g2").isNotLocallyCreated();
	}

	@Test
	public void whenAddingUsersToGroupInOtherCollectionsThenAllAdded() {
		services.createGroup("g1", req -> req.setName("Group 1").addCollections(collection1, collection3));
		services.createGroup("g2", req -> req.setName("Group 2").addCollections(collection1, collection3));

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "undertaker@constellio.com")
				.addToCollections(collection1));

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addToCollections(collection1));

		services.execute("undertaker", req -> req
				.addToCollection(collection3)
				.addToGroupInCollection("g1", collection3)
				.addToGroupInCollection("g2", collection3));

		services.execute("embalmer", req -> req
				.addToCollections(collection2, collection3)
				.addToGroupInCollection("g1", collection3)
				.addToGroupInCollection("g2", collection3));

		assertThatUser("undertaker")
				.isInCollections(collection1, collection3)
				.hasGroupsInCollection(collection3, "g1", "g2");

		assertThatUser("embalmer")
				.isInCollections(collection1, collection2, collection3)
				.hasGroupsInCollection(collection3, "g1", "g2");

	}

	@Test
	public void givenSyncedUsersWhenChangingNameEmailInfosWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToCollections(collection1, collection3));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection3));


		//Try to put back embalmer with it's old name. Attempt failed, because the user is synced
		assertThatException(() -> services.execute("embalmer", (req) -> req.setName("Paul", "Bearer"))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeNameOfSyncedUser.class));

		//Try to put back embalmer with it's old email. Attempt failed, because the user is synced
		assertThatException(() -> services.execute("machoman", (req) -> req.setEmail("zemachoman@constellio.com"))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeNameOfSyncedUser.class));

		assertThatUser("embalmer")
				.hasName("William Alvin", "Moody")
				.hasEmail("bill@constellio.com");

		assertThatUser("machoman")
				.hasName("Macho", "Man")
				.hasEmail("machoman@constellio.com");

		services.execute("embalmer", (req) -> req.ldapSyncRequest()
				.setName("Paul", "Bearer").setEmail("embalmer@constellio.com"));

		assertThatUser("embalmer")
				.hasName("Paul", "Bearer")
				.hasEmail("embalmer@constellio.com")
				.hasSyncMode(SYNCED);

		services.execute("machoman", (req) -> req.stopSyncingLDAP()
				.setName("Randy", "Savage").setEmail("randy.savage@constellio.com"));

		assertThatUser("machoman")
				.hasName("Randy", "Savage")
				.hasEmail("randy.savage@constellio.com")
				.hasSyncMode(NOT_SYNCED);
		;

	}

	@Test
	public void givenSyncedUsersWhenAddingSyncedGroupWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection3);

		services.createGroup("g1", req -> req.setName("Group 1").addCollections(collection1, collection3)
				.setLocallyCreated(true));
		services.createGroup("g2", req -> req.setName("Group 2").addCollections(collection1, collection3)
				.setLocallyCreated(false));

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToCollections(collection1, collection3));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection3));

		//No problem since g1 is created locally
		services.execute("embalmer", req -> req
				.addToGroupInCollection("g1", collection1).addToGroupInCollection("g1", collection3));
		services.execute("machoman", req -> req
				.addToGroupInCollection("g1", collection1).addToGroupInCollection("g1", collection3));

		assertThatUser("embalmer")
				.hasGroupsInCollection(collection1, "g1")
				.hasGroupsInCollection(collection3, "g1")
				.hasSyncMode(SYNCED);

		assertThatUser("machoman")
				.hasGroupsInCollection(collection1, "g1")
				.hasGroupsInCollection(collection3, "g1")
				.hasSyncMode(SYNCED);

		assertThatException(() -> services.execute("embalmer", (req) -> req.addToGroupInCollection("g2", collection1))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup.class));

		assertThatException(() -> services.execute("machoman", (req) -> req.addToGroupInCollection("g2", collection3))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup.class));

		assertThatUser("embalmer")
				.hasGroupsInCollection(collection1, "g1")
				.hasGroupsInCollection(collection3, "g1");

		assertThatUser("machoman")
				.hasGroupsInCollection(collection1, "g1")
				.hasGroupsInCollection(collection3, "g1");

		services.execute("embalmer", (req) -> req.ldapSyncRequest().addToGroupInCollection("g2", collection1));

		assertThatUser("embalmer")
				.hasGroupsInCollection(collection1, "g1", "g2")
				.hasGroupsInCollection(collection3, "g1")
				.hasSyncMode(SYNCED);

		services.execute("machoman", (req) -> req.stopSyncingLDAP().addToGroupInCollection("g2", collection1));

		assertThatUser("machoman")
				.hasGroupsInCollection(collection1, "g1", "g2")
				.hasGroupsInCollection(collection3, "g1")
				.hasSyncMode(NOT_SYNCED);

	}


	@Test
	public void givenSyncedUsersWhenRemovingSyncedGroupWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection3);

		services.createGroup("g1", req -> req.setName("Group 1").addCollections(collection1, collection3)
				.setLocallyCreated(true));
		services.createGroup("g2", req -> req.setName("Group 2").addCollections(collection1, collection3)
				.setLocallyCreated(false));

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToCollections(collection1, collection3)
				.addToGroupsInCollections(asList("g1", "g2"), asList(collection1, collection3)));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection3)
				.addToGroupsInCollections(asList("g1", "g2"), asList(collection1, collection3)));

		//No problem since g1 is created locally
		services.execute("embalmer", req -> req
				.removeFromGroupOfCollection("g1", collection1).removeFromGroupOfCollection("g1", collection3));
		services.execute("machoman", req -> req
				.removeFromGroupOfCollection("g1", collection1).removeFromGroupOfCollection("g1", collection3));

		assertThatUser("embalmer")
				.hasGroupsInCollection(collection1, "g2")
				.hasGroupsInCollection(collection3, "g2")
				.hasSyncMode(SYNCED);

		assertThatUser("machoman")
				.hasGroupsInCollection(collection1, "g2")
				.hasGroupsInCollection(collection3, "g2")
				.hasSyncMode(SYNCED);

		assertThatException(() -> services.execute("embalmer", (req) -> req.addToGroupInCollection("g2", collection1))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup.class));

		assertThatException(() -> services.execute("machoman", (req) -> req.addToGroupInCollection("g2", collection3))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup.class));

		assertThatUser("embalmer")
				.hasGroupsInCollection(collection1, "g2")
				.hasGroupsInCollection(collection3, "g2")
				.hasSyncMode(SYNCED);

		assertThatUser("machoman")
				.hasGroupsInCollection(collection1, "g2")
				.hasGroupsInCollection(collection3, "g2")
				.hasSyncMode(SYNCED);

		services.execute("embalmer", (req) -> req.ldapSyncRequest().removeFromGroupOfCollection("g2", collection1)
				.removeFromGroupOfCollection("g2", collection3));

		assertThatUser("embalmer")
				.hasNoGroupsInCollection(collection1)
				.hasNoGroupsInCollection(collection3)
				.hasSyncMode(SYNCED);


		services.execute("machoman", (req) -> req.stopSyncingLDAP().removeFromGroupOfCollection("g2", collection1)
				.removeFromGroupOfCollection("g2", collection3));

		assertThatUser("machoman")
				.hasNoGroupsInCollection(collection1)
				.hasNoGroupsInCollection(collection3)
				.hasSyncMode(NOT_SYNCED);

	}


	@Test
	public void givenSyncedUsersWhenRemovingFromSyncedCollectionWithoutSyncFlagThenException() {

		//need to setup synConfig before

		LDAPUserSyncConfiguration ldapUserSyncConfiguration = new LDAPUserSyncConfiguration( admin, "",
				new RegexFilter("",""), new RegexFilter("",""), new Duration(1,1),
				asList("1 0 1"),
				asList(""), asList(""),
				asList(""),false,
				asList(collection1,collection2));

		when(ldapConfigurationManagerMock.getLDAPUserSyncConfiguration(anyBoolean())).thenReturn(ldapUserSyncConfiguration);

		services = new UserServices(getModelLayerFactory(), ldapConfigurationManagerMock);

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection2));

		//No problem since g1 is created locally
		services.execute("embalmer", req -> req
				.addToCollection(collection3));
		services.execute("machoman", req -> req
				.addToCollection(collection3));

		assertThatUser("embalmer").isInCollections(collection1, collection2, collection3).hasSyncMode(SYNCED);
		assertThatUser("machoman").isInCollections(collection1, collection2, collection3).hasSyncMode(SYNCED);

		//No problem since g1 is created locally
		services.execute("embalmer", req -> req
				.removeFromCollection(collection3));
		services.execute("machoman", req -> req
				.removeFromCollection(collection3));

		assertThatUser("embalmer").isInCollections(collection1, collection2).hasSyncMode(SYNCED);
		assertThatUser("machoman").isInCollections(collection1, collection2).hasSyncMode(SYNCED);


		assertThatException(() -> services.execute("embalmer", (req) -> req.removeFromCollection(collection1))
		).is(instanceOf(UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection.class));

		assertThatUser("embalmer").isInCollections(collection1, collection2).hasSyncMode(SYNCED);
		assertThatUser("machoman").isInCollections(collection1, collection2).hasSyncMode(SYNCED);

		services.execute("embalmer", (req) -> req.ldapSyncRequest().removeFromCollections(collection1));

		assertThatUser("embalmer")
				.isInCollections(collection2)
				.hasSyncMode(SYNCED);

		services.execute("machoman", (req) -> req.stopSyncingLDAP().removeFromCollections(collection2));

		assertThatUser("machoman")
				.isInCollections(collection1)
				.hasSyncMode(NOT_SYNCED);

	}

	@Test
	public void givenSyncedUsersWhenDeletingWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection2));

		assertThatUser("embalmer").isInCollections(collection1, collection2).hasSyncMode(SYNCED);
		assertThatUser("machoman").isInCollections(collection1, collection2).hasSyncMode(SYNCED);


		assertThatException(() -> services.execute("embalmer", (req) -> req.removeFromAllCollections())
		).is(instanceOf(UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection.class));

		assertThatUser("embalmer").isInCollections(collection1, collection2).hasSyncMode(SYNCED);
		assertThatUser("machoman").isInCollections(collection1, collection2).hasSyncMode(SYNCED);

		services.execute("embalmer", (req) -> req.ldapSyncRequest().removeFromAllCollections());

		assertThatUser("embalmer").doesNotExist();

		//Would be recreated during next sync
		assertThatException(() -> services.execute("machoman", (req) -> req.stopSyncingLDAP().removeFromAllCollections())
		).is(instanceOf(UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection.class));

		assertThatUser("machoman").isInCollections(collection1, collection2).hasSyncMode(SYNCED);

	}

	//test 1
	@Test
	public void whenRemovingSyncedUserFromSyncedGroupWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createGroup("g1", req->req.ldapSyncRequest().setName("The group").addCollections(collection1, collection2));

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToGroupInCollection("g1", collection1)
				.addToGroupInCollection("g1", collection2)
				.addToCollections(collection1, collection2));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToGroupInCollection("g1", collection1)
				.addToGroupInCollection("g1", collection2)
				.addToCollections(collection1, collection2));

		assertThatException(() -> services.execute("embalmer", req->req.removeFromGroupOfCollection("g1", collection1))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup.class));
		assertThatUser("embalmer").hasGroupsInCollection(collection1, "g1").hasGroupsInCollection(collection2, "g1");
		assertThatUser("machoman").hasGroupsInCollection(collection1, "g1").hasGroupsInCollection(collection2, "g1");

		services.execute("embalmer", req->req.stopSyncingLDAP()
				.removeFromGroupOfCollection("g1", collection1));

		services.execute("machoman", req->req.ldapSyncRequest()
				.removeFromGroupOfCollection("g1", collection1).removeFromGroupOfCollection("g1", collection2));

		assertThatUser("embalmer")
				.hasNoGroupsInCollection(collection1)
				.hasGroupsInCollection(collection2, "g1")
				.hasSyncMode(NOT_SYNCED);
		assertThatUser("machoman")
				.hasNoGroupsInCollection(collection1)
				.hasNoGroupsInCollection(collection2)
				.hasSyncMode(SYNCED);;

	}

	//test2
	@Test
	public void whenModifyingStatusOfSyncedUserWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createGroup("g1", req->req.ldapSyncRequest().setName("The group").addCollections(collection1, collection2));

		services.createUser("embalmer", (req) -> req.ldapSyncRequest()
				.setNameEmail("William Alvin", "Moody", "bill@constellio.com")
				.addToCollections(collection1, collection2));

		services.createUser("machoman", (req) -> req.ldapSyncRequest()
				.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addToCollections(collection1, collection2));

		assertThatException(() -> services.execute("embalmer", req->req.setStatusForCollection(PENDING, collection1))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeStatusOfSyncedUser.class));
		assertThatUser("embalmer").hasStatusIn(ACTIVE, collection1).hasStatusIn(ACTIVE, collection2);
		assertThatUser("machoman").hasStatusIn(ACTIVE, collection1).hasStatusIn(ACTIVE, collection2);

		services.execute("embalmer", req->req.stopSyncingLDAP()
				.setStatusForCollection(PENDING, collection1));

		services.execute("machoman", req->req.ldapSyncRequest()
				.setStatusForCollection(PENDING, collection1));

		assertThatUser("embalmer")
				.hasStatusIn(PENDING, collection1)
				.hasStatusIn(ACTIVE, collection2)
				.hasSyncMode(NOT_SYNCED);
		assertThatUser("machoman")
				.hasStatusIn(PENDING, collection1)
				.hasStatusIn(ACTIVE, collection2)
				.hasSyncMode(SYNCED);


	}

	//test3
	@Test
	public void whenRemovingSyncedGroupOfSyncedCollectionsWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createGroup("g1", req->req.ldapSyncRequest().setName("The group")
				.addCollections(collection1, collection2, collection3));

		services.executeGroupRequest("g1", req->req.removeCollection(collection3));
		assertThatGroup("g1").isActiveInAllItsCollections().isInCollections(collection1, collection2);

		assertThatException(() -> services.executeGroupRequest("g1", req->req.removeCollections(collection1, collection2))
		).is(instanceOf(UserServicesRuntimeException_CannotRemoveSyncedGroupFromSyncedCollection.class));

		services.executeGroupRequest("g1", req->req.ldapSyncRequest().removeCollections(collection1, collection2));
		assertThatGroup("g1").doesNotExist();
	}

	//test4
	@Test
	public void whenModifyingStatusOfSyncedGroupOfSyncedCollectionsWithoutSyncFlagThenException() {

		activateLDAPSyncOnCollections(collection1, collection2);

		services.createGroup("g1", req->req.ldapSyncRequest().setName("The group")
				.addCollections(collection1, collection2, collection3));

		assertThatException(() -> services.executeGroupRequest("g1", req->req.setStatusInAllCollections(GlobalGroupStatus.INACTIVE))
		).is(instanceOf(UserServicesRuntimeException_CannotChangeStatusOfSyncedGroup.class));
		assertThatGroup("g1").isActiveInAllItsCollections().isInCollections(collection1, collection2);

		services.executeGroupRequest("g1", req->req.ldapSyncRequest().setStatusInAllCollections(GlobalGroupStatus.INACTIVE));
		assertThatGroup("g1").isInactiveInAllItsCollections().isInCollections(collection1, collection2);
	}


	// --------- Utils ---------

	private void activateLDAPSyncOnCollections(String... collections) {
		LDAPServerConfiguration ldapServerConfiguration = new LDAPServerConfiguration(Collections.emptyList(),
				Collections.emptyList(), LDAPDirectoryType.ACTIVE_DIRECTORY, true, true);

		LDAPUserSyncConfiguration ldapUserSyncConfiguration = new LDAPUserSyncConfiguration(
				getUser(),
				getPassword(),
				getUserFiler(),
				getGroupFiler(),
				null,
				getScheduleTimeList(),
				getGroupBaseContextList(),
				getUsersWithoutGroupsBaseContextList(),
				getUserFilterGroupsList(),
				isMembershipAutomaticDerivationActivated(),
				Arrays.asList(collections));

		getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(
				ldapServerConfiguration, ldapUserSyncConfiguration, false);

	}


	private GroupAssertions assertThatGroup(String groupCode) {
		return new GroupAssertions(groupCode);
	}

	@AllArgsConstructor
	private class GroupAssertions {

		String groupCode;

		GroupAssertions isInCollections(String... expectedCollectionsArray) {
			List<String> expectedCollections = asList(expectedCollectionsArray);

			SystemWideGroup systemWideGroup = groupInfo(groupCode);
			assertThat(systemWideGroup).isNotNull();
			assertThat(systemWideGroup.getCollections()).containsOnly(expectedCollectionsArray);

			for (String collection : asList(collection1, collection2, collection3)) {
				boolean expectedInThisCollection = expectedCollections.contains(collection);
				if (expectedInThisCollection) {
					assertThat(group(groupCode, collection)).describedAs("Group '" + groupCode + "' is expected in collection '" + collection + "'").isNotNull();
				} else {
					assertThat(group(groupCode, collection)).describedAs("Group '" + groupCode + "' is not expected in collection '" + collection + "'").isNull();
				}
			}
			return this;
		}

		public GroupAssertions doesNotExist() {
			SystemWideGroup systemWideGroup = groupInfo(groupCode);
			assertThat(systemWideGroup).isNull();

			for (String collection : asList(collection1, collection2, collection3)) {
				assertThat(group(groupCode, collection)).describedAs("Group '" + groupCode + "' is not expected in collection '" + collection + "'").isNull();
			}
			return this;
		}

		public GroupAssertions isLocallyCreated() {
			assertThat(groupInfo(groupCode).isLocallyCreated()).describedAs("locally created status").isEqualTo(true);
			return this;
		}

		public GroupAssertions isNotLocallyCreated() {
			assertThat(groupInfo(groupCode).isLocallyCreated()).describedAs("locally created status").isEqualTo(false);
			return this;
		}

		private GroupAssertions isActiveInAllItsCollections() {
			SystemWideGroup group = groupInfo(groupCode);
			return hasStatusIn(GlobalGroupStatus.ACTIVE, group.getCollections());
		}

		private GroupAssertions isInactiveInAllItsCollections() {
			SystemWideGroup group = groupInfo(groupCode);
			return hasStatusIn(INACTIVE, group.getCollections());
		}


		private GroupAssertions isOnlyActiveIn(String... collections) {
			SystemWideGroup group = groupInfo(groupCode);
			hasStatusIn(GlobalGroupStatus.ACTIVE, Arrays.asList(collections));

			List<String> inactiveCollections = new ArrayList<>(group.getCollections());
			inactiveCollections.removeAll(asList(collections));

			if (!inactiveCollections.isEmpty()) {
				hasStatusIn(INACTIVE, inactiveCollections);
			}


			return this;
		}

		private GroupAssertions hasStatusIn(GlobalGroupStatus expectedStatus, List<String> collections) {
			for (String collection : collections) {
				assertThat(group(groupCode, collection).getStatus()).isEqualTo(expectedStatus);
				boolean expectedLogicallyDeletedStatus = expectedStatus != GlobalGroupStatus.ACTIVE;
				assertThat(group(groupCode, collection).isLogicallyDeletedStatus()).isEqualTo(expectedLogicallyDeletedStatus);
			}
			return this;
		}

		public GroupAssertions hasCaption(String expectedCaption) {
			SystemWideGroup group = groupInfo(groupCode);
			assertThat(group.getCaption()).isEqualTo(expectedCaption);
			for (String collection : group.getCollections()) {
				assertThat(group(groupCode, collection).getCaption()).isEqualTo(expectedCaption);
			}
			return this;
		}

		public GroupAssertions hasName(String expectedName) {
			SystemWideGroup group = groupInfo(groupCode);
			assertThat(group.getName()).isEqualTo(expectedName);
			for (String collection : group.getCollections()) {
				assertThat(group(groupCode, collection).getTitle()).isEqualTo(expectedName);
			}
			return this;
		}

		public GroupAssertions hasCode(String expectedCode) {
			SystemWideGroup group = groupInfo(groupCode);
			assertThat(group.getCode()).isEqualTo(expectedCode);
			for (String collection : group.getCollections()) {
				assertThat(group(groupCode, collection).getCode()).isEqualTo(expectedCode);
			}
			return this;
		}

		public GroupAssertions hasTitle(String expectedTitle) {
			SystemWideGroup group = groupInfo(groupCode);
			assertThat(group.getName()).isEqualTo(expectedTitle);
			for (String collection : group.getCollections()) {
				assertThat(group(groupCode, collection).getTitle()).isEqualTo(expectedTitle);
			}
			return this;
		}
	}


	private UserAssertions assertThatUser(String username) {
		return new UserAssertions(username);
	}

	@AllArgsConstructor
	private class UserAssertions {

		String username;

		UserAssertions isInCollections(String... expectedCollectionsArray) {
			List<String> expectedCollections = asList(expectedCollectionsArray);

			assertThat(userCredential(username)).isNotNull();

			SystemWideUserInfos systemWideUser = userInfos(username);
			assertThat(systemWideUser).isNotNull();
			assertThat(systemWideUser.getCollections()).containsOnly(expectedCollectionsArray);

			for (String collection : asList(collection1, collection2, collection3)) {
				boolean expectedInThisCollection = expectedCollections.contains(collection);
				if (expectedInThisCollection) {
					assertThat(user(username, collection)).describedAs("User '" + username + "' is expected in collection '" + collection + "'").isNotNull();
				} else {
					assertThat(user(username, collection)).describedAs("User '" + username + "' is not expected in collection '" + collection + "'").isNull();
				}
			}

			return this;
		}

		public UserAssertions doesNotExist() {
			assertThat(userInfos(username)).isNull();
			assertThat(userCredential(username)).isNull();
			assertThat(user(username, collection1)).isNull();
			assertThat(user(username, collection2)).isNull();
			assertThat(user(username, collection3)).isNull();

			return this;
		}

		private UserAssertions hasStatusIn(UserCredentialStatus expectedStatus, String collection) {
			User user = user(username, collection);
			assertThat(user).describedAs("Expecting user '" + username + "' to exist in collection '" + collection + "', but it does not").isNotNull();
			assertThat(userInfos(user.getUsername()).getStatus(user.getCollection()))
					.describedAs("Status in collection '" + collection + "'").isEqualTo(expectedStatus);
			assertThat(user.getStatus()).isEqualTo(expectedStatus);
			boolean expectedLogicallyDeletedStatus = expectedStatus != ACTIVE;
			assertThat(user.isLogicallyDeletedStatus()).isEqualTo(expectedLogicallyDeletedStatus);

			return this;
		}

		private UserAssertions isPhysicicallyDeletedIn(String collection) {
			assertThat(userInfos(username).getStatus(collection)).isNull();
			assertThat(userInfos(username).getCollections()).doesNotContain(collection);
			assertThat(user(username, collection)).isNull();

			return this;
		}

		private UserAssertions hasSyncMode(UserSyncMode mode) {
			assertThat(userInfos(username).getSyncMode()).isSameAs(mode);

			return this;
		}

		private UserAssertions hasNoGroupsInCollection(String collection) {
			return hasGroupsInCollection(collection, new String[]{});
		}

		private UserAssertions hasGroupsInCollection(String collection, String... groupCodes) {
			String[] groupIds = new String[groupCodes.length];
			for (int i = 0; i < groupCodes.length; i++) {
				groupIds[i] = group(groupCodes[i], collection).getId();
			}

			assertThat(user(username, collection).getUserGroups()).containsOnly(groupIds);

			return this;
		}

		public UserAssertions hasName(String firstName, String lastName) {
			SystemWideUserInfos userInfos = services.getUserInfos(username);
			assertThat(userInfos.getFirstName()).isEqualTo(firstName);
			assertThat(userInfos.getLastName()).isEqualTo(lastName);

			assertThat(userCredential(username).getFirstName()).isEqualTo(firstName);
			assertThat(userCredential(username).getLastName()).isEqualTo(lastName);

			for (String collection : userInfos.getCollections()) {
				assertThat(user(username, collection).getFirstName()).isEqualTo(firstName);
				assertThat(user(username, collection).getLastName()).isEqualTo(lastName);
			}


			return this;
		}

		public UserAssertions hasEmail(String email) {
			SystemWideUserInfos userInfos = services.getUserInfos(username);
			assertThat(userInfos.getEmail()).isEqualTo(email);

			assertThat(userCredential(username).getEmail()).isEqualTo(email);

			for (String collection : userInfos.getCollections()) {
				assertThat(user(username, collection).getEmail()).isEqualTo(email);
			}


			return this;
		}
	}

	private UserAndGroupsAddUpdateResponseAssertions assertThatResponse(UserAndGroupsAddUpdateResponse response) {
		return new UserAndGroupsAddUpdateResponseAssertions(response);
	}

	@AllArgsConstructor
	private class UserAndGroupsAddUpdateResponseAssertions {
		UserAndGroupsAddUpdateResponse response;

		public UserAndGroupsAddUpdateResponseAssertions containsGroupInCollection(String groupCode, String collection) {
			List<UserAndGroupsResponseError> errors = response.getErrors();
			Boolean containsCodeAndCollection = false;

			for (UserAndGroupsResponseError error : errors) {
				if (error.getParameters().get("groupCode").equals(groupCode) && error.getParameters().get("collection").equals(collection)) {
					containsCodeAndCollection = true;
				}
			}
			assertThat(containsCodeAndCollection).isTrue();

			return this;
		}

		public UserAndGroupsAddUpdateResponseAssertions hasExactlyErrorCodes(String... errorCodes) {
			List<UserAndGroupsResponseError> errors = response.getErrors();
			List<String> responseErrorCodes = new ArrayList<>();
			errors.forEach(error -> {
				responseErrorCodes.add(error.getCode());
			});
			assertThat(errorCodes).containsExactly(errorCodes);

			return this;
		}
	}

	private void givenUserCreatedUserFoldersInCollection(String username, String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, getModelLayerFactory());
		User user = user(username, collection);
		try {
			recordServices.add(schemas.newCapsule().setCode("capsule").setTitle("Capsule").setCreatedBy(user));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void givenUserCreatedRecordsInCollection(String username, String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, getModelLayerFactory());
		User user = user(username, collection);
		UserFolder userFolder = schemas.newUserFolder().setUser(user).setTitle("My folder!");
		try {
			recordServices.add(userFolder);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private SystemWideUserInfos userInfos(String username) {
		try {
			return services.getUserInfos(username);
		} catch (UserServicesRuntimeException_NoSuchUser e) {
			return null;
		}
	}

	private UserCredential userCredential(String username) {
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		Record userCredentialRecord = searchServices.searchSingleResult(from(systemSchemas.credentialSchemaType())
				.where(systemSchemas.credentialUsername()).isEqualTo(username));
		return userCredentialRecord == null ? null : systemSchemas.wrapUserCredential(userCredentialRecord);
	}


	private User user(String username, String collection) {
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, getModelLayerFactory());
		Record userRecord = searchServices.searchSingleResult(from(schemas.user.schemaType())
				.where(schemas.user.username()).isEqualTo(username));
		return userRecord == null ? null : schemas.wrapUser(userRecord);
	}

	private Group group(String code, String collection) {
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, getModelLayerFactory());
		Record groupRecord = searchServices.searchSingleResult(from(schemas.group.schemaType())
				.where(schemas.group.code()).isEqualTo(code));
		return groupRecord == null ? null : schemas.wrapGroup(groupRecord);
	}

	private SystemWideGroup groupInfo(String code) {
		return services.getNullableGroup(code);
	}


	private void createAuthorisationGivingAccessToGroupInCollection(String groupCode, String collection) {
		AdministrativeUnit administrativeUnit = new RMSchemasRecordsServices(collection, getAppLayerFactory()).getAdministrativeUnitWithCode("ze-unit");
		Group group = group(groupCode, collection);
		getModelLayerFactory().newAuthorizationsServices().add(AuthorizationAddRequest.authorizationForGroups(group).on(administrativeUnit).givingReadAccess());

	}

	private void createAuthorisationGivingAccessToUserInCollection(String username, String collection) {
		AdministrativeUnit administrativeUnit = new RMSchemasRecordsServices(collection, getAppLayerFactory()).getAdministrativeUnitWithCode("ze-unit");
		User user = user(username, collection);
		getModelLayerFactory().newAuthorizationsServices().add(AuthorizationAddRequest.authorizationForUsers(user).on(administrativeUnit).givingReadAccess());

	}
}
