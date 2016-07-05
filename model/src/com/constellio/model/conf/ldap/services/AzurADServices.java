package com.constellio.model.conf.ldap.services;

import java.util.List;

import com.constellio.model.conf.ldap.config.LDAPServerConfiguration;
import com.constellio.model.conf.ldap.config.LDAPUserSyncConfiguration;
import com.constellio.model.conf.ldap.services.LDAPServicesException.CouldNotConnectToLDAP;

public class AzurADServices implements LDAPServices {
	@Override
	public void authenticateUser(LDAPServerConfiguration ldapServerConfiguration, String user, String password)
			throws CouldNotConnectToLDAP {

	}

	@Override
	public List<String> getTestSynchronisationUsersNames(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		return null;
	}

	@Override
	public List<String> getTestSynchronisationGroups(LDAPServerConfiguration ldapServerConfiguration,
			LDAPUserSyncConfiguration ldapUserSyncConfiguration) {
		return null;
	}

}
