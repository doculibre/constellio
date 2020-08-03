package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.LDAPDirectoryType;

public class LDAPServicesFactory {
	public LDAPServices newLDAPServices(LDAPDirectoryType directoryType) {
		switch (directoryType) {
			case E_DIRECTORY:
			case ACTIVE_DIRECTORY:
				return new LDAPServicesImpl();
			case AZURE_AD:
				return new AzureADServices();
			default:
				throw new RuntimeException("Unsupported type " + directoryType);
		}
	}
}
