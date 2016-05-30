package com.constellio.model.services.users.sync;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import com.constellio.sdk.tests.annotations.SlowTest;

@InternetTest
public class LDAPUserSyncManagerAcceptanceTest extends ConstellioTest {
	ModelLayerFactory modelLayerFactory;

	UserServices userServices;
	private UserCredentialsManager userCredentialsManager;
	private GlobalGroupsManager globalGroupsManager;
	private LDAPConfigurationManager ldapConfigurationManager;

	@Before
	public void setup()
			throws Exception {
		//givenConstellioProperties(LDAPTestConfig.getConfigMap());
		givenCollectionWithTitle(zeCollection, "Collection de test");
		givenCollectionWithTitle(businessCollection, "Collection de Rida");
		modelLayerFactory = getModelLayerFactory();

		userServices = modelLayerFactory.newUserServices();
		userCredentialsManager = modelLayerFactory.getUserCredentialsManager();
		globalGroupsManager = modelLayerFactory.getGlobalGroupsManager();
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

		ldapUserSyncManager.synchronize();
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

		for (GlobalGroup group : globalGroupsManager.getAllGroups()) {
			String code = group.getName();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(code)).isTrue();
		}
		assertThat(globalGroupsManager.getAllGroups().size()).isEqualTo(14);
	}

	@Test
	@SlowTest
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
		ldapUserSyncManager.synchronize();

		UserCredential importedUser = user("indexer");
		assertThat(importedUser.getFirstName()).isEqualTo("indexer");
		assertThat(importedUser.getLastName()).isEqualTo("indexer");
		assertThat(importedUser.getEmail()).isEqualTo("iindexer@doculibre.ca");
		assertThat(importedUser.getMsExchDelegateListBL()).isEqualTo(msExchDelegateListBL);
		assertThat(importedUser.getDn()).isEqualTo("CN=indexer indexer,CN=Users,DC=test,DC=doculibre,DC=ca");

		UserCredential updatedUser = importedUser.withMsExchDelegateListBL(Arrays.asList("newMSExchDelegateListBL"));
		userServices.addUpdateUserCredential(updatedUser);
		updatedUser = user("indexer");
		assertThat(updatedUser.getMsExchDelegateListBL()).isEqualTo(Arrays.asList("newMSExchDelegateListBL"));

		ldapUserSyncManager.synchronize();
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
		return globalGroupsManager.getGlobalGroupWithCode(code);
	}

	private UserCredential user(String code) {
		return userCredentialsManager.getUserCredential(code);
	}

	@Test
	public void givenExistingGroupBeforeLDAPSyncThenAfterLDAPSyncGroupWithSameCollections() {
		String group = "CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		GlobalGroup globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).isEmpty();
		globalGroup = globalGroup.withUsersAutomaticallyAddedToCollections(asList(new String[] { zeCollection }));
		userServices.addUpdateGlobalGroup(globalGroup);
		globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).containsOnly(zeCollection);

		ldapUserSyncManager.synchronize();
		globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).containsOnly(zeCollection);
		UserCredential userInGroup = userServices.getUser("bfay");
		assertThat(userInGroup.getCollections()).containsOnly(zeCollection);
	}

	@Test
	public void givenExistingUserBeforeLDAPSyncThenAfterLDAPSyncUserWithSameCollections() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).isEmpty();
		userServices.addUserToCollection(bfay, zeCollection);
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection);
		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection);
	}

	@Test
	public void givenExistingGroupInZeCollectionWhenLDAPSynchronizeWithBusinessCollectionSelectedThenGroupWithZeCollectionAndBusinessCollection() {
		String group = "CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		GlobalGroup globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).isEmpty();
		globalGroup = globalGroup.withUsersAutomaticallyAddedToCollections(asList(new String[] { zeCollection }));
		userServices.addUpdateGlobalGroup(globalGroup);

		saveValidLDAPConfigWithEntrepriseCollectionSelected();
		ldapUserSyncManager.synchronize();

		globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).containsOnly(zeCollection, businessCollection);
		UserCredential userInGroup = userServices.getUser("bfay");
		assertThat(userInGroup.getCollections()).containsOnly(zeCollection, businessCollection);
	}

	@Test
	public void givenExistingUserInZeCollectionWhenLDAPSynchronizeWithBusinessCollectionSelectedThenGroupWithZeCollectionAndBusinessCollection() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).isEmpty();
		userServices.addUserToCollection(bfay, zeCollection);
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection);

		saveValidLDAPConfigWithEntrepriseCollectionSelected();
		ldapUserSyncManager.synchronize();

		bfay = userServices.getUser("bfay");
		assertThat(bfay.getCollections()).containsOnly(zeCollection, businessCollection);
	}

	@Test
	public void beforeSyncUserInactiveInConstellioButActiveInLDAPThenAfterSyncUserActive() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);

		bfay = bfay.withStatus(UserCredentialStatus.SUSPENDED);
		userServices.addUpdateUserCredential(bfay);

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	@Test
	public void givenUserWithTokensWhenSyncThenKeepTokens() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		userServices.addUpdateUserCredential(bfay.withSystemAdminPermission());
		bfay = userServices.getUser("bfay");
		assertThat(bfay.isSystemAdmin()).isTrue();

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.isSystemAdmin()).isTrue();
	}

	@Test
	public void givenUserWithGeneratedTokensWhenSyncThenKeepTokens() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		String token = userServices.generateToken(bfay.getUsername());

		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).containsOnly(token);

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).containsOnly(token);
	}

	@Test
	public void givenUserIsSystemAdminWhenSyncThenStaySystemAdmin() {
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		userServices.generateToken(bfay.getUsername());
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).isNotEmpty();

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getTokenKeys()).isNotEmpty();
	}

	@Test
	public void beforeSyncUserActiveInConstellioButInactiveInLDAPThenAfterSyncUserInactive() {
		String inactiveUserInLDAP = "krbtgt";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential userCredentials = userServices.getUser(inactiveUserInLDAP);
		assertThat(userCredentials.getStatus()).isEqualTo(UserCredentialStatus.DELETED);

		UserCredential userCredential = userServices
				.createUserCredential(inactiveUserInLDAP, inactiveUserInLDAP, inactiveUserInLDAP,
						inactiveUserInLDAP + "@doculibre.com", asList(new String[] {}), asList(new String[] {}),
						UserCredentialStatus.ACTIVE);
		userServices.addUpdateUserCredential(userCredential);
		userServices.getUser(inactiveUserInLDAP);

		ldapUserSyncManager.synchronize();
		assertThat(userServices.getUser(inactiveUserInLDAP).getUsername()).isEqualTo(inactiveUserInLDAP);
	}

	@Test
	public void beforeSyncUserInGroupsA_BInConstellioButInGroupsB_CInLDAPThenAfterSyncUserInGroupsB_C() {
		String groupA = "A";
		String groupB = "CN=B,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		String groupC = "CN=C,OU=Fonct1,OU=Groupes,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = getModelLayerFactory().getLdapUserSyncManager();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		List<String> currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupC);
		List<String> usersAutomaticallyAddedToCollections = Collections.emptyList();
		userServices.addUpdateGlobalGroup(userServices.createGlobalGroup(
				groupA, groupA, usersAutomaticallyAddedToCollections, null, GlobalGroupStatus.ACTIVE));
		bfay = bfay.withGlobalGroups(asList(groupA, groupB));
		userServices.addUpdateUserCredential(bfay);
		currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupA);

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupC);
	}
}
