package com.constellio.model.services.security;

import static org.mockito.Mockito.*;

import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.services.security.authentification.CombinedAuthenticationService;
import com.constellio.model.services.security.authentification.LDAPAuthenticationService;
import com.constellio.model.services.security.authentification.PasswordFileAuthenticationService;
import org.junit.Before;
import org.junit.Test;

public class CombinedAuthenticationServiceAcceptanceTest {

    CombinedAuthenticationService combinedAuthenticationService;
    LDAPConfigurationManager ldapConfigurationManager = mock(LDAPConfigurationManager.class);
    LDAPAuthenticationService ldapAuthenticationService;
    PasswordFileAuthenticationService passwordFileAuthenticationService;

    @Before
    public void setUp()
            throws Exception {
        ldapConfigurationManager = mock(LDAPConfigurationManager.class);
        ldapAuthenticationService = mock(LDAPAuthenticationService.class);
        passwordFileAuthenticationService = mock(PasswordFileAuthenticationService.class);

        combinedAuthenticationService = new CombinedAuthenticationService(ldapConfigurationManager, ldapAuthenticationService, passwordFileAuthenticationService);
    }

    @Test
    public void givenAdminInACollectionWhenAuthenticateThenTryPasswordFileAuthenticate() {
        when(ldapConfigurationManager.isLDAPAuthentication()).thenReturn(true);
        combinedAuthenticationService.authenticate("anAdmin", "password", true);
        verify(ldapAuthenticationService, times(1)).authenticate(anyString(), anyString(), anyBoolean());
        verify(passwordFileAuthenticationService, times(1)).authenticate(anyString(), anyString(), anyBoolean());
    }

    @Test
    public void givenNonAdminWhenAuthenticateThenLdapOnly() {
        when(ldapConfigurationManager.isLDAPAuthentication()).thenReturn(true);
        combinedAuthenticationService.authenticate("anAdmin", "password", false);
        verify(ldapAuthenticationService, times(1)).authenticate(anyString(), anyString(), anyBoolean());
        verify(passwordFileAuthenticationService, never()).authenticate(anyString(), anyString(), anyBoolean());
    }
}
