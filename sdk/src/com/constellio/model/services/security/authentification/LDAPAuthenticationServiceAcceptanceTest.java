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
package com.constellio.model.services.security.authentification;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;

public class LDAPAuthenticationServiceAcceptanceTest extends ConstellioTest {

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingValidLdapUsersWithValidCredentialsThenSuccess()
			throws Exception {
		//givenConfiguredToConnectOnTestActiveDirectory();
		givenCollectionWithUsers("administrator");
		saveValidLDAPConfig();
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("administrator", SDKPasswords.testLDAPServer());
		assertThat(authenticated).isTrue();
	}

	private void saveValidLDAPConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPServerConfiguration();
		LDAPUserSyncConfiguration ldapUserSyncConfiguration = LDAPTestConfig.getLDAPUserSyncConfiguration();
		getModelLayerFactory().getLdapConfigurationManager()
				.saveLDAPConfiguration(ldapServerConfiguration, ldapUserSyncConfiguration);
	}

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingNonLdapUsersWithValidCredentialsThenFailure()
			throws Exception {
		//givenConfiguredToConnectOnTestActiveDirectory();
		givenCollectionWithUsers("bob");
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("bob", "password");
		assertThat(authenticated).isFalse();
	}

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingNonLdapAdminUserThenSuccess()
			throws Exception {
		//givenConfiguredToConnectOnTestActiveDirectory();
		givenCollectionWithUsers("admin");
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("admin", "password");
		assertThat(authenticated).isTrue();
	}

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingValidLdapUsersWithInvalidCredentialsThenFailure()
			throws Exception {
		//givenConfiguredToConnectOnTestActiveDirectory();
		givenCollectionWithUsers("administrator");
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("administrator", "bad_password");
		assertThat(authenticated).isFalse();

	}

	/*private void givenConfiguredToConnectOnTestActiveDirectory() {
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(ModelLayerConfiguration configuration) {
				List<String> urls = LDAPTestConfig.getUrls();
				List<String> domains = LDAPTestConfig.getDomains();
				LDAPDirectoryType directoryType = LDAPTestConfig.getDirectoryType();
				LDAPServerConfiguration serverConfig = new LDAPServerConfiguration(urls, domains, directoryType);

				when(configuration.isLDAPAuthentication()).thenReturn(true);
				when(configuration.getLDAPServerConfiguration()).thenReturn(serverConfig);
			}
		});
	}*/

	private void givenCollectionWithUsers(String... usernames) {
		givenCollection(zeCollection);
		UserServices userServices = getModelLayerFactory().newUserServices();
		for (String username : usernames) {
			UserCredential userCredential = new UserCredential(username, "Inc", "Onnu", username + "@constellio.com",
					new ArrayList<String>(), asList(zeCollection), UserCredentialStatus.ACTIVE);

			userServices.addUpdateUserCredential(userCredential);
		}
	}

}
