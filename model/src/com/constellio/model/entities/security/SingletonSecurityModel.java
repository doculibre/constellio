package com.constellio.model.entities.security;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

	Map<String, Group> groupsById = new HashMap<>();

	List<Group> groups;
	List<String> disabledGroupCodes;
	String collection;
	List<String> securableRecordSchemaTypes;

	static int REFRESH_COUNTER = 0;

	public static SingletonSecurityModel empty(String collection) {

		return new SingletonSecurityModel(Collections.<Authorization>emptyList(),
				Collections.<Group>emptyList(), FROM_PARENT_TO_CHILD, Collections.<String>emptyList(), Collections.<String>emptyList(), collection);
	}

	public SingletonSecurityModel(List<Authorization> authorizationDetails, final List<Group> groups,
								  GroupAuthorizationsInheritance groupAuthorizationsInheritance,
								  List<String> disabledGroupCodes, List<String> securableRecordSchemaTypes,
								  String collection) {
		System.out.println("Security model refresh #" + ++REFRESH_COUNTER);
		this.groups = groups;
		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
		this.disabledGroupCodes = disabledGroupCodes;
		this.collection = collection;
		this.securableRecordSchemaTypes = securableRecordSchemaTypes;
		initGroupsMaps(groups);
		initAuthsMaps(authorizationDetails);
	}

	protected void initAuthsMaps(List<Authorization> authorizationDetails) {
		for (Authorization authorizationDetail : authorizationDetails) {
			insertAuthorizationInMemoryMaps(authorizationDetail);
		}
	}

	private void insertAuthorizationInMemoryMaps(Authorization authorizationDetail) {
		boolean securableRecord = securableRecordSchemaTypes.contains(authorizationDetail.getTargetSchemaType());
		SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(
				authorizationDetail, securableRecord, groupAuthorizationsInheritance);
		authorizations.add(securityModelAuthorization);
		authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
		authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);

		for (String principalId : authorizationDetail.getPrincipals()) {
			Group group = groupsById.get(principalId);
			if (group != null) {
				securityModelAuthorization.addGroup(group);
			} else {
				securityModelAuthorization.addUserId(principalId);
			}
			authorizationsByPrincipalId.add(principalId, securityModelAuthorization);
		}
	}

	protected void initGroupsMaps(List<Group> groups) {
		for (final Group group : groups) {
			groupsById.put(group.getId(), group);
		}

		for (final Group group : groups) {

			for (String ancestor : group.getAncestors()) {
				if (groupAuthorizationsInheritance == FROM_PARENT_TO_CHILD) {
					groupsGivingAccessToGroup.add(group.getId(), (Group) groupsById.get(ancestor));
					groupsReceivingAccessToGroup.add(ancestor, group);

				} else {
					groupsReceivingAccessToGroup.add(group.getId(), (Group) groupsById.get(ancestor));
					groupsGivingAccessToGroup.add(ancestor, group);
				}

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
	public Group getPrincipalById(String id) {
		return groupsById.get(id);
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

	private void removeAuthWithId(String authId, List<SecurityModelAuthorization> auths) {
		Iterator<SecurityModelAuthorization> authsIterator = auths.iterator();
		while (authsIterator.hasNext()) {
			if (authId.equals(authsIterator.next().getDetails().getId())) {
				authsIterator.remove();
				break;
			}
		}

	}

	public synchronized void removeAuth(String authId) {
		removeAuthWithId(authId, authorizations);

		SecurityModelAuthorization auth = authorizationsById.remove(authId);

		for (String userId : auth.getUserIds()) {
			removeAuthWithId(authId, authorizationsByPrincipalId.get(userId));
		}

		for (Group group : auth.getGroups()) {
			removeAuthWithId(authId, authorizationsByPrincipalId.get(group.getId()));
		}

		removeAuthWithId(authId, authorizationsByTargets.get(auth.getDetails().getTarget()));
	}

	public synchronized void updateCache(List<Authorization> newAuths, List<Authorization> modifiedAuths) {
		for (Authorization auth : newAuths) {
			insertAuthorizationInMemoryMaps(auth);
		}

		for (Authorization auth : modifiedAuths) {
			removeAuth(auth.getId());
		}

		for (Authorization auth : modifiedAuths) {
			insertAuthorizationInMemoryMaps(auth);
		}
	}
}
