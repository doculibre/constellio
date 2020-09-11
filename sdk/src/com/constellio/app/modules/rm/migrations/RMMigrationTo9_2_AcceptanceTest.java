package com.constellio.app.modules.rm.migrations;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import lombok.AllArgsConstructor;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static com.constellio.model.entities.security.global.UserCredentialStatus.DISABLED;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class RMMigrationTo9_2_AcceptanceTest extends ConstellioTest {

	@Test
	public void givenSystemIn9_0WValidateMigrationOf9_0And9_2ThenOk() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9.0.3.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsResetAndDisableLDAPSync()
				.withFakeEncryptionServices();

		List<String> collections = getModelLayerFactory().getCollectionsListManager().getCollections();
		List<User> users = new ArrayList<>();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		for (String collection :
				collections) {
			users.addAll(getAllUsersInCollection(modelLayerFactory, collection));
		}
		List<UserCredential> credentials = getUserCredentials(modelLayerFactory);

		List<Group> groups = new ArrayList<>();
		for (String collection :
				collections) {
			groups.addAll(getAllGroupsInCollection(modelLayerFactory, collection));
		}

		//Verify users

		//Check a certain user is not there? In LDAP case they are added to system collection?
		String collectiondef = collections.isEmpty() && collections.size() < 2 ? zeCollection : collections.get(1);

		MetadataSchema credentialMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION).getDefaultSchema(UserCredential.SCHEMA_TYPE);
		MetadataSchema userMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectiondef).getDefaultSchema(User.SCHEMA_TYPE);
		MetadataSchema groupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectiondef).getDefaultSchema(Group.SCHEMA_TYPE);
		MetadataSchema globalGroupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION).getDefaultSchema(GlobalGroup.SCHEMA_TYPE);

		//SyncMode est dans les utilisateurs?
		assertThat(credentialMetadataSchema.getMetadata(UserCredential.SYNC_MODE).getDataEntry().getType()).isEqualTo(MANUAL);
		for (UserCredential user : credentials) {
			assertThat(user.getSyncMode()).isNotNull();
		}

		//Metadonnées retirées?
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("firstname")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("lastname")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("email")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("personalEmails")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("phone")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("globalGroups")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("fax")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("jobTitle")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("address")).isFalse();

		//Métadonnées déplacés dans users?
		assertThat(userMetadataSchema.hasMetadataWithCode(User.DOMAIN)).isTrue();
		assertThat(userMetadataSchema.hasMetadataWithCode(User.MS_EXCHANGE_DELEGATE_LIST)).isTrue();

		//Les utilisateurs qui n'étaient pas actifs sont supprimés logiquement?
		//		List<UserCredential> credentialsDeleted = credentials.stream()
		//				.filter(x -> !x.getStatus().equals(UserCredentialStatus.ACTIVE))
		//				.collect(toList());
		//		for (UserCredential credential : credentialsDeleted) {
		//			assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);
		//		}

		//physically removed users if not used. Need to be encoded.

		//Verify Groups

		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("usersAutomaticallyAddedToCollections")).isFalse();
		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("status")).isFalse();
		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("locallyCreated")).isFalse();
		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("hierarchy")).isFalse();
		assertThat(groupMetadataSchema.hasMetadataWithCode("status")).isTrue();
		assertThat(groupMetadataSchema.hasMetadataWithCode("locallyCreated")).isTrue();
		assertThat(groupMetadataSchema.hasMetadataWithCode("hierarchy")).isTrue();

		verifySaveState(groups, users, credentials);
		verifyData(groups, users, credentials);

	}

	private void verifyData(List<Group> groups, List<User> users, List<UserCredential> credentials) {
		List<Group> nobility = groups.stream().filter(x -> x.getCode().equals("nobility")
														   && !x.getCollection().equals(Collection.SYSTEM_COLLECTION)).collect(toList());

		assertThat(nobility).isNotEmpty().hasSize(2);


		List<String> collectionRidaGroups = groups.stream()
				.filter(x -> x.getCollection().equals("LaCollectionDeRida")).map(group -> group.getCode()).collect(toList());
		assertThat(collectionRidaGroups).isNotEmpty().contains("explorers", "Bosses", "royale");

		//dusty (edouard's rival) is part of big bad bosses
		assertThatUser("dusty")
				.hasStatusIn(DISABLED, businessCollection)
				.hasName("dusty", "le chien")
				.hasEmail("dusty@doculibre.com")
				.hasGroupsInCollection(businessCollection, "Bosses");

		//All global groups are gone
		assertThat(getAllGlobalGroups(getModelLayerFactory())).isEmpty();

		Group legends = groups.stream().filter(x -> x.getCode().equals("legends")
													&& !x.getCollection().equals(Collection.SYSTEM_COLLECTION)).findFirst().get();
		Group villains = groups.stream().filter(x -> x.getCode().equals("villains")
													 && !x.getCollection().equals(Collection.SYSTEM_COLLECTION)).findFirst().get();
		assertThat(nobility.get(0).getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(legends.getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(villains.getStatus()).isEqualTo(GlobalGroupStatus.INACTIVE);
		assertThat(legends.isLocallyCreated()).isTrue();
		assertThat(nobility.get(0).isLocallyCreated()).isTrue();
		assertThat(villains.isLocallyCreated()).isTrue();

	}

	private void verifySaveState(List<Group> groups, List<User> users, List<UserCredential> credentials) {
		//utilisateurs actifs ayant utilisé le sytème sont présents
		User dusty = users.stream().filter(x -> x.getUsername().equals("dusty")).findFirst().get();
		User marie = users.stream().filter(x -> x.getUsername().equals("marie")).findFirst().get();
		User cartier = users.stream().filter(x -> x.getUsername().equals("cartie")).findFirst().get();
		//utilisateurs non-actifs ayant utilisé le sytème sont présents
		User colomb = users.stream().filter(x -> x.getUsername().equals("colomb")).findFirst().get();
		User elizabeth = users.stream().filter(x -> x.getUsername().equals("elizabeth")).findFirst().get();
		User naruto = users.stream().filter(x -> x.getUsername().equals("naruto")).findFirst().get();
		User queen = users.stream().filter(x -> x.getUsername().equals("queen")).findFirst().get();

		//Autres utilisateurs présents
		User sauron = users.stream().filter(x -> x.getUsername().equals("sauron")).findFirst().get();
		User admin = users.stream().filter(x -> x.getUsername().equals("admin")).findFirst().get();
		User bob = users.stream().filter(x -> x.getUsername().equals("bob")).findFirst().get();
		User chuck = users.stream().filter(x -> x.getUsername().equals("chuck")).findFirst().get();
		User dakota = users.stream().filter(x -> x.getUsername().equals("dakota")).findFirst().get();
		//User moe = users.stream().filter(x -> x.getUsername().equals("moe")).findFirst().get();

		//not suppose to exists
		// User Wanderer had no collections
		User wanderer = users.stream().filter(x -> x.getUsername().equals("wanderer")).findFirst().orElse(null);
		//Oscar was a pending user that never login or did anything
		User oscar = users.stream().filter(x -> x.getUsername().equals("oscar")).findFirst().orElse(null);
		//Louis 16 was a suspended user that never used the system
		User louis16 = users.stream().filter(x -> x.getUsername().equals("louis16")).findFirst().orElse(null);
		//Sasuke was a disabled user that never used the system
		User sasuke = users.stream().filter(x -> x.getUsername().equals("sasuke")).findFirst().orElse(null);

		assertThat(dusty).isNotNull();
		assertThat(marie).isNotNull();
		assertThat(cartier).isNotNull();
		assertThat(colomb).isNotNull();
		assertThat(elizabeth).isNotNull();
		assertThat(naruto).isNotNull();
		assertThat(queen).isNotNull();
		assertThat(sauron).isNotNull();
		assertThat(admin).isNotNull();
		assertThat(bob).isNotNull();
		assertThat(chuck).isNotNull();
		assertThat(dakota).isNotNull();
		//assertThat(moe).isNotNull();

		assertThat(wanderer).isNull();
		//assertThat(oscar).isNull();
		//assertThat(louis16).isNull();
		//assertThat(sasuke).isNull();

		Group rumors = groups.stream().filter(x -> x.getCode().equals("rumors")).findFirst().get();
		Group explorers = groups.stream().filter(x -> x.getCode().equals("explorers")).findFirst().get();
		Group heroes = groups.stream().filter(x -> x.getCode().equals("heroes")).findFirst().get();
		Group legends = groups.stream().filter(x -> x.getCode().equals("legends")).findFirst().get();
		Group nobility = groups.stream().filter(x -> x.getCode().equals("nobility")).findFirst().get();
		Group villains = groups.stream().filter(x -> x.getCode().equals("villains")).findFirst().get();
		Group england = groups.stream().filter(x -> x.getCode().equals("england")).findFirst().get();
		Group france = groups.stream().filter(x -> x.getCode().equals("france")).findFirst().get();
		Group royale = groups.stream().filter(x -> x.getCode().equals("royale")).findFirst().get();
		Group bosses = groups.stream().filter(x -> x.getCode().equals("bosses")).findFirst().orElse(null);
		Group sidekicks = groups.stream().filter(x -> x.getCode().equals("sidekicks")).findFirst().get();

		assertThat(rumors).isNotNull();
		assertThat(explorers).isNotNull();
		assertThat(heroes).isNotNull();
		assertThat(legends).isNotNull();
		assertThat(nobility).isNotNull();
		assertThat(villains).isNotNull();
		assertThat(england).isNotNull();
		assertThat(france).isNotNull();
		assertThat(royale).isNotNull();
		assertThat(sidekicks).isNotNull();
	}

	@Test
	public void givenSystemIn9_0WWithLDAPValidateMigrationOf9_0And9_2ThenOk() {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, "given_system_in_9_0_3_ldap.zip");

		getCurrentTestSession().getFactoriesTestFeatures()
				.givenSystemInState(state).withPasswordsResetAndDisableLDAPSync()
				.withFakeEncryptionServices();

		List<String> collections = getAppLayerFactory().getCollectionsManager().getCollectionCodes();
		List<User> users = new ArrayList<>();

		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		for (String collection :
				collections) {
			users.addAll(getAllUsersInCollection(modelLayerFactory, collection));
		}
		List<UserCredential> credentials = getUserCredentials(modelLayerFactory);

		List<Group> groups = new ArrayList<>();
		for (String collection :
				collections) {
			groups.addAll(getAllGroupsInCollection(modelLayerFactory, collection));
		}

		//Verify users

		//Check a certain user is not there? In LDAP case they are added to system collection?
		String collectiondef = collections.isEmpty() && collections.size() < 2 ? zeCollection : collections.get(1);

		MetadataSchema credentialMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION).getDefaultSchema(UserCredential.SCHEMA_TYPE);
		MetadataSchema userMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectiondef).getDefaultSchema(User.SCHEMA_TYPE);
		MetadataSchema groupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collectiondef).getDefaultSchema(Group.SCHEMA_TYPE);
		MetadataSchema globalGroupMetadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(Collection.SYSTEM_COLLECTION).getDefaultSchema(GlobalGroup.SCHEMA_TYPE);

		//SyncMode est dans les utilisateurs?
		assertThat(credentialMetadataSchema.getMetadata(UserCredential.SYNC_MODE).getDataEntry().getType()).isEqualTo(MANUAL);
		for (UserCredential user : credentials) {
			assertThat(user.getSyncMode()).isNotNull();
		}

		//Metadonnées retirées?
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("firstname")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("lastname")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("email")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("personalEmails")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("phone")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("globalGroups")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("fax")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("jobTitle")).isFalse();
		//		assertThat(credentialMetadataSchema.hasMetadataWithCode("address")).isFalse();

		//Métadonnées déplacés dans users?
		assertThat(userMetadataSchema.hasMetadataWithCode(User.DOMAIN)).isTrue();
		assertThat(userMetadataSchema.hasMetadataWithCode(User.MS_EXCHANGE_DELEGATE_LIST)).isTrue();

		//Les utilisateurs qui n'étaient pas actifs sont supprimés logiquement?
		//		List<UserCredential> credentialsDeleted = credentials.stream()
		//				.filter(x -> !x.getStatus().equals(UserCredentialStatus.ACTIVE))
		//				.collect(toList());
		//		for (UserCredential credential : credentialsDeleted) {
		//			assertThat(credential.getStatus()).isEqualTo(UserCredentialStatus.DISABLED);
		//		}

		//physically removed users if not used. Need to be encoded.

		//Verify Groups

		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("usersAutomaticallyAddedToCollections")).isFalse();
		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("status")).isFalse();
		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("locallyCreated")).isFalse();
		//		assertThat(globalGroupMetadataSchema.hasMetadataWithCode("hierarchy")).isFalse();
		assertThat(groupMetadataSchema.hasMetadataWithCode("status")).isTrue();
		assertThat(groupMetadataSchema.hasMetadataWithCode("locallyCreated")).isTrue();
		assertThat(groupMetadataSchema.hasMetadataWithCode("hierarchy")).isTrue();

		verifySaveStateLdap(groups, users, credentials);

	}

	private void verifySaveStateLdap(List<Group> groups, List<User> users, List<UserCredential> credentials) {
		//utilisateurs actifs ayant utilisé le sytème sont présents
		User kidd = users.stream().filter(x -> x.getUsername().equals("kidd")).findFirst().get();
		User edward = users.stream().filter(x -> x.getUsername().equals("blackbeard")).findFirst().get();
		User philippe = users.stream().filter(x -> x.getUsername().equals("philippe")).findFirst().get();

		assertThat(kidd).isNotNull();
		assertThat(edward).isNotNull();
		assertThat(philippe).isNotNull();

		Group pirates = groups.stream().filter(x -> x.getCode().equals("CN=pirates,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca")).findFirst().get();
		Group groupe1 = groups.stream().filter(x -> x.getCode().equals("CN=groupe1,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca")).findFirst().get();

		assertThat(pirates).isNotNull();
		assertThat(groupe1).isNotNull();
	}

	//helpers
	private List<GlobalGroup> getAllGlobalGroups(ModelLayerFactory modelLayerFactory) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		List<Record> groupeRecord = searchServices.search(new LogicalSearchQuery(
				from(systemSchemas.globalGroupSchemaType()).returnAll()));
		return groupeRecord == null ? null : systemSchemas.wrapOldGlobalGroups(groupeRecord);
	}

	private List<UserCredential> getUserCredentials(ModelLayerFactory modelLayerFactory) {
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(Collection.SYSTEM_COLLECTION, modelLayerFactory);

		List<Record> userCredentialRecord = searchServices.search(new LogicalSearchQuery(
				from(systemSchemas.credentialSchemaType()).returnAll()));
		return userCredentialRecord == null ? null : systemSchemas.wrapUserCredentials(userCredentialRecord);
	}

	private List<User> getAllUsersInCollection(ModelLayerFactory modelLayerFactory, String collection) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		List<Record> userRecord = searchServices.search(new LogicalSearchQuery(
				from(schemas.user.schemaType()).returnAll()));
		return userRecord == null ? null : schemas.wrapUsers(userRecord);
	}

	private List<Group> getAllGroupsInCollection(ModelLayerFactory modelLayerFactory, String collection) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		List<Record> groupRecord = searchServices.search(new LogicalSearchQuery(
				from(schemas.group.schemaType()).returnAll()));
		return groupRecord == null ? null : schemas.wrapGroups(groupRecord);
	}


	private RMMigrationTo9_2_AcceptanceTest.UserAssertions assertThatUser(String username) {
		return new RMMigrationTo9_2_AcceptanceTest.UserAssertions(username);
	}

	@AllArgsConstructor
	private class UserAssertions {

		String username;

		RMMigrationTo9_2_AcceptanceTest.UserAssertions isInCollections(String... expectedCollectionsArray) {
			List<String> expectedCollections = asList(expectedCollectionsArray);

			SystemWideUserInfos systemWideUser = userInfos(username);
			assertThat(systemWideUser).isNotNull();
			assertThat(systemWideUser.getCollections()).containsOnly(expectedCollectionsArray);

			for (String collection : asList(zeCollection, "LaCollectionDeRida")) {
				boolean expectedInThisCollection = expectedCollections.contains(collection);
				if (expectedInThisCollection) {
					assertThat(user(username, collection)).describedAs("User '" + username + "' is expected in collection '" + collection + "'").isNotNull();
				} else {
					assertThat(user(username, collection)).describedAs("User '" + username + "' is not expected in collection '" + collection + "'").isNull();
				}
			}

			return this;
		}

		public RMMigrationTo9_2_AcceptanceTest.UserAssertions doesNotExist() {
			assertThat(userInfos(username)).isNull();
			assertThat(user(username, "LaCollectionDeRida")).isNull();
			assertThat(user(username, "LaCollectionDeRida")).isNull();

			return this;
		}

		private RMMigrationTo9_2_AcceptanceTest.UserAssertions hasStatusIn(UserCredentialStatus expectedStatus,
																		   String collection) {
			User user = user(username, collection);
			assertThat(user).describedAs("Expecting user '" + username + "' to exist in collection '" + collection + "', but it does not").isNotNull();
			assertThat(userInfos(user.getUsername()).getStatus(user.getCollection()))
					.describedAs("Status in collection '" + collection + "'").isEqualTo(expectedStatus);
			assertThat(user.getStatus()).isEqualTo(expectedStatus);
			boolean expectedLogicallyDeletedStatus = expectedStatus != ACTIVE;
			assertThat(user.isLogicallyDeletedStatus()).isEqualTo(expectedLogicallyDeletedStatus);

			return this;
		}

		private RMMigrationTo9_2_AcceptanceTest.UserAssertions isPhysicicallyDeletedIn(String collection) {
			assertThat(userInfos(username).getStatus(collection)).isNull();
			assertThat(userInfos(username).getCollections()).doesNotContain(collection);
			assertThat(user(username, collection)).isNull();

			return this;
		}

		private RMMigrationTo9_2_AcceptanceTest.UserAssertions hasSyncMode(UserSyncMode mode) {
			assertThat(userInfos(username).getSyncMode()).isSameAs(mode);

			return this;
		}

		private RMMigrationTo9_2_AcceptanceTest.UserAssertions hasGroupsInCollection(String collection,
																					 String... groupCodes) {
			String[] groupIds = new String[groupCodes.length];
			for (int i = 0; i < groupCodes.length; i++) {
				groupIds[i] = group(groupCodes[i], collection).getId();
			}

			assertThat(user(username, collection).getUserGroups()).containsOnly(groupIds);

			return this;
		}

		public RMMigrationTo9_2_AcceptanceTest.UserAssertions hasName(String firstName, String lastName) {
			SystemWideUserInfos userInfos = getModelLayerFactory().newUserServices().getUserInfos(username);
			assertThat(userInfos.getFirstName()).isEqualTo(firstName);
			assertThat(userInfos.getLastName()).isEqualTo(lastName);

			for (String collection : userInfos.getCollections()) {
				assertThat(user(username, collection).getFirstName()).isEqualTo(firstName);
				assertThat(user(username, collection).getLastName()).isEqualTo(lastName);
			}


			return this;
		}

		public RMMigrationTo9_2_AcceptanceTest.UserAssertions hasEmail(String email) {
			SystemWideUserInfos userInfos = getModelLayerFactory().newUserServices().getUserInfos(username);
			assertThat(userInfos.getEmail()).isEqualTo(email);

			for (String collection : userInfos.getCollections()) {
				assertThat(user(username, collection).getEmail()).isEqualTo(email);
			}


			return this;
		}
	}


	private SystemWideUserInfos userInfos(String username) {
		try {
			return getModelLayerFactory().newUserServices().getUserInfos(username);

		} catch (UserServicesRuntimeException_NoSuchUser e) {
			return null;
		}
	}

	private User user(String username, String collection) {
		//This method may exist in UserServices for the moment, but we will try to remove it from the service
		return getModelLayerFactory().newUserServices().getUserInCollection(username, collection);
	}

	private Group group(String code, String collection) {
		return getModelLayerFactory().newUserServices().getGroupInCollection(code, collection);
	}

	private SystemWideGroup groupInfo(String code) {
		return getModelLayerFactory().newUserServices().getGroup(code);
	}


}
