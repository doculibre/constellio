package com.constellio.model.conf.ldap.user;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

public interface LDAPUserBuilder {

	String[] getFetchedAttributes();
	String getUserIdAttribute();
	LDAPUser buildUser(String userId, Attributes attrs) throws NamingException;
}
