package com.constellio.model.services.users;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_AtLeastOneCollectionRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_EmailRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_FirstNameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUsername;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_LastNameRequired;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import java.util.stream.Collectors;

import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static com.constellio.model.entities.security.global.UserCredentialStatus.DELETED;
import static com.constellio.model.entities.security.global.UserCredentialStatus.PENDING;
import static com.constellio.model.entities.security.global.UserCredentialStatus.SUSPENDED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatException;
import static org.assertj.core.api.Assertions.assertThat;

public class UserServicesRefactAcceptanceTest extends ConstellioTest {

	RecordServices recordServices;
	SearchServices searchServices;
	UserServices services;

	String collection1 = "collection1";
	String collection2 = "collection2";
	String collection3 = "collection3";

	SchemasRecordsServices collection1Schemas;
	SchemasRecordsServices collection2Schemas;
	SchemasRecordsServices collection3Schemas;
	SchemasRecordsServices systemSchemas;

	@Before
	public void setup() {
		prepareSystem(withCollection("collection1"), withCollection("collection2"), withCollection("collection3"));
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		services = getModelLayerFactory().newUserServices();

		collection1Schemas = new SchemasRecordsServices("collection1", getModelLayerFactory());
		collection2Schemas = new SchemasRecordsServices("collection2", getModelLayerFactory());
		collection3Schemas = new SchemasRecordsServices("collection3", getModelLayerFactory());
		systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, getModelLayerFactory());
	}

	@Test
	public void whenCreatingUserThenValidateFieldsAndSaveTheUser() {

		assertThatException(() -> services.createUser("andre geant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addCollection("collection1"))
		).isInstanceOf(UserServicesRuntimeException_InvalidUsername.class);

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com"))
		).isInstanceOf(UserServicesRuntimeException_AtLeastOneCollectionRequired.class);

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.addCollection("collection1"))
		).isInstanceOf(UserServicesRuntimeException_EmailRequired.class);

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setLastName("Le géant")
				.setEmail("andre@constellio.com").addCollection("collection1"))
		).isInstanceOf(UserServicesRuntimeException_FirstNameRequired.class);

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setFirstName("André")
				.setEmail("andre@constellio.com").addCollection("collection1"))
		).isInstanceOf(UserServicesRuntimeException_LastNameRequired.class);

		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addCollection("inexistingCollection"))
		).isInstanceOf(UserServicesRuntimeException_InvalidCollection.class);


		assertThatException(() -> services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addCollection("collection1").addToGroupInEachCollection("inexistingGroup"))
		).isInstanceOf(UserServicesRuntimeException_InvalidGroup.class);

		services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addCollections("collection1", "collection2"));

		SystemWideUserInfos userInfos = services.getUserInfos("andregeant");
		assertThat(userInfos.getUsername()).isEqualTo("andregeant");
		assertThat(userInfos.getFirstName()).isEqualTo("André");
		assertThat(userInfos.getLastName()).isEqualTo("Le géant");
		assertThat(userInfos.getEmail()).isEqualTo("andre@constellio.com");
		assertThat(userInfos.getCollections()).containsOnly("collection1", "collection2");
		assertThat(userInfos.getGlobalGroups()).isEmpty();

		assertThat(userCredential("andregeant")).isNotNull();
		assertThat(user("andregeant", collection1)).isNotNull();
		assertThat(user("andregeant", collection2)).isNotNull();
		assertThat(user("andregeant", collection3)).isNull();
	}

	@Test
	public void givenExistingUserThenCanModifyInfos() {
		services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addCollections("collection1", "collection2"));

		//Possible to modify infos without passing name, email, etc.
		services.execute("andregeant", (req) -> req.setDn("andre"));
		assertThat(userInfos("andregeant").getDn()).isEqualTo("andre");

		//Also possible to change name and email
		services.execute("andregeant", (req) -> req.setName("Géant", "Ferré"));
		assertThat(userInfos("andregeant").getFirstName()).isEqualTo("Géant");
		assertThat(userInfos("andregeant").getLastName()).isEqualTo("Ferré");


		//TODO Philippe : Tester toutes les métadonnées (ex. jobTitle, phone, etc.) en ajout/modification
	}


	@Test
	public void givenUsersInDifferentCollectionsWhenStreamingUsersThenAllReturnedWihoutDuplicates() {
		services.createUser("andregeant", (req) -> req.setName("André", "Le géant")
				.setEmail("andre@constellio.com").addCollections("collection2", "collection3"));

		services.createUser("kane", (req) -> req.setName("Fake", "Diesel")
				.setEmail("kane@constellio.com").addCollections("collection2"));

		services.createUser("machoman", (req) -> req.setName("Macho", "Man")
				.setEmail("machoman@constellio.com").addCollections("collection1", "collection3"));

		services.createUser("sting", (req) -> req.setName("Blade Runner", "Sting")
				.setEmail("sting@constellio.com").addCollections("collection1", "collection2", "collection3"));

		assertThat(services.streamUserInfos().map((SystemWideUserInfos::getUsername)).collect(Collectors.toList()))
				.containsExactly("andregeant", "kane", "machoman", "sting");

		assertThat(services.streamUserInfos().findFirst().get().getCollections()).containsOnly("collection2", "collection3");

		assertThat(services.streamUserInfos(collection1).map((SystemWideUserInfos::getUsername)).collect(Collectors.toList()))
				.containsExactly("machoman", "sting");

		assertThat(services.streamUserInfos(collection2).map((SystemWideUserInfos::getUsername)).collect(Collectors.toList()))
				.containsExactly("andregeant", "kane", "sting");

		assertThat(services.streamUserInfos(collection3).map((SystemWideUserInfos::getUsername)).collect(Collectors.toList()))
				.containsExactly("andregeant", "machoman", "sting");

		assertThat(services.streamUser(collection1).map((User::getUsername)).collect(Collectors.toList()))
				.containsExactly("machoman", "sting");

		assertThat(services.streamUser(collection2).map((User::getUsername)).collect(Collectors.toList()))
				.containsExactly("andregeant", "kane", "sting");

		assertThat(services.streamUser(collection3).map((User::getUsername)).collect(Collectors.toList()))
				.containsExactly("andregeant", "machoman", "sting");

	}

	@Test
	public void whenDeletingUsersThenDeleteThemWhereverPossible() {

		services.createUser("rey", (req) -> req.setNameEmail("Rey", "Mysterio", "rey.mysterio@constellio.com")
				.addCollections("collection1", "collection2"));

		services.createUser("ric", (req) -> req.setNameEmail("Ric", "Flair", "ric.flair@constellio.com")
				.addCollections("collection1", "collection2"));
		givenUserCreatedRecordsInCollection("ric", "collection2");

		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addCollections("collection1", "collection2"));
		givenUserCreatedUserFoldersInCollection("embalmer", "collection1");

		services.createUser("randy", (req) -> req.setNameEmail("Randy", "Orton", "randy.orton@constellio.com")
				.addCollections("collection1", "collection2"));
		givenUserCreatedRecordsInCollection("randy", "collection1");

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addCollections("collection1", "collection2"));
		givenUserCreatedUserFoldersInCollection("undertaker", "collection2");

		services.execute("rey", (req) -> req.markForDeletionInAllCollections());
		services.execute("ric", (req) -> req.markForDeletionInAllCollections());
		services.execute("embalmer", (req) -> req.markForDeletionInAllCollections());
		services.execute("randy", (req) -> req.markForDeletionInAllCollection("collection1"));
		services.execute("undertaker", (req) -> req.markForDeletionInAllCollection("collection1"));

		assertThatUserIsPhysicicallyDeletedInAllCollections("ric");

		assertThatUserIsPhysicicallyDeletedIn("ric", "collection1");
		assertThatUserAsStatusIn("ric", "collection2", DELETED);

		assertThatUserAsStatusIn("embalmer", "collection1", DELETED);
		assertThatUserIsPhysicicallyDeletedIn("embalmer", "collection2");

		assertThatUserAsStatusIn("randy", "collection1", DELETED);
		assertThatUserAsStatusIn("randy", "collection2", ACTIVE);

		assertThatUserIsPhysicicallyDeletedIn("undertaker", "collection1");
		assertThatUserAsStatusIn("undertaker", "collection2", ACTIVE);

	}

	@Test
	public void whenChangingStatusThenAppliedToSpecifiedCollectionsAndUpdateLogicallyDeletedMetadata() {

		services.createUser("randy", (req) -> req.setNameEmail("Randy", "Orton", "randy.orton@constellio.com")
				.addCollections("collection1", "collection2"));

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addCollections("collection1", "collection2"));

		services.createUser("shawn", (req) -> req.setNameEmail("Shawn", "Michaels", "shawn.michaels@constellio.com")
				.addCollections("collection1", "collection2"));

		services.createUser("rey", (req) -> req.setNameEmail("Rey", "Mysterio", "rey.mysterio@constellio.com")
				.addCollections("collection1", "collection2"));


		services.execute("randy", (req) -> req.setStatusForAllCollections(SUSPENDED));

		services.execute("undertaker", (req) -> req.setStatusForCollection(PENDING, "collection2"));

		services.execute("shawn", (req) -> req.setStatusForCollection(DELETED, "collection1"));

		services.execute("rey", (req) -> req.setStatusForCollection(PENDING, "collection1")
				.setStatusForCollection(SUSPENDED, "collection2"));

		assertThatUserAsStatusIn("undertaker", "collection1", SUSPENDED);
		assertThatUserAsStatusIn("undertaker", "collection2", SUSPENDED);

		assertThatUserAsStatusIn("randy", "collection1", ACTIVE);
		assertThatUserAsStatusIn("randy", "collection2", PENDING);

		assertThatUserAsStatusIn("shawn", "collection1", DELETED);
		assertThatUserAsStatusIn("shawn", "collection2", ACTIVE);

		assertThatUserAsStatusIn("rey", "collection1", PENDING);
		assertThatUserAsStatusIn("rey", "collection2", SUSPENDED);

		assertThat(userInfos("undertaker").hasStatusInAllCollection(SUSPENDED)).isTrue();
		assertThat(userInfos("undertaker").hasStatusInAnyCollection(SUSPENDED)).isTrue();
		assertThat(userInfos("undertaker").hasStatusInAllCollection(PENDING)).isFalse();
		assertThat(userInfos("undertaker").hasStatusInAnyCollection(PENDING)).isFalse();

		assertThat(userInfos("rey").hasStatusInAllCollection(PENDING)).isFalse();
		assertThat(userInfos("rey").hasStatusInAnyCollection(PENDING)).isTrue();
		assertThat(userInfos("rey").hasStatusInAllCollection(SUSPENDED)).isFalse();
		assertThat(userInfos("rey").hasStatusInAnyCollection(SUSPENDED)).isTrue();

		services.execute("rey", (req) -> req.setStatusForAllCollections(ACTIVE));

		assertThatUserAsStatusIn("rey", "collection1", ACTIVE);
		assertThatUserAsStatusIn("rey", "collection2", ACTIVE);
	}

	@Test
	public void whenAddingAndRemovingCollectionsThenApplied() {
		services.createUser("embalmer", (req) -> req.setNameEmail("Paul", "Bearer", "embalmer@constellio.com")
				.addCollections("collection1", "collection2"));
		givenUserCreatedUserFoldersInCollection("embalmer", "collection2");

		services.createUser("undertaker", (req) -> req.setNameEmail("The", "Undertaker", "the_undertaker@constellio.com")
				.addCollections("collection1", "collection2"));
		givenUserCreatedRecordsInCollection("undertaker", "collection2");

		services.createUser("machoman", (req) -> req.setNameEmail("Macho", "Man", "machoman@constellio.com")
				.addCollections("collection1", "collection3"));

		services.execute("embalmer", (req) -> req.addCollection("collection3").removeCollection("collection2"));
		services.execute("undertaker", (req) -> req.addCollection("collection3").removeCollection("collection2"));
		services.execute("machoman", (req) -> req.addCollection("collection3").removeCollection("collection2"));


		assertThatUserAsStatusIn("embalmer", "collection1", ACTIVE);
		assertThatUserAsStatusIn("embalmer", "collection2", DELETED);
		assertThatUserAsStatusIn("embalmer", "collection3", ACTIVE);

		assertThatUserAsStatusIn("undertaker", "collection1", ACTIVE);
		assertThatUserAsStatusIn("undertaker", "collection2", DELETED);
		assertThatUserAsStatusIn("undertaker", "collection3", ACTIVE);

		assertThatUserAsStatusIn("machoman", "collection1", ACTIVE);
		assertThatUserIsPhysicicallyDeletedIn("machoman", "collection2");
		assertThatUserAsStatusIn("machoman", "collection3", ACTIVE);


	}

	@Test
	public void whenAddingGroupsThenSaved() {
		//TODO
	}

	// --------- Utils ---------

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
		return services.getUserInfos(username);
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

	private void assertThatUserAsStatusIn(String username, String collection, UserCredentialStatus expectedStatus) {
		User user = user(username, collection);
		assertThat(userInfos(user.getUsername()).getStatus(user.getCollection())).isEqualTo(expectedStatus);
		assertThat(user.getStatus()).isEqualTo(expectedStatus);
		boolean expectedLogicallyDeletedStatus = expectedStatus != ACTIVE;
		assertThat(user.isLogicallyDeletedStatus()).isEqualTo(expectedLogicallyDeletedStatus);
	}

	private void assertThatUserIsPhysicicallyDeletedIn(String username, String collection) {
		assertThat(userInfos(username).getStatus(collection)).isNull();
		assertThat(userInfos(username).getCollections()).doesNotContain(collection);
		assertThat(user(username, collection)).isNull();
	}

	private void assertThatUserIsPhysicicallyDeletedInAllCollections(String username) {
		assertThat(userInfos(username)).isNull();
		assertThat(userCredential(username)).isNull();
		assertThat(user(username, collection1)).isNull();
		assertThat(user(username, collection2)).isNull();
		assertThat(user(username, collection3)).isNull();

	}
}
