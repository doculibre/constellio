package com.constellio.model.services.security.authentification;

import static com.constellio.sdk.tests.TestUtils.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;

public class LDAPAuthenticationServiceAcceptanceTest extends ConstellioTest {

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingValidLdapUsersWithValidCredentialsThenSuccess()
			throws Exception {
		givenCollectionWithUsers("administrator");
		saveValidLDAPConfig();
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("administrator", SDKPasswords.testLDAPPassword());
		assertThat(authenticated).isTrue();
	}

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingValidLdapUsersWithBlankPasswordThenFailure()
			throws Exception {
		//givenConfiguredToConnectOnTestActiveDirectory();
		givenCollectionWithUsers("administrator");
		saveValidLDAPConfig();
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("administrator", "");
		assertThat(authenticated).isFalse();
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
		givenCollectionWithUsers("bob");
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("bob", "password");
		assertThat(authenticated).isFalse();
	}

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingNonLdapAdminUserThenSuccess()
			throws Exception {
		givenCollectionWithUsers("admin");
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("admin", "password");
		assertThat(authenticated).isTrue();
	}

	@Test
	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingValidLdapUsersWithInvalidCredentialsThenFailure()
			throws Exception {
		givenCollectionWithUsers("administrator");
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("administrator", "bad_password");
		assertThat(authenticated).isFalse();
	}

	private void givenCollectionWithUsers(String... usernames) {
		givenCollection(zeCollection);
		UserServices userServices = getModelLayerFactory().newUserServices();
		for (String username : usernames) {
			UserCredential userCredential = userServices.createUserCredential(username, "Inc", "Onnu",
					username + "@constellio.com", new ArrayList<String>(), asList(zeCollection), UserCredentialStatus.ACTIVE);

			userServices.addUpdateUserCredential(userCredential);
		}
	}


}
