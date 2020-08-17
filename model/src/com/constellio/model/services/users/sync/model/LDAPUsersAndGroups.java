package com.constellio.model.services.users.sync.model;

import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;

import java.util.HashSet;
import java.util.Set;

public class LDAPUsersAndGroups {
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
