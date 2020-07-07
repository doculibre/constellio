package com.constellio.model.services.security.authentification;

import com.constellio.model.conf.LDAPTestConfig;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.SDKPasswords;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import org.junit.Test;

import java.util.ArrayList;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class LDAPAuthenticationServiceAcceptanceTest extends ConstellioTest {

	//TODO - Disabled @Test
	public void givenLdapsWhenAuthenticatingUsersThenOK()
			throws Exception {
		givenCollectionWithUsers("administrator");
		saveValidLDAPSConfig();
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();

		boolean authenticated = authenticationService.authenticate("administrator", SDKPasswords.testLDAPPassword());
		assertThat(authenticated).isTrue();

		authenticated = authenticationService.authenticate("administrator", SDKPasswords.testLDAPPassword() + "salt");
		assertThat(authenticated).isFalse();
	}


	@Test
	@InDevelopmentTest

	public void givenActiveDirectoryAuthenticationManagerWhenAuthenticatingValidLdapUsersWithValidCredentialsThenSuccess()
			throws Exception {
		givenCollectionWithUsers("administrator");
		saveValidLDAPConfig();
		AuthenticationService authenticationService = getModelLayerFactory().newAuthenticationService();
		boolean authenticated = authenticationService.authenticate("administrator", SDKPasswords.testLDAPPassword());
		assertThat(authenticated).isTrue();
	}

	@Test
	@InDevelopmentTest
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

	private void saveValidLDAPSConfig() {
		LDAPServerConfiguration ldapServerConfiguration = LDAPTestConfig.getLDAPSServerConfiguration();
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
			UserCredential userCredential = createUserCredential(username, "Inc", "Onnu",
					username + "@constellio.com", new ArrayList<String>(), asList(zeCollection), UserCredentialStatus.ACTIVE);

			userServices.addUpdateUserCredential(userCredential);
		}
	}

}
