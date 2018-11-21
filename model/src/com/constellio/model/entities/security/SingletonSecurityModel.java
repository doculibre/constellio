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

	/**
	 * For each group, a list of groups receiving access from it (including self)
	 */
	KeyListMap<String, Group> groupsReceivingAccessToGroup = new KeyListMap<>();


	/**
	 * For each group, a list of groups giving access to it (including self)
	 */
	KeyListMap<String, Group> groupsGivingAccessToGroup = new KeyListMap<>();

	Map<String, Object> principalsById = new HashMap<>();

	List<Authorization> authorizationDetails;
	List<User> users;
	List<Group> groups;
	List<String> disabledGroupCodes;
	Taxonomy principalTaxonomy;
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
		this.collection = collection;

		initUserAndGroupsMaps(users, groups, groupAuthorizationsInheritance);

		initAuthsMaps(authorizationDetails, groupAuthorizationsInheritance, principalTaxonomy);

	}

	protected void initAuthsMaps(List<Authorization> authorizationDetails,
								 GroupAuthorizationsInheritance groupAuthorizationsInheritance,
								 Taxonomy principalTaxonomy) {
		for (Authorization authorizationDetail : authorizationDetails) {
			boolean conceptAuth = principalTaxonomy != null && principalTaxonomy.getSchemaTypes().contains(authorizationDetail.getTargetSchemaType());
			SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(
					authorizationDetail, conceptAuth, groupAuthorizationsInheritance);
			authorizations.add(securityModelAuthorization);
			authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
			authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);

			for (String principalId : authorizationDetail.getPrincipals()) {
				Object principalWrapper = principalsById.get(principalId);
				if (principalWrapper instanceof User) {
					securityModelAuthorization.addUser((User) principalWrapper);
				} else if (principalWrapper instanceof Group) {
					securityModelAuthorization.addGroup((Group) principalWrapper);
				}
				authorizationsByPrincipalId.add(principalId, securityModelAuthorization);
			}
		}
	}

	protected void initUserAndGroupsMaps(List<User> users, List<Group> groups,
										 GroupAuthorizationsInheritance groupAuthorizationsInheritance) {
		for (final Group group : groups) {
			principalsById.put(group.getId(), group);
		}


		for (final Group group : groups) {

			for (String ancestor : group.getAncestors()) {
				if (groupAuthorizationsInheritance == FROM_PARENT_TO_CHILD) {
					groupsGivingAccessToGroup.add(group.getId(), (Group) principalsById.get(ancestor));
					groupsReceivingAccessToGroup.add(ancestor, group);

				} else {
					groupsReceivingAccessToGroup.add(group.getId(), (Group) principalsById.get(ancestor));
					groupsGivingAccessToGroup.add(ancestor, group);
				}

			}
		}


		for (final User user : users) {
			principalsById.put(user.getId(), user);
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
		return groupsReceivingAccessToGroup.get(group.getId());
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


	@Override
	public Object getPrincipalById(String id) {
		return principalsById.get(id);
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId, boolean includeInherited) {
		if (includeInherited) {

			List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();
			for (Group inheritingPrincipal : groupsGivingAccessToGroup.get(principalId)) {
				returnedAuths.addAll(authorizationsByPrincipalId.get(inheritingPrincipal.getId()));
			}

			return returnedAuths;

		} else {
			return authorizationsByPrincipalId.get(principalId);
		}
	}


}
