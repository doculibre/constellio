package com.constellio.model.conf.ldap.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;

public interface LDAPServices {

	void authenticateUser(LDAPServerConfiguration ldapServerConfiguration, String user, String password)
			throws CouldNotConnectUserToLDAP;

	List<String> getTestSynchronisationUsersNames(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration);

	List<String> getTestSynchronisationGroups(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration);

	LDAPUsersAndGroups importUsersAndGroups(LDAPServerConfiguration serverConfiguration,
			LDAPUserSyncConfiguration userSyncConfiguration, String url);

	class LDAPUsersAndGroups {
		Set<LDAPGroup> groups = new HashSet<>();
		private Set<LDAPUser> users = new HashSet<>();

		public LDAPUsersAndGroups(Set<LDAPUser> ldapUsers, Set<LDAPGroup> ldapGroups) {
			this.users.addAll(ldapUsers);
			this.groups.addAll(ldapGroups);
		}

		public Set<LDAPGroup> getGroups() {
			return groups;
		}

		public Set<LDAPUser> getUsers() {
			return users;
		}
	}
}
