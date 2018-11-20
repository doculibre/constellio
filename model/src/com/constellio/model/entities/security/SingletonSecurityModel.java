package com.constellio.model.entities.security;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.model.entities.enums.GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD;

public class SingletonSecurityModel implements SecurityModel {

	GroupAuthorizationsInheritance groupAuthorizationsInheritance;

	List<SecurityModelAuthorization> authorizations = new ArrayList<>();

	Map<String, SecurityModelAuthorization> authorizationsById = new HashMap<>();

	KeyListMap<String, SecurityModelAuthorization> authorizationsByPrincipalId = new KeyListMap<>();

	KeyListMap<String, SecurityModelAuthorization> authorizationsByTargets = new KeyListMap<>();

	List<Authorization> authorizationDetails;
	List<User> users;
	List<Group> groups;
	List<String> disabledGroupCodes;
	Taxonomy principalTaxonomy;
	//RecordProvider recordProvider;
	String collection;

	public static SingletonSecurityModel empty(String collection) {

		return new SingletonSecurityModel(Collections.<Authorization>emptyList(),
				Collections.<User>emptyList(), Collections.<Group>emptyList(), FROM_PARENT_TO_CHILD, new ArrayList<String>(), null, collection);
	}

	public SingletonSecurityModel(List<Authorization> authorizationDetails, List<User> users,
								  final List<Group> groups,
								  GroupAuthorizationsInheritance groupAuthorizationsInheritance,
								  List<String> disabledGroupCodes, Taxonomy principalTaxonomy,
								  String collection) {
		this.authorizationDetails = authorizationDetails;
		this.users = users;
		this.groups = groups;
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
		this.disabledGroupCodes = disabledGroupCodes;
		this.principalTaxonomy = principalTaxonomy;
		//this.recordProvider = recordProvider;
		this.collection = collection;

		Map<String, Object> groupsAndUsersMap = new HashMap<>();
		for (final Group group : groups) {
			groupsAndUsersMap.put(group.getId(), group);
		}

		for (final User user : users) {
			groupsAndUsersMap.put(user.getId(), user);
		}

		for (Authorization authorizationDetail : authorizationDetails) {
			boolean conceptAuth = principalTaxonomy != null && principalTaxonomy.getSchemaTypes().contains(authorizationDetail.getTargetSchemaType());
			SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(
					authorizationDetail, conceptAuth, groupAuthorizationsInheritance);
			authorizations.add(securityModelAuthorization);
			authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
			authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);

			for (String principalId : authorizationDetail.getPrincipals()) {
				Object principalWrapper = groupsAndUsersMap.get(principalId);
				if (principalWrapper instanceof User) {
					securityModelAuthorization.addUser((User) principalWrapper);
				} else if (principalWrapper instanceof Group) {
					securityModelAuthorization.addGroup((Group) principalWrapper);
				}
				authorizationsByPrincipalId.add(principalId, securityModelAuthorization);
			}
		}

	}

	public String getCollection() {
		return collection;
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsOnTarget(String recordId) {
		return authorizationsByTargets.get(recordId);
	}

	@Override
	public SecurityModelAuthorization getAuthorizationWithId(String authId) {
		return authorizationsById.get(authId);
	}

	public List<User> getUsers() {
		return users;
	}

	public List<Group> getGroups() {
		return groups;
	}

	public List<Group> getGroupsInheritingAuthorizationsFrom(Group group) {

		if (groupAuthorizationsInheritance == GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD) {
			return getGroupHierarchy(group);

		} else {
			List<Group> returnedGroups = new ArrayList<>();
			for (Group aGroup : groups) {
				if (group.getAncestors().contains(aGroup.getId())) {
					returnedGroups.add(aGroup);
				}
			}
			return returnedGroups;
		}

	}

	@Override
	public boolean isGroupActive(Group group) {
		return !disabledGroupCodes.contains(group.getCode());
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity) {

		return SecurityModelUtils.getAuthorizationDetailsOnMetadatasProvidingSecurity(metadatasProvidingSecurity, this);

	}

	private List<Group> getGroupHierarchy(Group group) {
		List<Group> groupsHierarchy = new ArrayList<>();

		for (Group aGroup : groups) {
			if (aGroup.getParent() != null && aGroup.getParent().equals(group.getId())) {
				if (!disabledGroupCodes.contains(aGroup.getCode())) {
					groupsHierarchy.add(aGroup);
					groupsHierarchy.addAll(getGroupHierarchy(aGroup));
				}
			}
		}

		return groupsHierarchy;

	}


	public GroupAuthorizationsInheritance getGroupAuthorizationsInheritance() {
		return groupAuthorizationsInheritance;
	}

	@Override
	public Object getPrincipalById(String id) {
		for (User user : users) {
			if (user.getId().equals(id)) {
				return user;
			}
		}

		for (Group group : groups) {
			if (group.getId().equals(id)) {
				return group;
			}
		}

		return null;
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId) {
		return authorizationsByPrincipalId.get(principalId);
	}

}
