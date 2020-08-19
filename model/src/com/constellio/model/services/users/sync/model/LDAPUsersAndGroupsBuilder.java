package com.constellio.model.services.users.sync.model;

import com.constellio.model.conf.ldap.user.LDAPGroup;
import com.constellio.model.conf.ldap.user.LDAPUser;

import java.util.HashSet;
import java.util.Set;

public class LDAPUsersAndGroupsBuilder {
	Set<LDAPGroup> groups = new HashSet<>();
	private Set<LDAPUser> users = new HashSet<>();

	public LDAPUsersAndGroupsBuilder add(LDAPGroup group) {
		groups.add(group);
		return this;
	}

	public LDAPUsersAndGroupsBuilder add(LDAPUser user) {
		users.add(user);
		return this;
	}

	public LDAPUsersAndGroupsBuilder add(LDAPGroup... newGroups) {
		for (LDAPGroup group : newGroups) {
			groups.add(group);
		}
		return this;
	}

	public LDAPUsersAndGroupsBuilder add(LDAPUser... newUsers) {
		for (LDAPUser user : newUsers) {
			users.add(user);
		}
		return this;
	}


	public LDAPUsersAndGroups build() {
		return new LDAPUsersAndGroups(users, groups);
	}

}
