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

	public void addUser(User user) {
		users.add(user);
	}

	public void addGroup(Group group) {
		groups.add(group);
	}

	public List<User> getUsers() {
		return users;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public AuthorizationDetails getDetails() {
		return details;
	}
}
