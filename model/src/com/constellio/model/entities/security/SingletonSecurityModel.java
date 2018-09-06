package com.constellio.model.entities.security;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationDetails;

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

	KeyListMap<String, SecurityModelAuthorization> authorizationsByTargets = new KeyListMap<>();

	List<AuthorizationDetails> authorizationDetails;
	List<User> users;
	List<Group> groups;
	List<String> disabledGroupCodes;
	Taxonomy principalTaxonomy;

	public static SingletonSecurityModel EMPTY = new SingletonSecurityModel(Collections.<AuthorizationDetails>emptyList(),
			Collections.<User>emptyList(), Collections.<Group>emptyList(), FROM_PARENT_TO_CHILD, new ArrayList<String>(), null);

	public SingletonSecurityModel(List<AuthorizationDetails> authorizationDetails, List<User> users,
								  final List<Group> groups,
								  GroupAuthorizationsInheritance groupAuthorizationsInheritance,
								  List<String> disabledGroupCodes, Taxonomy principalTaxonomy) {
		this.authorizationDetails = authorizationDetails;
		this.users = users;
		this.groups = groups;
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
		this.disabledGroupCodes = disabledGroupCodes;
		this.principalTaxonomy = principalTaxonomy;

		for (AuthorizationDetails authorizationDetail : authorizationDetails) {
			boolean conceptAuth = principalTaxonomy != null && principalTaxonomy.getSchemaTypes().contains(authorizationDetail.getTargetSchemaType());
			SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(
					authorizationDetail, conceptAuth, groupAuthorizationsInheritance);
			authorizations.add(securityModelAuthorization);
			authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
			authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);
		}

		for (final Group group : groups) {
			for (String authId : group.<String>getList(Schemas.AUTHORIZATIONS)) {
				SecurityModelAuthorization securityModelAuthorization = authorizationsById.get(authId);
				securityModelAuthorization.addGroup(group);

				//				Provider<String, Group> groupProvider = new Provider<String, Group>() {
				//					@Override
				//					public Group get(String input) {
				//						for (Group group : groups) {
				//							if (group.getId().equals(input)) {
				//								return group;
				//							}
				//						}
				//						return null;
				//					}
				//				};
				//
				//				if (groupAuthorizationsInheritance == GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD) {
				//					for (String aParentGroup : retrieveAllParentGroupIds(group, groupProvider)) {
				//						securityModelAuthorization.addInheritingGroup(aParentGroup);
				//					}
				//				}
			}
		}

		for (User user : users) {
			for (String authId : user.<String>getList(Schemas.AUTHORIZATIONS)) {
				SecurityModelAuthorization securityModelAuthorization = authorizationsById.get(authId);
				securityModelAuthorization.addUser(user);
			}
		}
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
}
