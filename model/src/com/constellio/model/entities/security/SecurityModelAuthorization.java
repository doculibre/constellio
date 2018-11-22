package com.constellio.model.entities.security;

import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.List;

public class SecurityModelAuthorization {

	private List<User> users = new ArrayList<>();

	private List<Group> groups = new ArrayList<>();

	private Authorization details;

	private boolean securableRecord;

	private GroupAuthorizationsInheritance groupAuthorizationsInheritance;

	private SecurityModelAuthorization(GroupAuthorizationsInheritance groupAuthorizationsInheritance) {
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
	}

	public SecurityModelAuthorization(Authorization details,
									  boolean securableRecord,
									  GroupAuthorizationsInheritance groupAuthorizationsInheritance) {
		this.details = details;
		this.securableRecord = securableRecord;
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
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

	public Authorization getDetails() {
		return details;
	}

	public boolean isSecurableRecord() {
		return securableRecord;
	}

	static SecurityModelAuthorization wrapNewAuthWithoutUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			boolean securableRecord,
			Authorization details) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>();
		auth.groups = new ArrayList<>();
		auth.securableRecord = securableRecord;

		auth.details = details;

		return auth;
	}

	static SecurityModelAuthorization wrapExistingAuthUsingModifiedUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			boolean securableRecord,
			Authorization details,
			List<User> existingUsers,
			List<Group> existingGroups) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>();
		auth.groups = new ArrayList<>();
		auth.securableRecord = securableRecord;


		for (User user : existingUsers) {
			if (details.getPrincipals().contains(user.getId())) {
				auth.users.add(user);
			}
		}

		for (Group group : existingGroups) {
			if (details.getPrincipals().contains(group.getId())) {
				auth.groups.add(group);
			}
		}

		auth.details = details;
		return auth;
	}


	public String toString() {
		List<String> printablePrincipals = new ArrayList<>();
		for (Group group : groups) {
			printablePrincipals.add(group.getCode());
		}

		for (User user : users) {
			printablePrincipals.add(user.getUsername());
		}

		return "Giving " + (details.isNegative() ? "negative " : "") + details.getRoles() + " to " + printablePrincipals + " on " + details.getTarget() + " (" + details.getTargetSchemaType() + ")";
	}
}
