package com.constellio.model.services.users.sync;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

@InternetTest
public class LDAPUserSyncManagerAcceptanceTest extends ConstellioTest {
	ModelLayerFactory modelLayerFactory;

	UserServices userServices;
	private LDAPConfigurationManager ldapConfigurationManager;

	@Before
	public void setup()
			throws Exception {
		//givenConstellioProperties(LDAPTestConfig.getConfigMap());
		givenCollectionWithTitle(zeCollection, "Collection de test");
		givenCollectionWithTitle(businessCollection, "Collection de Rida");
		modelLayerFactory = getModelLayerFactory();

		userServices = spy(modelLayerFactory.newUserServices());
		this.ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();
		saveValidLDAPConfig();
	}

	private void saveValidLDAPConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	private void saveValidLDAPConfigWithEntrepriseCollectionSelected() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfigurationWithSelectedCollections(
				asList(businessCollection));
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void givenUserSyncConfiguredThenRunSynchronizationsBasedOnDuration()
			throws Exception {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		int usersCountAfterSync = userServices.getAllUserCredentials().size();

		for (UserCredential userCredential : userServices.getAllUserCredentials()) {
			String username = userCredential.getUsername();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(username)).isTrue();
		}

		int ldapActiveUsersCount = 14;
		assertThat(usersCountAfterSync).isEqualTo(ldapActiveUsersCount);
		UserCredential importedUser = user("bfay");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		//assertThat(importedUser.getEmail()).isEqualTo("");
		assertThat(importedUser.getMsExchDelegateListBL()).isEmpty();
		assertThat(importedUser.getDn()).isEqualTo("CN=bfay,CN=Users,DC=test,DC=doculibre,DC=ca");

		for (GlobalGroup group : userServices.getAllGroups()) {
			String code = group.getName();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(code)).isTrue();
		}
		assertThat(userServices.getAllGroups().size()).isEqualTo(14);
	}

	@Test
	// Confirm @SlowTest
	public void givenMsExchDelegateListBLAndUserSyncConfiguredWhenResyncronizeThenGoodInfosForUserIndexer()
			throws Exception {

		saveValidExchangeLDAPConfig();
		List<String> msExchDelegateListBL = new ArrayList<>();
		msExchDelegateListBL.add("CN=Shared Email,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 799,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 797,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 010,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 009,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 008,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 007,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 006,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 005,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 004,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 003,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 002,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 001,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=user 000,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=User Ten,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=User Five,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=User One,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=Malik E. El jihl,CN=Users,DC=test,DC=doculibre,DC=ca");
		msExchDelegateListBL.add("CN=Patrick D. Dupont,CN=Users,DC=test,DC=doculibre,DC=ca");

		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();
		ldapUserSyncManager.synchronizeIfPossible();

		UserCredential importedUser = user("indexer");
		assertThat(importedUser.getFirstName()).isEqualTo("indexer");
		assertThat(importedUser.getLastName()).isEqualTo("indexer");
		assertThat(importedUser.getEmail()).isEqualTo("iindexer@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEqualTo(msExchDelegateListBL);
		assertThat(importedUser.getDn()).isEqualTo("CN=indexer indexer,CN=Users,DC=test,DC=doculibre,DC=ca");

		UserCredential updatedUser = importedUser.setMsExchDelegateListBL(Arrays.asList("newMSExchDelegateListBL"));
		userServices.addUpdateUserCredential(updatedUser);
		updatedUser = user("indexer");
		assertThat(updatedUser.getMsExchDelegateListBL()).isEqualTo(Arrays.asList("newMSExchDelegateListBL"));

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential newImportedUser = user("indexer");
		assertThat(newImportedUser.getFirstName()).isEqualTo("indexer");
		assertThat(newImportedUser.getLastName()).isEqualTo("indexer");
		assertThat(newImportedUser.getEmail()).isEqualTo("iindexer@doculibre.ca");
		assertThat(newImportedUser.getMsExchDelegateListBL()).isEqualTo(msExchDelegateListBL);
	}

	private void saveValidExchangeLDAPConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getExchangeLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getExchangeLDAPUserSyncConfiguration();
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	private GlobalGroup group(String code) {
		return userServices.getGroup(code);
	}

	private UserCredential user(String code) {
		return userServices.getUserCredential(code);
	}

	@Test
	public void givenExistingGroupBeforeLDAPSyncThenAfterLDAPSyncGroupWithSameCollections() {
		String group = "CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		GlobalGroup globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).isEmpty();
		globalGroup = globalGroup.setUsersAutomaticallyAddedToCollections(asList(new String[]{zeCollection}));
		userServices.addUpdateGlobalGroup(globalGroup);
		globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).containsOnly(zeCollection);

		ldapUserSyncManager.synchronizeIfPossible();
		globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).containsOnly(zeCollection);
		UserCredential userInGroup = userServices.getUser("bfay");
		assertThat(userInGroup.getCollections()).containsOnly(zeCollection);
	}

	@Test
	public void givenExistingUserBeforeLDAPSyncThenAfterLDAPSyncUserWithSameCollections() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).isEmpty();
		userServices.addUserToCollection(bfay, zeCollection);
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection);
		ldapUserSyncManager.synchronizeIfPossible();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection);
	}

	@Test
	public void givenExistingGroupInZeCollectionWhenLDAPSynchronizeWithBusinessCollectionSelectedThenGroupWithZeCollectionAndBusinessCollection() {
		String group = "CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		GlobalGroup globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).isEmpty();
		globalGroup = globalGroup.setUsersAutomaticallyAddedToCollections(asList(new String[]{zeCollection}));
		userServices.addUpdateGlobalGroup(globalGroup);

		saveValidLDAPConfigWithEntrepriseCollectionSelected();
		ldapUserSyncManager.synchronizeIfPossible();

		globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).containsOnly(zeCollection, businessCollection);
		UserCredential userInGroup = userServices.getUser("bfay");
		assertThat(userInGroup.getCollections()).containsOnly(zeCollection, businessCollection);
	}

	@Test
	public void givenExistingUserInZeCollectionWhenLDAPSynchronizeWithBusinessCollectionSelectedThenGroupWithZeCollectionAndBusinessCollection() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).isEmpty();
		userServices.addUserToCollection(bfay, zeCollection);
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection);

		saveValidLDAPConfigWithEntrepriseCollectionSelected();
		ldapUserSyncManager.synchronizeIfPossible();

		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection, businessCollection);
	}

	@Test
	public void beforeSyncUserInactiveInConstellioButActiveInLDAPThenAfterSyncUserActive() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);

		bfay = bfay.setStatus(UserCredentialStatus.SUSPENDED);
		userServices.addUpdateUserCredential(bfay);

		ldapUserSyncManager.synchronizeIfPossible();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	@Test
	public void givenUserWithTokensWhenSyncThenKeepTokens() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		userServices.addUpdateUserCredential(bfay.setSystemAdminEnabled());
		bfay = userServices.getUser("bfay");
		assertThat(bfay.isSystemAdmin()).isTrue();

		ldapUserSyncManager.synchronizeIfPossible();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.isSystemAdmin()).isTrue();
	}

	@Test
	public void givenUserWithGeneratedTokensWhenSyncThenKeepTokens() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		String token = userServices.generateToken(bfay.getUsername());

		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).containsOnly(token);

		ldapUserSyncManager.synchronizeIfPossible();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).containsOnly(token);
	}

	@Test
	public void givenUserIsSystemAdminWhenSyncThenStaySystemAdmin() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		userServices.generateToken(bfay.getUsername());
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).isNotEmpty();

		ldapUserSyncManager.synchronizeIfPossible();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).isNotEmpty();
	}

	@Test
	public void beforeSyncUserActiveInConstellioButInactiveInLDAPThenAfterSyncUserInactive() {
		String inactiveUserInLDAP = "krbtgt";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential userCredentials = userServices.getUser(inactiveUserInLDAP);
		assertThat(userCredentials.getStatus()).isEqualTo(UserCredentialStatus.DELETED);

		UserCredential userCredential = createUserCredential(inactiveUserInLDAP, inactiveUserInLDAP, inactiveUserInLDAP,
				inactiveUserInLDAP + "@doculibre.com", asList(new String[]{}), asList(new String[]{}),
				UserCredentialStatus.ACTIVE);
		userServices.addUpdateUserCredential(userCredential);
		userServices.getUser(inactiveUserInLDAP);

		ldapUserSyncManager.synchronizeIfPossible();
		assertThat(userServices.getUser(inactiveUserInLDAP).getUsername()).isEqualTo(inactiveUserInLDAP);
	}

	@Test
	public void beforeSyncUserInGroupsA_BInConstellioButInGroupsB_CInLDAPThenAfterSyncUserInGroupsB_C() {
		String groupA = "A";
		String groupB = "CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		String groupC = "CN=C,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();
		UserCredential bfay = userServices.getUser("bfay");
		List<String> currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupC);
		List<String> usersAutomaticallyAddedToCollections = Collections.emptyList();
		userServices.addUpdateGlobalGroup(userServices.createGlobalGroup(
				groupA, groupA, usersAutomaticallyAddedToCollections, null, GlobalGroupStatus.ACTIVE, false));
		bfay = bfay.setGlobalGroups(asList(groupA, groupB));
		userServices.addUpdateUserCredential(bfay);
		currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupA);

		ldapUserSyncManager.synchronizeIfPossible();
		bfay = userServices.getUser("bfay");
		currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupC);
	}

	// Confirm @SlowTest
	@Test
	public void whenSearchingMoreThan1000UsersThenReturnAllUsers()
			throws Exception {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		ldapUserSyncConfiguration = new LDAPUserSyncConfiguration(ldapUserSyncConfiguration.getUser(),
				ldapUserSyncConfiguration.getPassword(),
				ldapUserSyncConfiguration.getUserFilter(),
				ldapUserSyncConfiguration.getGroupFilter(),
				ldapUserSyncConfiguration.getDurationBetweenExecution(),
				ldapUserSyncConfiguration.getScheduleTime(),
				ldapUserSyncConfiguration.getGroupBaseContextList(),
				Arrays.asList("OU=Departement1,OU=doculibre,DC=test,DC=doculibre,DC=ca"),
				ldapUserSyncConfiguration.getUserFilterGroupsList(),
				ldapUserSyncConfiguration.isMembershipAutomaticDerivationActivated());
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);

		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronizeIfPossible();

		List<UserCredential> allUsers = userServices.getAllUserCredentials();

		assertThat(allUsers.size()).isGreaterThan(3000);
	}

	@Test
	public void givenMembershipAutomaticDerivationOptionDisabled_WhenSyncing_ThenUsersAndGroupsImportAreDecoupledNotFilteredByGroupSearchBaseContext()
			throws Exception {
		// Given
		final LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();

		getModelLayerFactory().
				getLdapConfigurationManager().
				saveLDAPConfiguration(
						LDAPTestConfig.getLDAPServerConfiguration(),
						new LDAPUserSyncConfiguration(
								ldapUserSyncConfiguration.getUser(),
								ldapUserSyncConfiguration.getPassword(),
								new RegexFilter(
										"",
										"Sharepoint.*"
								),
								new RegexFilter(
										"",
										"group105"
								),
								ldapUserSyncConfiguration.getDurationBetweenExecution(),
								ldapUserSyncConfiguration.getScheduleTime(),
								Arrays.asList(
										"CN=group100,OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca",
										"CN=group101,OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca",
										"CN=group102,OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca",
										"CN=group103,OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca",
										"CN=group104,OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca",
										"CN=group105,OU=Departement2,OU=doculibre,DC=test,DC=doculibre,DC=ca"
								),
								Arrays.asList(
										"CN=Users,DC=test,DC=doculibre,DC=ca"
								),
								Arrays.asList(
										"CN=Sharepoint Groups Test,OU=Groupes,DC=test,DC=doculibre,DC=ca"
								),
								false
						)
				);

		// When
		getModelLayerFactory().getLdapUserSyncManager().synchronizeIfPossible();

		// Then
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION, modelLayerFactory);
		final Set<String> userNameList = new TreeSet<>(CollectionUtils.collect(systemSchemas.getAllUserCredentials(), new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((UserCredential) input).getUsername();
			}
		}));
		assertThat(userNameList).isEqualTo(new TreeSet<>(Arrays.asList(new String[]{
				// Users imported based on users search base, user groups filter and users regex search filter
				"admin", "bgagnon", "vdq2"})));

		final Set<String> groupNameList = new TreeSet<>(CollectionUtils.collect(userServices.getAllGroups(), new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((GlobalGroup) input).getName();
			}
		}));
		assertThat(groupNameList).isEqualTo(new TreeSet<>(Arrays.asList(new String[]{
				// Groups derived from imported users
				"Second sharepoint group", "Sharepoint Groups Test",
				// Groups imported based on groups search base and groups search filter
				"group100", "group101", "group102", "group103", "group104"})));
	}

	@Test
	public void givenMembershipAutomaticDerivationOptionDisabled_WhenSyncing_ThenEmptyGroupAreImported()
			throws Exception {
		// Given
		final LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();

		getModelLayerFactory().
				getLdapConfigurationManager().
				saveLDAPConfiguration(
						LDAPTestConfig.getLDAPServerConfiguration(),
						new LDAPUserSyncConfiguration(
								ldapUserSyncConfiguration.getUser(),
								ldapUserSyncConfiguration.getPassword(),
								new RegexFilter(
										"",
										""
								),
								new RegexFilter(
										"",
										""
								),
								ldapUserSyncConfiguration.getDurationBetweenExecution(),
								ldapUserSyncConfiguration.getScheduleTime(),
								Arrays.asList(
										"CN=emptyGroup,OU=Departement4,OU=doculibre,DC=test,DC=doculibre,DC=ca"
								),
								Arrays.asList(
										"CN=Users,DC=test,DC=doculibre,DC=ca"
								),
								Arrays.asList(
										"CN=Sharepoint Groups Test,OU=Groupes,DC=test,DC=doculibre,DC=ca"
								),
								false
						)
				);

		// When
		getModelLayerFactory().getLdapUserSyncManager().synchronizeIfPossible();

		// Then
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION, modelLayerFactory);
		final Set<String> userNameList = new TreeSet<>(CollectionUtils.collect(systemSchemas.getAllUserCredentials(), new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((UserCredential) input).getUsername();
			}
		}));
		assertThat(userNameList).isEqualTo(new TreeSet<>(Arrays.asList(new String[]{
				// Users imported based on users search base, user groups filter and users regex search filter
				"admin", "bgagnon", "sharepointtest", "vdq2"})));

		final Set<String> groupNameList = new TreeSet<>(CollectionUtils.collect(userServices.getAllGroups(), new Transformer() {
			@Override
			public Object transform(Object input) {
				return ((GlobalGroup) input).getName();
			}
		}));
		assertThat(groupNameList).isEqualTo(new TreeSet<>(Arrays.asList(new String[]{
				// Groups derived from imported users
				"Second sharepoint group", "Sharepoint Groups Test",
				// Groups imported based on groups search base and groups search filter
				"emptyGroup"})));
	}

	@Test
	public void givenExistingPreviouslySyncedUserInSomeLocalGroup_WhenSyncing_ThenUserHasBothTheLocalGroupAndThoseJustSynced() {
		// Given
		final LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		final String myLocalGroupCode = "myLocalGroupCode";
		final GlobalGroup myLocalGroup = userServices.createGlobalGroup(myLocalGroupCode, myLocalGroupCode, Arrays.asList(new String[]{zeCollection}), null, GlobalGroupStatus.ACTIVE, true);
		userServices.addUpdateGlobalGroup(myLocalGroup);

		ldapUserSyncManager.synchronizeIfPossible();

		final String myUsername = "bfay";
		UserCredential myUserCredential = userServices.getUser(myUsername);
		userServices.addUserToCollection(myUserCredential, zeCollection);
		myUserCredential.setGlobalGroups(Arrays.asList(new String[]{myLocalGroupCode}));
		userServices.addUpdateUserCredential(myUserCredential);

		// When
		ldapUserSyncManager.synchronizeIfPossible();

		// Then
		myUserCredential = userServices.getUser(myUsername);
		assertThat(new TreeSet<>(myUserCredential.getGlobalGroups())).isEqualTo(new TreeSet<>(Arrays.asList(new String[]{"CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca", "CN=C,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca", myLocalGroupCode})));
	}
}
