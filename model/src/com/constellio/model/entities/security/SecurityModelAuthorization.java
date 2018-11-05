package com.constellio.model.entities.security;

import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.List;

public class SecurityModelAuthorization {

	List<User> users = new ArrayList<>();

	List<Group> groups = new ArrayList<>();

	Authorization details;

	GroupAuthorizationsInheritance groupAuthorizationsInheritance;

	boolean conceptAuth;

	private SecurityModelAuthorization(GroupAuthorizationsInheritance groupAuthorizationsInheritance) {
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
	}

	public SecurityModelAuthorization(Authorization details,
									  boolean conceptAuth,
									  GroupAuthorizationsInheritance groupAuthorizationsInheritance) {
		this.details = details;
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

	public List<String> getPrincipalIds() {
		List<String> principalIds = new ArrayList<>();

		for (Group group : groups) {
			principalIds.add(group.getId());
		}

		if (groupAuthorizationsInheritance != GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD) {
			for (Group group : groups) {
				for (String ancestor : group.getAncestors()) {
					if (!principalIds.contains(ancestor)) {
						principalIds.add(ancestor);
					}
				}
			}
		}

		for (User user : users) {
			principalIds.add(user.getId());
		}

		return principalIds;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public Authorization getDetails() {
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

	public boolean isConceptAuth() {
		return conceptAuth;
	}

	public static SecurityModelAuthorization wrapNewAuthWithoutUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			Taxonomy principalTaxonomy,
			Authorization details) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>();
		auth.groups = new ArrayList<>();
		auth.conceptAuth = principalTaxonomy != null
						   && principalTaxonomy.getSchemaTypes().contains(details.getTargetSchemaType());

		auth.details = details;

		return auth;
	}

	public static SecurityModelAuthorization wrapExistingAuthUsingModifiedUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			Taxonomy principalTaxonomy,
			Authorization details,
			List<User> existingUsers,
			List<Group> existingGroups) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>();
		auth.groups = new ArrayList<>();
		auth.conceptAuth = principalTaxonomy != null
						   && principalTaxonomy.getSchemaTypes().contains(details.getTargetSchemaType());


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
