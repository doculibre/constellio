package com.constellio.model.conf.ldap.services;

import java.util.List;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectToLDAP;

public interface LDAPServices {
	void authenticateUser(LDAPServerConfiguration ldapServerConfiguration, String user, String password)
			throws CouldNotConnectToLDAP;

	List<String> getTestSynchronisationUsersNames(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration);

	List<String> getTestSynchronisationGroups(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration);
}
