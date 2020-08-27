package com.constellio.model.services.users.sync.model;

import java.util.HashSet;
import java.util.Set;

public class UpdatedUsersAndGroups {

	public UpdatedUsersAndGroups() {
	}

	public UpdatedUsersAndGroups(Set<String> usersNames, Set<String> groupsCodes) {
		this.usersNames = usersNames;
		this.groupsCodes = groupsCodes;
	}

	private Set<String> usersNames = new HashSet<>();
	private Set<String> groupsCodes = new HashSet<>();

	public void addUsername(String username) {
		usersNames.add(username);
	}

	public Set<String> getUsersNames() {
		return usersNames;
	}

	public Set<String> getGroupsCodes() {
		return groupsCodes;
	}

	public void addGroupCode(String groupCode) {
		groupsCodes.add(groupCode);
	}
}