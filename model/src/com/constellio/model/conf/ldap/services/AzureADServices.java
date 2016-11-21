package com.constellio.model.conf.ldap.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;

public class AzureADServices implements LDAPServices {
	private static final Logger LOGGER = LoggerFactory.getLogger(LDAPServicesImpl.class);

	@Override
	public void authenticateUser(LDAPServerConfiguration ldapServerConfiguration, String user, String password)
			throws CouldNotConnectUserToLDAP {
        new AzureAdClient(ldapServerConfiguration, null).authenticiate(user, password);
	}

	@Override
	public List<String> getTestSynchronisationUsersNames(final LDAPServerConfiguration ldapServerConfiguration,
			final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		Set<String> results = new HashSet<>();

        try (final AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration)) {
            azureAdClient.init();

            results = azureAdClient.getUserNameList();
        } catch (final Throwable t) {
            LOGGER.error("Unexpected error in users synchronization dry run", t);
        }

		return new ArrayList<>(results);
	}

	@Override
	public List<String> getTestSynchronisationGroups(final LDAPServerConfiguration ldapServerConfiguration,
			final LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		Set<String> results = new HashSet<>();

        try (final AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration)) {
            azureAdClient.init();

            results = azureAdClient.getGroupNameList();
        } catch (final Throwable t) {
            LOGGER.error("Unexpected error in groups synchronization dry run", t);
        }

		return new ArrayList<>(results);
	}

	@Override
	public LDAPUsersAndGroups importUsersAndGroups(final LDAPServerConfiguration ldapServerConfiguration,
			final LDAPUserSyncConfiguration ldapUserSyncConfiguration, final String url) {
		final Map<String, LDAPGroup> ldapGroups = new HashMap<>();
        final Map<String, LDAPUser> ldapUsers = new HashMap<>();

        try (final AzureAdClient azureAdClient = new AzureAdClient(ldapServerConfiguration, ldapUserSyncConfiguration)) {
            azureAdClient.init();

            azureAdClient.getGroupsAndTheirUsers(ldapGroups, ldapUsers);

            azureAdClient.getUsersAndTheirGroups(ldapGroups, ldapUsers);
        }

		return new LDAPUsersAndGroups(new HashSet<>(ldapUsers.values()), new HashSet<>(ldapGroups.values()));
	}

}
