/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.users.sync;

import static org.assertj.core.api.Assertions.assertThat;

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

	@Test
	public void givenUserSyncConfiguredThenRunSynchronizationsBasedOnDuration()
			throws Exception {
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager,
				ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

		ldapUserSyncManager.synchronize();
		int usersCountAfterSync = userServices.getAllUserCredentials().size();

		for (UserCredential userCredential : userServices.getAllUserCredentials()) {
			String username = userCredential.getUsername();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(username)).isTrue();
		}

		int ldapActiveUsersCount = 6;
		assertThat(usersCountAfterSync).isEqualTo(ldapActiveUsersCount);
		UserCredential importedUser = user("bfay");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("");

		for (GlobalGroup group : globalGroupsManager.getAllGroups()) {
			String code = group.getName();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(code)).isTrue();
		}
		assertThat(globalGroupsManager.getAllGroups().size()).isEqualTo(11);
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
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager,
				ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

		ldapUserSyncManager.synchronize();
		GlobalGroup globalGroup = userServices.getGroup(group);
		assertThat(globalGroup.getUsersAutomaticallyAddedToCollections()).isEmpty();
		globalGroup = globalGroup.withUsersAutomaticallyAddedToCollections(Arrays.asList(new String[] { zeCollection }));
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
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager,
				ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

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
	public void beforeSyncUserInactiveInConstellioButActiveInLDAPThenAfterSyncUserActive() {
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager,
				ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		assertThat(bfay.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);

		bfay = bfay.withStatus(UserCredentialStatus.SUPENDED);
		userServices.addUpdateUserCredential(bfay);

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		assertThat(bfay.getStatus()).isEqualTo(UserCredentialStatus.ACTIVE);
	}

	@Test
	public void beforeSyncUserActiveInConstellioButInactiveInLDAPThenAfterSyncUserInactive() {
		String inactiveUserInLDAP = "krbtgt";
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager,
				ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

		ldapUserSyncManager.synchronize();
		UserCredential userCredentials = userServices.getUser(inactiveUserInLDAP);
		assertThat(userCredentials.getStatus()).isEqualTo(UserCredentialStatus.DELETED);

		UserCredential userCredential = new UserCredential(inactiveUserInLDAP, inactiveUserInLDAP, inactiveUserInLDAP,
				inactiveUserInLDAP + "@doculibre.com",
				Arrays.asList(new String[] { }), Arrays.asList(new String[] { }), UserCredentialStatus.ACTIVE);
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
		String ldapSystemGroup = "CN=WSS_ADMIN_WPG,CN=Users,DC=test,DC=doculibre,DC=ca";
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager,
				ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

		ldapUserSyncManager.synchronize();
		UserCredential bfay = userServices.getUser("bfay");
		List<String> currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupC, ldapSystemGroup);
		List<String> usersAutomaticallyAddedToCollections = Collections.emptyList();
		userServices.addUpdateGlobalGroup(
				new GlobalGroup(groupA, groupA, usersAutomaticallyAddedToCollections, null, GlobalGroupStatus.ACTIVE));
		bfay = bfay.withGlobalGroups(Arrays.asList(new String[] { groupA, groupB }));
		userServices.addUpdateUserCredential(bfay);
		currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupA);

		ldapUserSyncManager.synchronize();
		bfay = userServices.getUser("bfay");
		currentGroups = bfay.getGlobalGroups();
		assertThat(currentGroups).containsOnly(groupB, groupC, ldapSystemGroup);
	}
}
