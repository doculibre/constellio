package com.constellio.model.entities.security;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.AuthorizationDetails;

public class SecurityModelAuthorization {

	List<User> users = new ArrayList<>();

	List<Group> groups = new ArrayList<>();

	AuthorizationDetails details;

	public SecurityModelAuthorization(AuthorizationDetails details) {
		this.users = users;
		this.groups = groups;
		this.details = details;
	}

	public SecurityModelAuthorization(SecurityModelAuthorization cloned) {
		this.users = new ArrayList<>(cloned.users);
		this.groups = new ArrayList<>(cloned.groups);
		this.details = cloned.details;
	}

	public void addUser(User user) {
		users.add(user);
	}

	public void addGroup(Group group) {
		groups.add(group);
	}

	public List<User> getUsers() {
		return users;
	}

	public List<String> getPrincipalIds() {
		List<String> principalIds = new ArrayList<>();
		for (User user : users) {
			principalIds.add(user.getId());
		}

		for (Group group : groups) {
			principalIds.add(group.getId());
		}

		return principalIds;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public AuthorizationDetails getDetails() {
		return details;
	}

	public void removeUser(User user) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUsername().equals(user.getUsername())) {
				users.remove(i);
				break;
			}
		}
	}

	public void removeGroup(Group group) {
		for (int i = 0; i < groups.size(); i++) {
			if (groups.get(i).getCode().equals(group.getCode())) {
				groups.remove(i);
				break;
			}
		}
	}
}
