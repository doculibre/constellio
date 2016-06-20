package com.constellio.model.conf.ldap.services;

import com.constellio.model.conf.ldap.LDAPDirectoryType;

public class LDAPServicesFactory {
	public static LDAPServices newLDAPServices(LDAPDirectoryType directoryType) {
		switch (directoryType) {
		case E_DIRECTORY:
		case ACTIVE_DIRECTORY:
			return new LDAPServicesImpl();
		case AZUR_AD:
			return new AzurADServices();
		default:
			throw new RuntimeException("Unsupported type " + directoryType);
		}
	}
}
