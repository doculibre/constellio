package com.constellio.model.entities.security;

import com.constellio.data.utils.KeyListMap;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SingletonSecurityModel implements SecurityModel {

	GroupAuthorizationsInheritance groupAuthorizationsInheritance;

	List<SecurityModelAuthorization> authorizations = new ArrayList<>();

	Map<String, SecurityModelAuthorization> authorizationsById = new HashMap<>();

	KeyListMap<String, SecurityModelAuthorization> authorizationsByPrincipalId = new KeyListMap<>();

	KeyListMap<String, SecurityModelAuthorization> authorizationsByTargets = new KeyListMap<>();

	/**
	 * For each group, a list of groups receiving access from it (including self)
	 */
	KeyListMap<String, String> groupsReceivingAccessToGroup = new KeyListMap<>();


	/**
	 * For each group, a list of groups giving access to it (including self)
	 */
	KeyListMap<String, String> groupsGivingAccessToGroup = new KeyListMap<>();

	Map<String, Boolean> globalGroupDisabledMap = new HashMap<>();

	String collection;
	List<String> securableRecordSchemaTypes;


	public static SingletonSecurityModel empty(String collection) {
		return new SingletonSecurityModel(collection);
	}


	private SingletonSecurityModel(String collection) {

		this.groupAuthorizationsInheritance = GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD;
		this.globalGroupDisabledMap = Collections.emptyMap();
		this.collection = collection;
		this.securableRecordSchemaTypes = Collections.emptyList();
	}

	public SingletonSecurityModel(List<Authorization> authorizationDetails,
								  Map<String, Boolean> globalGroupDisabledMap,
								  KeyListMap<String, String> groupsReceivingAccessToGroup,
								  KeyListMap<String, String> groupsGivingAccessToGroup,
								  GroupAuthorizationsInheritance groupAuthorizationsInheritance,
								  List<String> securableRecordSchemaTypes,
								  String collection) {

		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
		this.collection = collection;
		this.securableRecordSchemaTypes = securableRecordSchemaTypes;
		this.globalGroupDisabledMap = globalGroupDisabledMap;
		this.groupsReceivingAccessToGroup = groupsReceivingAccessToGroup;
		this.groupsGivingAccessToGroup = groupsGivingAccessToGroup;
		initAuthsMaps(authorizationDetails);
	}

	protected void initAuthsMaps(List<Authorization> authorizationDetails) {
		for (Authorization authorizationDetail : authorizationDetails) {
			insertAuthorizationInMemoryMaps(authorizationDetail);
		}
	}

	private void insertAuthorizationInMemoryMaps(Authorization authorizationDetail) {
		boolean securableRecord = false;

		try {
			securableRecord = securableRecordSchemaTypes.contains(authorizationDetail.getTargetSchemaType());


			SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(
					authorizationDetail, securableRecord, groupAuthorizationsInheritance);
			authorizations.add(securityModelAuthorization);
			authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
			authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);

			for (String principalId : authorizationDetail.getPrincipals()) {
				if (globalGroupDisabledMap.keySet().contains(principalId)) {
					securityModelAuthorization.addGroupId(principalId);
				} else {
					securityModelAuthorization.addUserId(principalId);
				}
				authorizationsByPrincipalId.add(principalId, securityModelAuthorization);
			}

		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//Can occur during migration of old versions
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

	public Set<String> getGroupIds() {
		return globalGroupDisabledMap.keySet();
	}

	public List<String> getGroupsInheritingAuthorizationsFrom(String groupId) {
		return groupsReceivingAccessToGroup.get(groupId);
	}

	@Override
	public boolean isGroupActive(String groupId) {
		return Boolean.TRUE.equals(globalGroupDisabledMap.get(groupId));
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity) {

		return SecurityModelUtils.getAuthorizationDetailsOnMetadatasProvidingSecurity(metadatasProvidingSecurity, this);
	}


	@Override
	public List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId, boolean includeInherited) {
		if (includeInherited) {

			List<SecurityModelAuthorization> returnedAuths = new ArrayList<>();
			for (String inheritingPrincipal : groupsGivingAccessToGroup.get(principalId)) {
				returnedAuths.addAll(authorizationsByPrincipalId.get(inheritingPrincipal));
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

		for (String groupId : auth.getGroupIds()) {
			removeAuthWithId(authId, authorizationsByPrincipalId.get(groupId));
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
