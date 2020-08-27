package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectUserToLDAP;
import com.constellio.model.services.users.sync.model.LDAPUsersAndGroups;

import java.util.List;

public interface LDAPServices {

	void authenticateUser(LDAPServerConfiguration ldapServerConfiguration, String user, String password)
			throws CouldNotConnectUserToLDAP;

	List<String> getTestSynchronisationUsersNames(LDAPServerConfiguration ldapServerConfiguration,
												  LDAPUserSyncConfiguration ldapUserSyncConfiguration);

	List<String> getTestSynchronisationGroups(LDAPServerConfiguration ldapServerConfiguration,
											  LDAPUserSyncConfiguration ldapUserSyncConfiguration);

	LDAPUsersAndGroups importUsersAndGroups(LDAPServerConfiguration serverConfiguration,
											LDAPUserSyncConfiguration userSyncConfiguration, String url);
}
