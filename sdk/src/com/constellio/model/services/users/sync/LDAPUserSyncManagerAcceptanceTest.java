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

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.*;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.GlobalGroupsManager;
import com.constellio.model.services.users.UserCredentialsManager;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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
		getModelLayerFactory().getLdapConfigurationManager().saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}


	@Test
	public void givenUserSyncConfiguredThenRunSynchronizationsBasedOnDuration()
			throws Exception {
		LDAPUserSyncManager ldapUserSyncManager = new LDAPUserSyncManager(userServices, globalGroupsManager, ldapConfigurationManager, null);
		ldapUserSyncManager.initialize();

		ldapUserSyncManager.synchronize();
		int usersCountAfterSync = userServices.getAllUserCredentials().size();

		for(UserCredential userCredential : userServices.getAllUserCredentials()){
			String username = userCredential.getUsername();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(username)).isTrue();
		}

		int ldapActiveUsersCount = 6;
		assertThat(usersCountAfterSync).isEqualTo(1 + ldapActiveUsersCount);
		UserCredential importedUser = user("bfay");
		assertThat(importedUser.getFirstName()).isEqualTo("Nicolas");
		assertThat(importedUser.getLastName()).isEqualTo("Belisle");
		assertThat(importedUser.getEmail()).isEqualTo("");

		for(GlobalGroup group : globalGroupsManager.getAllGroups()){
			String code = group.getName();
			assertThat(ldapConfigurationManager.getLDAPUserSyncConfiguration().isGroupAccepted(code)).isTrue();
		}
		assertThat(globalGroupsManager.getAllGroups().size()).isEqualTo(9);
	}

	private GlobalGroup group(String code) {
		return globalGroupsManager.getGlobalGroupWithCode(code);
	}

	private UserCredential user(String code) {
		return userCredentialsManager.getUserCredential(code);
	}
}
