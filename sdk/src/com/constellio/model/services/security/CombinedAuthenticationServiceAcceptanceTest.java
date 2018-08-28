package com.constellio.model.services.security;

import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.extensions.ModelLayerSystemExtensions;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.security.authentification.CombinedAuthenticationService;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CombinedAuthenticationServiceAcceptanceTest extends ConstellioTest {

	CombinedAuthenticationService combinedAuthenticationService;
	LDAPConfigurationManager ldapConfigurationManager = mock(LDAPConfigurationManager.class);
	LDAPAuthenticationService ldapAuthenticationService;
	PasswordFileAuthenticationService passwordFileAuthenticationService;

	@Mock ModelLayerSystemExtensions modelLayerSystemExtensions;
	@Mock ModelLayerExtensions modelLayerExtensions;

	@Before
	public void setUp()
			throws Exception {
		ldapConfigurationManager = mock(LDAPConfigurationManager.class);
		ldapAuthenticationService = mock(LDAPAuthenticationService.class);
		passwordFileAuthenticationService = mock(PasswordFileAuthenticationService.class);
		when(modelLayerExtensions.getSystemWideExtensions()).thenReturn(modelLayerSystemExtensions);

		combinedAuthenticationService = new CombinedAuthenticationService(ldapConfigurationManager, ldapAuthenticationService,
				passwordFileAuthenticationService, modelLayerExtensions);
	}

	@Test
	public void givenNonAdminWhenAuthenticateThenLdapOnly() {
		when(ldapConfigurationManager.isLDAPAuthentication()).thenReturn(true);
		combinedAuthenticationService.authenticate("anAdmin", "password");
		verify(ldapAuthenticationService, times(1)).authenticate(anyString(), anyString());
		verify(passwordFileAuthenticationService, never()).authenticate(anyString(), anyString());
	}

	@Test
	public void givenNonAdminWhenLDAPAuthenticateFailedAndNoFallBackAvailableThenLdapOnly() {
		when(modelLayerSystemExtensions.canAuthenticateUsingPasswordFileIfLDAPFailed("anAdmin")).thenReturn(false);
		when(ldapConfigurationManager.isLDAPAuthentication()).thenReturn(true);
		when(passwordFileAuthenticationService.authenticate("anAdmin", "password")).thenReturn(true);

		boolean response = combinedAuthenticationService.authenticate("anAdmin", "password");
		verify(ldapAuthenticationService, times(1)).authenticate(anyString(), anyString());
		verify(passwordFileAuthenticationService, never()).authenticate(anyString(), anyString());
		assertThat(response).isEqualTo(false);
	}

	@Test
	public void givenNonAdminWhenLDAPAuthenticateFailedAndFallBackAvailableThenFallbackOnly() {
		when(modelLayerSystemExtensions.canAuthenticateUsingPasswordFileIfLDAPFailed("anAdmin")).thenReturn(true);
		when(ldapConfigurationManager.isLDAPAuthentication()).thenReturn(true);
		when(passwordFileAuthenticationService.authenticate("anAdmin", "password")).thenReturn(true);
		when(passwordFileAuthenticationService.authenticate("anAdmin", "badPassword")).thenReturn(false);

		boolean response1 = combinedAuthenticationService.authenticate("anAdmin", "password");
		assertThat(response1).isEqualTo(true);
		verify(ldapAuthenticationService, times(1)).authenticate(anyString(), anyString());
		verify(passwordFileAuthenticationService, times(1)).authenticate(anyString(), anyString());

		boolean response2 = combinedAuthenticationService.authenticate("anAdmin", "badPassword");
		assertThat(response2).isEqualTo(false);
	}
}
