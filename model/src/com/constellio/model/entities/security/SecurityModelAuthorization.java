package com.constellio.model.entities.security;

import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;

import java.util.ArrayList;
import java.util.List;

public class SecurityModelAuthorization {

	private List<String> userIds = new ArrayList<>();

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

	public void addUserId(String userId) {
		userIds.add(userId);
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void addGroup(Group group) {
		groups.add(group);
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
		auth.userIds = new ArrayList<>();
		auth.groups = new ArrayList<>();
		auth.securableRecord = securableRecord;

		auth.details = details;

		return auth;
	}

	static SecurityModelAuthorization wrapExistingAuthUsingModifiedUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			boolean securableRecord,
			Authorization details,
			List<Group> existingGroups) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.userIds = new ArrayList<>();
		auth.groups = new ArrayList<>();
		auth.securableRecord = securableRecord;

		for (Group group : existingGroups) {
			if (details.getPrincipals().contains(group.getId())) {
				auth.groups.add(group);
			}
		}

		for (String userId : details.getPrincipals()) {
			boolean found = false;
			for (Group group : auth.groups) {
				if (group.getId().equals(userId)) {
					found = true;
					break;
				}
			}

			if (!found) {
				auth.userIds.add(userId);
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

		printablePrincipals.addAll(userIds);

		return "Giving " + (details.isNegative() ? "negative " : "") + details.getRoles() + " to " + printablePrincipals + " on " + details.getTarget() + " (" + details.getTargetSchemaType() + ")";
	}
}
