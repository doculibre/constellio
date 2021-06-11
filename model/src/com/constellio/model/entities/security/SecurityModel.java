package com.constellio.model.entities.security;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.records.structures.NestedRecordAuthorizations;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.AuthorizationDetailsFilter;

import java.util.List;

public interface SecurityModel {

	List<SecurityModelAuthorization> getAuthorizationsOnTarget(String targetId);

	List<SecurityModelAuthorization> wrapNestedAuthorizationsOnTarget(NestedRecordAuthorizations authorizations);

	SecurityModelAuthorization getAuthorizationWithId(String authId);

	List<String> getGroupsInheritingAuthorizationsFrom(String groupId);

	boolean isGroupActive(String groupId);

	List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity);

	List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId, boolean includeInherited);

	List<String> getGroupsGivingAccessToUser(String userId);

	KeySetMap<String, String> retrieveUserTokens(User user, boolean includeSpecifics,
												 AuthorizationDetailsFilter filter);

	boolean hasNoNegativeAuth();

	long getVersion();
}
