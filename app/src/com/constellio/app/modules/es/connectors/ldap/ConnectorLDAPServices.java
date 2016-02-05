package com.constellio.app.modules.es.connectors.ldap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

import com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType;
import com.constellio.model.conf.ldap.RegexFilter;

public interface ConnectorLDAPServices {
	ConnectorLDAPSearchResult getAllObjectsUsingFilter(LdapContext ctx, String objectClass, String objectCategory, Set<String> searchContextsNames, RegexFilter filter);

	Map<String, LDAPObjectAttributes> getObjectsAttributes(LdapContext ctx, Set<String> objectsIds);

	LDAPObjectAttributes getObjectAttributes(LdapContext ctx, String objectsId);

	LdapContext connectToLDAP(String url, String user, String password, Boolean followReferences,
			boolean activeDirectory);

	boolean isObjectEnabled(LDAPObjectAttributes object, DirectoryType directoryType);

}
