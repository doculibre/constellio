package com.constellio.app.modules.es.connectors.ldap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

public class LDAPObjectAttributes {
	Map<String, LDAPObjectAttribute> ldapObjects = new HashMap<>();

	public LDAPObjectAttributes(Attributes attrs) {
		if (attrs != null) {
			NamingEnumeration<? extends Attribute> all = attrs.getAll();
			while (all.hasMoreElements()) {
				try {
					Attribute att;
					LDAPObjectAttribute attribute = new LDAPObjectAttribute(att = all.next());
					ldapObjects.put(att.getID(), attribute);
				} catch (NamingException e) {
					//TODO
					throw new RuntimeException(e);
				}
			}
		}
	}

	public LDAPObjectAttributes() {
	}

	public void addAttribute(String key, LDAPObjectAttribute attribute) {
		ldapObjects.put(key, attribute);
	}

	public LDAPObjectAttribute get(String key) {
		return ldapObjects.get(key);
	}
}
