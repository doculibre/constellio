package com.constellio.model.conf.ldap.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.constellio.model.conf.ldap.RegexFilter;
import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 */
public class AzureAdClientTest extends ConstellioTest {

    @Mock
    LDAPServerConfiguration ldapServerConfiguration;

    @Mock
    LDAPUserSyncConfiguration ldapUserSyncConfiguration;

    @Before
    public void setUp()
            throws Exception {
        when(ldapServerConfiguration.getClientId()).thenReturn("c5df7384-1ba3-4683-afc3-b33c7411f693");
        when(ldapServerConfiguration.getTenantName()).thenReturn("adgrics.onmicrosoft.com");
        when(ldapUserSyncConfiguration.getClientId()).thenReturn("b514f145-024b-4693-8b6a-5ea311beae85");
        when(ldapUserSyncConfiguration.getClientSecret()).thenReturn("12phVwr/mGXQaWtCWeq1Sm0SgTfXlgwa3i3q3/Gqguc=");

        when(ldapUserSyncConfiguration.isGroupAccepted(any("".getClass()))).thenReturn(true);
        when(ldapUserSyncConfiguration.isUserAccepted(any("".getClass()))).thenReturn(true);
    }

    @Test
    public void testGetUserNameList() throws Exception {
        AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration);
        azureAdClient.init();

        final Set<String> userNameList = azureAdClient.getUserNameList();

        assertThat(userNameList).isNotEmpty();
    }

    @Test
    public void testGetGroupNameList() throws Exception {
        AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration);
        azureAdClient.init();

        final Set<String> groupNameList = azureAdClient.getGroupNameList();

        assertThat(groupNameList).isNotEmpty();
    }

    @Test
    public void testGetGroupsAndTheirUsers() throws Exception {
        AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration);
        azureAdClient.init();

        final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
        final Map<String, LDAPUser> ldapUsers = new HashMap<>();

        azureAdClient.getGroupsAndTheirUsers(ldapGroups, ldapUsers);

        assertThat(ldapGroups).isNotEmpty();
        assertThat(ldapUsers).isNotEmpty();
    }

    @Test
    public void testGetUsersAndTheirGroups() throws Exception {
        AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration);
        azureAdClient.init();

        final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
        final Map<String, LDAPUser> ldapUsers = new HashMap<>();

        azureAdClient.getUsersAndTheirGroups(ldapGroups, ldapUsers);

        assertThat(ldapGroups).isNotEmpty();
        assertThat(ldapUsers).isNotEmpty();
    }

    @Test
    public void testAuthenticiate() throws Exception {
        new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration).authenticiate("user1@adgrics.onmicrosoft.com", "Comu12980");
    }
}