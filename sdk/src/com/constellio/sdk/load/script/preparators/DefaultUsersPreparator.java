package com.constellio.sdk.load.script.preparators;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.XmlUserCredential;
import com.constellio.sdk.load.script.UserPreparator;

public class DefaultUsersPreparator implements UserPreparator {

	List<String> collections;

	int usersCount;

	int groupsCount;

	int sequence;

	public DefaultUsersPreparator(List<String> collections, int usersCount, int groupsCount) {
		this.collections = collections;
		this.usersCount = usersCount;
		this.groupsCount = groupsCount;
	}

	@Override
	public List<GlobalGroup> createGroups() {
		List<GlobalGroup> groups = new ArrayList<>();
		for (int i = 0; i < groupsCount; i++) {
			String code = "" + i;
			String name = "Group '" + code + "'";
			groups.add(new GlobalGroup(code, name, collections, null, GlobalGroupStatus.ACTIVE));
		}

		return groups;
	}

	@Override
	public List<UserCredential> createUsers(List<String> groups) {
		List<UserCredential> userCredentials = new ArrayList<>();

		userCredentials.add(newUser("admin", "admin", "admin", groups));
		for (int i = 0; i < usersCount; i++) {
			String code = "user" + i;
			List<String> userGroups = new ArrayList<>();
			userGroups.addAll(groups);
			userCredentials.add(newUser(code, code, code, userGroups));
		}

		return userCredentials;
	}

	private UserCredential newUser(String username, String firstName, String lastName, List<String> groups) {
		String email = firstName + "." + lastName + "@constellio.com";
		return new XmlUserCredential(username, firstName, lastName, email, groups, collections, UserCredentialStatus.ACTIVE);
	}
}
