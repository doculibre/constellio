package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.SolrGlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.SDKFoldersLocator;
import com.constellio.sdk.tests.annotations.SlowTest;

@SlowTest
public class CoreMigrationTo_6_2AcceptanceTest extends ConstellioTest {

	@Test
	public void whenMigratingFromASystemWithUserAndGroupsThenMigrated()
			throws OptimisticLockingConfiguration, NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		givenSystemAtVersion5_1_2withTokens();
		UserServices userServices = getModelLayerFactory().newUserServices();

		SolrGlobalGroup heroes = (SolrGlobalGroup) userServices.getGroup("heroes");
		SolrGlobalGroup legends = (SolrGlobalGroup) userServices.getGroup("legends");
		SolrGlobalGroup sidekicks = (SolrGlobalGroup) userServices.getGroup("sidekicks");

		assertThat(heroes).isNotNull();
		assertThat(legends).isNotNull();
		assertThat(legends.getStatus()).isEqualTo(GlobalGroupStatus.ACTIVE);
		assertThat(legends.getName()).isEqualTo("The legends");

		assertThat(sidekicks.getParent()).isEqualTo(heroes.getCode());

		assertThat(userServices.getUserCredential("admin")).isNotNull();
		assertThat(userServices.getUserCredential("bob")).isNotNull();
		assertThat(userServices.getUserCredential("charles")).isNotNull();
		assertThat(userServices.getUserCredential("dakota")).isNotNull();
		assertThat(userServices.getUserCredential("edouard")).isNotNull();
		assertThat(userServices.getUserCredential("gandalf")).isNotNull();

		UserCredential dakotaUser = userServices.getUserCredential("dakota");
		assertThat(dakotaUser.getFirstName()).isEqualTo("Dakota");
		assertThat(dakotaUser.getLastName()).isEqualTo("L'Indien");
		assertThat(dakotaUser.getUsername()).isEqualTo("dakota");
		assertThat(dakotaUser.getEmail()).isEqualTo("dakota@doculibre.com");
		assertThat(dakotaUser.getCollections()).containsOnly("zeCollection");
		assertThat(dakotaUser.getGlobalGroups()).containsOnly(heroes.getCode());
		assertThat(dakotaUser.getStatus()).isEqualTo(ACTIVE);

		List<String> adminTokens = getModelLayerFactory().newUserServices().getUserCredential("admin").getTokenKeys();
		assertThat(adminTokens).containsOnly("6f9b7e63-a6c1-4783-9143-1e69edf34b4c");

	}

	private void givenSystemAtVersion5_1_2withTokens() {
		givenTransactionLogIsEnabled();
		File statesFolder = new File(new SDKFoldersLocator().getInitialStatesFolder(), "olds");
		File state = new File(statesFolder, "given_system_in_5.1.2.2_with_tasks,rm_modules__with_tokens.zip");

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state);
	}

	@Test
	public void givenSaveSateThenUsersAndGroupsMigratedCorrectly()
			throws Exception {
		String collection2 = "collection2";
		givenSystemInState("given_system_in_6.1_with_users.zip");
		UserServices userServices = getModelLayerFactory().newUserServices();
		Group group1 = userServices.getGroupInCollection("group1InBothCollections", zeCollection);
		Group group11 = userServices.getGroupInCollection("group11", zeCollection);
		Group group111 = userServices.getGroupInCollection("group111", zeCollection);
		Group group1111 = userServices.getGroupInCollection("group1111", zeCollection);
		Group group11111 = userServices.getGroupInCollection("group11111", zeCollection);

		//0. validate users groups relations
		User user1 = userServices.getUserInCollection("user1", zeCollection);
		assertThat(user1.getUserGroups()).containsOnly(group1.getId());
		User user11 = userServices.getUserInCollection("user11", zeCollection);
		assertThat(user11.getUserGroups()).containsOnly(group11.getId());
		User user111 = userServices.getUserInCollection("user111", zeCollection);
		assertThat(user111.getUserGroups()).containsOnly(group111.getId());
		User user1111 = userServices.getUserInCollection("user1111", zeCollection);
		assertThat(user1111.getUserGroups()).containsOnly(group1111.getId());
		User user11111 = userServices.getUserInCollection("user11111", zeCollection);
		assertThat(user11111.getUserGroups()).containsOnly(group11111.getId());
		User user1InAllGroups = userServices.getUserInCollection("user1InAllGroups", zeCollection);
		assertThat(user1InAllGroups.getUserGroups().size()).isEqualTo(14);

		//1. validate collections
		assertThat(userServices.getUser("user1").getCollections()).containsOnly(collection2, zeCollection);
		UserCredential user2InCollection2 = userServices.getUser("user2InCollection2");
		assertThat(user2InCollection2.getCollections()).containsOnly(collection2);
		UserCredential user3InZeCollection = userServices.getUser("user3InZeCollcetion");
		assertThat(user3InZeCollection.getCollections()).containsOnly(zeCollection);
		UserCredential user4InNoCollection = userServices.getUser("user4InNoCollection");
		assertThat(user4InNoCollection.getCollections()).isEmpty();

		//2. validate statuses
		assertThat(user1.getStatus()).isEqualTo(ACTIVE);
		User user5Approval = userServices.getUserInCollection("user5Apporval", zeCollection);
		assertThat(user5Approval.getStatus()).isEqualTo(UserCredentialStatus.PENDING);
		User user6Suspended = userServices.getUserInCollection("user6Suspended", zeCollection);
		assertThat(user6Suspended.getStatus()).isEqualTo(UserCredentialStatus.SUSPENDED);
		User user7Deleted = userServices.getUserInCollection("user7Deleted", zeCollection);
		assertThat(user7Deleted.getStatus()).isEqualTo(UserCredentialStatus.DELETED);

		//3. validate profiles
		User userDossierRecent = userServices.getUserInCollection("userDossierRecent", zeCollection);
		assertThat(userDossierRecent.getDefaultTabInFolderDisplay()).isEqualTo("M");
		assertThat(userDossierRecent.getStartTab()).isEqualTo("lastViewedFolders");
		assertThat(userDossierRecent.getDefaultTaxonomy()).isNull();

		User userPhone1 = userServices.getUserInCollection("userPhone1", zeCollection);
		assertThat(userPhone1.getPhone()).isEqualTo("4185230001");

		User userSousDossiers = userServices.getUserInCollection("userSousDossiers", zeCollection);
		assertThat(userSousDossiers.getDefaultTabInFolderDisplay()).isEqualTo("SF");
		assertThat(userSousDossiers.getStartTab()).isEqualTo("taxonomies");
		assertThat(userSousDossiers.getDefaultTaxonomy()).isNull();

		User userPartageRseau = userServices.getUserInCollection("userPartageReseau", zeCollection);
		assertThat(userPartageRseau.getDefaultTabInFolderDisplay()).isEqualTo("M");
		assertThat(userPartageRseau.getStartTab()).isEqualTo("taxonomies");
		assertThat(userPartageRseau.getDefaultTaxonomy()).isEqualTo("smbFolders");

		//4. validate tokens
		UserCredential user11Credentials = userServices.getUser("user11");
		Map<String, LocalDateTime> tokens = user11Credentials.getAccessTokens();
		assertThat(tokens.size()).isEqualTo(1);
		assertThat(user2InCollection2.getAccessTokens()).isEmpty();

	}

	@Test
	public void givenSystemWithStrangeUsersThenNotAllAreMigrated()
			throws Exception {
		givenSystemInState("given_system_in_6.1_with__module__with-strange-users.zip");
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		SchemasRecordsServices system = new SchemasRecordsServices(SYSTEM_COLLECTION, getModelLayerFactory());
		UserServices userServices = getModelLayerFactory().newUserServices();

		//Verify usercredential and global group

		List<GlobalGroup> globalGroups = system.wrapGlobalGroups(searchServices.search(new LogicalSearchQuery(
				from(system.globalGroupSchemaType()).returnAll())));

		assertThat(globalGroups).extracting("code", "name", "status").contains(
				tuple("group1", "Ze group 1", GlobalGroupStatus.ACTIVE)
		);

		List<UserCredential> userCredentials = system.wrapCredentials(searchServices.search(new LogicalSearchQuery(
				from(system.credentialSchemaType()).returnAll())));

		assertThat(userCredentials).extracting("username", "firstName", "lastName", "email", "status").contains(
				tuple("admin", "System", "Admin", "admin@organization.com", ACTIVE),
				tuple("alice1", "Alice", "Wonderland", "alice@email.com", ACTIVE),
				tuple("alice2", "Alice", "Wonderland", "alice@email.com", ACTIVE),
				tuple("charles", "Charles", "Xavier", null, ACTIVE),
				tuple("chuck", null, null, null, ACTIVE),
				tuple("gandalf", "gandalf", "legris", "gandalf.legris@gmail.com", ACTIVE)
		);

		//Verify user and groups is ze collection

		SchemasRecordsServices collection = new SchemasRecordsServices(zeCollection, getModelLayerFactory());
		List<Group> groups = system.wrapGroups(searchServices.search(new LogicalSearchQuery(
				from(collection.groupSchemaType()).returnAll())));

		assertThat(groups).extracting("code", "title").contains(
				tuple("group1", "Ze group 1")
		);

		List<User> users = system.wrapUsers(searchServices.search(new LogicalSearchQuery(
				from(collection.userSchemaType()).returnAll())));

		assertThat(users).extracting("username", "firstName", "lastName", "email", "status").contains(
				tuple("alice1", "Alice", "Wonderland", "alice@email.com", ACTIVE),
				tuple("alice2", "Alice", "Wonderland", "alice@email.com", ACTIVE),
				tuple("chuck", null, null, null, ACTIVE),
				tuple("gandalf", "gandalf", "legris", "gandalf.legris@gmail.com", ACTIVE)
		);

	}

	private void givenSystemInState(String systemState) {
		givenTransactionLogIsEnabled();
		File statesFolder = new SDKFoldersLocator().getInitialStatesFolder();
		File state = new File(statesFolder, systemState);

		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(state).withFakeEncryptionServices();
	}

}
