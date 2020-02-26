package com.constellio.model.entities.security;

import com.constellio.data.utils.KeyListMap;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.AuthorizationDetailsFilter;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SingletonSecurityModel implements SecurityModel {

	GroupAuthorizationsInheritance groupAuthorizationsInheritance;

	List<SecurityModelAuthorization> authorizations = new ArrayList<>();

	Map<String, SecurityModelAuthorization> authorizationsById = new HashMap<>();

	KeyListMap<String, SecurityModelAuthorization> authorizationsByPrincipalId = new KeyListMap<>();

	Map<String, List<SecurityModelAuthorization>> directAndInheritedAuthorizationsByPrincipalId = new HashMap<>();

	KeyListMap<String, SecurityModelAuthorization> authorizationsByTargets = new KeyListMap<>();

	/**
	 * For each group, a list of groups receiving access from it (hierarchical, including self)
	 */
	KeyListMap<String, String> groupsReceivingAccessToGroup = new KeyListMap<>();

	/**
	 * For each users, a list of groups giving access to it (hierarchical)
	 */
	KeyListMap<String, String> groupsGivingAccessToUser = new KeyListMap<>();

	/**
	 * For each principal, a list of principals giving access to it (hierarchical, including self)
	 */
	KeyListMap<String, String> principalsGivingAccessToPrincipal = new KeyListMap<>();

	/**
	 * For each group, a list of groups giving access to it (including self)
	 */
	KeyListMap<String, String> groupsGivingAccessToGroup = new KeyListMap<>();

	Map<String, Boolean> globalGroupEnabledMap = new HashMap<>();

	String collection;
	List<String> securableRecordSchemaTypes;

	/**
	 * Cache for retrieveUserTokens, which is called a lot!
	 */
	Map<Integer, KeySetMap<String, String>> cachedUserTokens = new HashMap<>();

	private boolean noNegativeAuth;

	private long version;

	public static SingletonSecurityModel empty(String collection) {
		return new SingletonSecurityModel(collection);
	}


	private SingletonSecurityModel(String collection) {

		this.groupAuthorizationsInheritance = GroupAuthorizationsInheritance.FROM_PARENT_TO_CHILD;
		this.globalGroupEnabledMap = Collections.emptyMap();
		this.collection = collection;
		this.securableRecordSchemaTypes = Collections.emptyList();
	}

	public SingletonSecurityModel(List<Authorization> authorizationDetails,
								  Map<String, Boolean> globalGroupDisabledMap,
								  KeyListMap<String, String> groupsReceivingAccessToGroup,
								  KeyListMap<String, String> groupsGivingAccessToGroup,
								  KeyListMap<String, String> groupsGivingAccessToUser,
								  KeyListMap<String, String> principalsGivingAccessToPrincipal,
								  GroupAuthorizationsInheritance groupAuthorizationsInheritance,
								  List<String> securableRecordSchemaTypes,
								  String collection) {

		this.groupAuthorizationsInheritance = groupAuthorizationsInheritance;
		this.collection = collection;
		this.securableRecordSchemaTypes = securableRecordSchemaTypes;
		this.globalGroupEnabledMap = globalGroupDisabledMap;
		this.groupsReceivingAccessToGroup = groupsReceivingAccessToGroup;
		this.principalsGivingAccessToPrincipal = principalsGivingAccessToPrincipal;
		this.groupsGivingAccessToGroup = groupsGivingAccessToGroup;
		this.groupsGivingAccessToUser = groupsGivingAccessToUser;
		this.version = new Date().getTime();
		initAuthsMaps(authorizationDetails);

	}

	protected void initAuthsMaps(List<Authorization> authorizationDetails) {
		cachedUserTokens = new HashMap<>();
		directAndInheritedAuthorizationsByPrincipalId = new HashMap<>();
		for (Authorization authorizationDetail : authorizationDetails) {
			insertAuthorizationInMemoryMaps(authorizationDetail);
			try {
				noNegativeAuth &= !authorizationDetail.isNegative();
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
				log.warn("negative metadata does not exist", e);
			}
		}
	}


	private void insertAuthorizationInMemoryMaps(Authorization authorizationDetail) {

		try {
			boolean securableRecord = securableRecordSchemaTypes.contains(authorizationDetail.getTargetSchemaType());
			SecurityModelAuthorization securityModelAuthorization = new SecurityModelAuthorization(
					authorizationDetail, securableRecord, groupAuthorizationsInheritance);
			authorizations.add(securityModelAuthorization);
			authorizationsById.put(authorizationDetail.getId(), securityModelAuthorization);
			authorizationsByTargets.add(authorizationDetail.getTarget(), securityModelAuthorization);

			for (String principalId : authorizationDetail.getPrincipals()) {
				if (globalGroupEnabledMap.keySet().contains(principalId)) {
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
		return globalGroupEnabledMap.keySet();
	}

	public List<String> getGroupsInheritingAuthorizationsFrom(String groupId) {
		return groupsReceivingAccessToGroup.get(groupId);
	}

	public List<String> getGroupsGivingAccessToUser(String userId) {
		return groupsGivingAccessToUser.get(userId);
	}


	@Override
	public KeySetMap<String, String> retrieveUserTokens(User user, boolean includeSpecifics,
														AuthorizationDetailsFilter filter) {

		KeySetMap<String, String> tokens = null;
		int cacheKey = 0;

		if (filter.getCacheKey() != -1) {
			cacheKey = user.getWrappedRecordId().intValue() * 8;
			if (cacheKey > 0) {
				cacheKey += includeSpecifics ? 5 : 0;
				cacheKey += filter.getCacheKey();
			} else {
				cacheKey -= includeSpecifics ? 5 : 0;
				cacheKey -= filter.getCacheKey();
			}
			tokens = cachedUserTokens.get(cacheKey);
		}


		if (tokens == null) {
			tokens = computeRetrieveUserTokens(user, includeSpecifics, filter);

			if (cacheKey != 0) {
				synchronized (cachedUserTokens) {
					cachedUserTokens.put(cacheKey, tokens);
				}
			}
		}

		return tokens;
	}

	@Override
	public boolean hasNoNegativeAuth() {
		return noNegativeAuth;
	}

	@Override
	public long getVersion() {
		return 0;
	}

	KeySetMap<String, String> computeRetrieveUserTokens(User user, boolean includeSpecifics,
														AuthorizationDetailsFilter filter) {
		KeySetMap<String, String> tokens = new KeySetMap<>();
		for (SecurityModelAuthorization auth : getAuthorizationsToPrincipal(user.getId(), true)) {
			if (auth.getDetails().isActiveAuthorization() && filter.isIncluded(auth.getDetails())
				&& (!Authorization.isSecurableSchemaType(auth.getDetails().getTargetSchemaType()) || includeSpecifics)) {
				tokens.add(auth.getDetails().getTarget(), auth.getDetails().getId());
			}
		}


		return tokens;
	}

	@Override
	public boolean isGroupActive(String groupId) {
		return Boolean.TRUE.equals(globalGroupEnabledMap.get(groupId));
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity) {

		return SecurityModelUtils.getAuthorizationDetailsOnMetadatasProvidingSecurity(metadatasProvidingSecurity, this);
	}


	@Override
	public List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId, boolean includeInherited) {
		if (includeInherited) {
			List<SecurityModelAuthorization> returnedAuths = directAndInheritedAuthorizationsByPrincipalId.get(principalId);

			if (returnedAuths == null) {
				returnedAuths = new ArrayList<>();
				Set<String> addedAuths = new HashSet<>();
				for (String principal : principalsGivingAccessToPrincipal.get(principalId)) {
					for (SecurityModelAuthorization auth : authorizationsByPrincipalId.get(principal)) {
						if (!addedAuths.contains(auth.getDetails().getId())) {
							addedAuths.add(auth.getDetails().getId());
							returnedAuths.add(auth);
						}
					}
				}
				synchronized (directAndInheritedAuthorizationsByPrincipalId) {
					directAndInheritedAuthorizationsByPrincipalId.put(principalId, returnedAuths);
				}
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
		directAndInheritedAuthorizationsByPrincipalId = new HashMap<>();
		cachedUserTokens = new HashMap<>();
		removeAuthWithId(authId, authorizations);

		SecurityModelAuthorization auth = authorizationsById.remove(authId);

		for (String userId : auth.getUserIds()) {
			removeAuthWithId(authId, authorizationsByPrincipalId.get(userId));
		}

		for (String groupId : auth.getGroupIds()) {
			removeAuthWithId(authId, authorizationsByPrincipalId.get(groupId));
		}

		removeAuthWithId(authId, authorizationsByTargets.get(auth.getDetails().getTarget()));
		this.version = new Date().getTime();
	}

	public synchronized void updateCache(List<Authorization> newAuths, List<Authorization> modifiedAuths) {
		cachedUserTokens = new HashMap<>();
		directAndInheritedAuthorizationsByPrincipalId = new HashMap<>();
		for (Authorization auth : newAuths) {
			insertAuthorizationInMemoryMaps(auth);
		}

		for (Authorization auth : modifiedAuths) {
			removeAuth(auth.getId());
		}

		for (Authorization auth : modifiedAuths) {
			insertAuthorizationInMemoryMaps(auth);
		}
		this.version = new Date().getTime();
	}
}
