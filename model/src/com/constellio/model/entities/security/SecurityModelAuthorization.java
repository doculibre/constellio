package com.constellio.model.entities.security;

import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.services.records.RecordId;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SecurityModelAuthorization {

	private List<String> userIds = new ArrayList<>();

	private List<String> groupIds = new ArrayList<>();

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

	public RecordId getTargetRecordId() {
		return RecordId.toId(details.getTarget());
	}

	public void addUserId(String userId) {
		userIds.add(userId);
	}

	public List<String> getUserIds() {
		return userIds;
	}

	public void addGroupId(String groupId) {
		groupIds.add(groupId);
	}

	public List<String> getGroupIds() {
		return groupIds;
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
		auth.groupIds = new ArrayList<>();
		auth.securableRecord = securableRecord;

		auth.details = details;

		return auth;
	}

	static SecurityModelAuthorization wrapExistingAuthUsingModifiedUsersAndGroups(
			GroupAuthorizationsInheritance groupAuthorizationsInheritance,
			boolean securableRecord,
			Authorization details,
			Set<String> existingGroups) {

		SecurityModelAuthorization auth = new SecurityModelAuthorization(groupAuthorizationsInheritance);
		auth.userIds = new ArrayList<>();
		auth.groupIds = new ArrayList<>();
		auth.securableRecord = securableRecord;

		for (String userId : details.getPrincipals()) {
			if (existingGroups.contains(userId)) {
				auth.groupIds.add(userId);
			} else {
				auth.userIds.add(userId);
			}
		}

		auth.details = details;
		return auth;
	}


	public String toString() {
		List<String> printablePrincipals = new ArrayList<>();
		printablePrincipals.addAll(groupIds);
		printablePrincipals.addAll(userIds);

		return "Giving " + (details.isNegative() ? "negative " : "") + details.getRoles() + " to " + printablePrincipals + " on " + details.getTarget() + " (" + details.getTargetSchemaType() + ")";
	}
}
