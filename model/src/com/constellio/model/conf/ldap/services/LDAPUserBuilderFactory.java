package com.constellio.model.conf.ldap.services;

import javax.naming.NamingException;

import com.constellio.model.conf.ldap.LDAPDirectoryType;
import com.constellio.model.conf.ldap.user.ADUserBuilder;
import com.constellio.model.conf.ldap.user.EdirectoryUserBuilder;
import com.constellio.model.conf.ldap.user.LDAPUserBuilder;

public class LDAPUserBuilderFactory {

	public static LDAPUserBuilder getUserBuilder(LDAPDirectoryType directoryType)
			throws NamingException {
		switch(directoryType){
		case ACTIVE_DIRECTORY:
			return new ADUserBuilder();
		case E_DIRECTORY:
			return new EdirectoryUserBuilder();
		default:
			throw new RuntimeException("Unsupported type " + directoryType);
		}
	}
}
