package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 *
 */
@InternetTest
public class AzureAdClientAcceptanceTest extends ConstellioTest {

	@Mock
	LDAPServerConfiguration ldapServerConfiguration;

	@Mock
	LDAPUserSyncConfiguration ldapUserSyncConfiguration;

	@Mock
	AzureRequestHelper azureRequestHelper;

	@Before
	public void setUp()
			throws Exception {
		when(ldapServerConfiguration.getClientId()).thenReturn("69ab5806-25cf-4d80-a818-5b7cb7df1681");
		when(ldapServerConfiguration.getTenantName()).thenReturn("adgrics.onmicrosoft.com");
		when(ldapUserSyncConfiguration.getClientId()).thenReturn("bec3eab8-7c58-4263-b439-71ae66faa656");
		when(ldapUserSyncConfiguration.getClientSecret()).thenReturn("");

		when(ldapUserSyncConfiguration.isGroupAccepted(any("".getClass()))).thenReturn(true);
		when(ldapUserSyncConfiguration.isUserAccepted(any("".getClass()))).thenReturn(true);
	}

	@Test
	@InDevelopmentTest
	public void testGetUserNameList()
			throws Exception {
		AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration);

		final Set<String> results = azureAdClient.getUserNameList();

		assertThat(results).isNotEmpty();
		assertThat(results.size()).isEqualTo(21);
	}

	@Test
	@InDevelopmentTest
	public void testGetGroupNameList()
			throws Exception {
		AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration);

		final Set<String> results = azureAdClient.getGroupNameList();

		assertThat(results).isNotEmpty();
		assertThat(results.size()).isEqualTo(3);
	}

	@Test
	@InDevelopmentTest
	public void testGetGroupsAndTheirUsers()
			throws Exception {
		azureRequestHelper.maxResults = 1;

		AzureAdClient azureAdClientSpy = spy(new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration));

		final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
		final Map<String, LDAPUser> ldapUsers = new HashMap<>();

		azureAdClientSpy.getGroupsAndTheirUsers(ldapGroups, ldapUsers);

		assertThat(ldapGroups).isNotEmpty();
		assertThat(ldapGroups.size()).isEqualTo(3);
		assertThat(ldapUsers).isNotEmpty();
		assertThat(ldapUsers.size()).isEqualTo(20);
	}

	@Test
	@InDevelopmentTest
	public void testGetUsersAndTheirGroups()
			throws Exception {
		azureRequestHelper.maxResults = 2;

		AzureAdClient azureAdClientSpy = spy(new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration));

		final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
		final Map<String, LDAPUser> ldapUsers = new HashMap<>();

		azureAdClientSpy.getUsersAndTheirGroups(ldapGroups, ldapUsers);

		assertThat(ldapGroups).isNotEmpty();
		assertThat(ldapGroups.size()).isEqualTo(3);
		assertThat(ldapUsers).isNotEmpty();
		//user 3 is without groups
		assertThat(ldapUsers.size()).isEqualTo(21);
	}

	@Test
	@InDevelopmentTest
	public void testGetGroupsAndSubGroupsAndTheirUsers()
			throws Exception {
		azureRequestHelper.maxResults = 2;

		AzureAdClient azureAdClientSpy = spy(new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration));

		final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
		final Map<String, LDAPUser> ldapUsers = new HashMap<>();

		azureAdClientSpy.getUsersAndTheirGroups(ldapGroups, ldapUsers);

		assertThat(ldapGroups).isNotEmpty();
		assertThat(ldapGroups.size()).isEqualTo(3);
		assertThat(ldapUsers).isNotEmpty();
		//user 3 is without groups
		assertThat(ldapUsers.size()).isEqualTo(21);
	}

	@Test
	@InDevelopmentTest
	public void testAuthenticiate()
			throws Exception {
		new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration)
				.authenticate("user1@adgrics.onmicrosoft.com", "Comu12980");
	}
}