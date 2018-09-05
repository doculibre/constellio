package com.constellio.model.entities.security;

import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationDetails;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecurityModelAuthorization {

	List<User> users = new ArrayList<>();

	List<Group> groups = new ArrayList<>();

	AuthorizationDetails details;

	GroupAuthorizationsInheritance groupAuthorizationsInheritance;

	private SecurityModelAuthorization(GroupAuthorizationsInheritance groupAuthorizationsInheritance) {
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
	}

	public SecurityModelAuthorization(AuthorizationDetails details,
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

		if (groupAuthorizationsInheritance == GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD) {

		} else {
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

	public static SecurityModelAuthorization wrapNewAuthUsingModifiedUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			AuthorizationDetails details,
			List<User> modifiedUsersInTransaction,
			List<Group> modifiedGroupsInTransaction) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>();
		auth.groups = new ArrayList<>();
		for (User user : modifiedUsersInTransaction) {
			if (user.getUserAuthorizations().contains(details.getId())) {
				auth.users.add(user);
			}
		}

		for (Group group : modifiedGroupsInTransaction) {
			if (group.getList(Schemas.AUTHORIZATIONS).contains(details.getId())) {
				auth.groups.add(group);
			}
		}

		auth.details = details;
		return auth;
	}

	public static SecurityModelAuthorization wrapExistingAuthUsingModifiedUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			AuthorizationDetails details,
			List<User> existingUsers,
			List<Group> existingGroups,
			List<User> modifiedUsersInTransaction,
			List<Group> modifiedGroupsInTransaction) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>();
		auth.groups = new ArrayList<>();

		Set<String> modifiedPrincipals = new HashSet<>();
		for (User user : modifiedUsersInTransaction) {
			if (user.getUserAuthorizations().contains(details.getId())) {
				auth.users.add(user);
			}
			modifiedPrincipals.add(user.getId());
		}

		for (Group group : modifiedGroupsInTransaction) {
			if (group.getList(Schemas.AUTHORIZATIONS).contains(details.getId())) {
				auth.groups.add(group);
			}
			modifiedPrincipals.add(group.getId());
		}

		for (User user : existingUsers) {
			if (!modifiedPrincipals.contains(user.getId()) && user.getUserAuthorizations().contains(details.getId())) {
				auth.users.add(user);
			}
		}

		for (Group group : existingGroups) {
			if (!modifiedPrincipals.contains(group.getId())
				&& group.getList(Schemas.AUTHORIZATIONS).contains(details.getId())) {
				auth.groups.add(group);

			}
		}

		auth.details = details;
		return auth;
	}

	public static SecurityModelAuthorization cloneWithModifiedUserAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			SecurityModelAuthorization cloned,
			List<User> modifiedUsersInTransaction,
			List<Group> modifiedGroupsInTransaction) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.users = new ArrayList<>(cloned.users);
		auth.groups = new ArrayList<>(cloned.groups);
		auth.details = cloned.details;

		for (User user : modifiedUsersInTransaction) {
			if (user.getUserAuthorizations().contains(auth.details.getId())) {
				auth.users.add(user);
			}
		}

		for (Group group : modifiedGroupsInTransaction) {
			if (group.getList(Schemas.AUTHORIZATIONS).contains(auth.details.getId())) {
				auth.groups.add(group);
			}
		}

		return auth;
	}
}
