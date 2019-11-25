package com.constellio.model.entities.security;

import com.constellio.model.entities.calculators.DynamicDependencyValues;

import java.util.List;

public interface SecurityModel {

	List<SecurityModelAuthorization> getAuthorizationsOnTarget(String targetId);

	SecurityModelAuthorization getAuthorizationWithId(String authId);

	List<String> getGroupsInheritingAuthorizationsFrom(String groupId);

	boolean isGroupActive(String groupId);

	List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity);

	List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId, boolean includeInherited);

	List<String> getGroupsGivingAccessToUser(String userId);
}
