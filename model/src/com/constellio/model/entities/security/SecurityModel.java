package com.constellio.model.entities.security;

import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.records.wrappers.Group;

import java.util.List;

public interface SecurityModel {

	List<SecurityModelAuthorization> getAuthorizationsOnTarget(String targetId);

	SecurityModelAuthorization getAuthorizationWithId(String authId);

	List<Group> getGroupsInheritingAuthorizationsFrom(Group group);

	boolean isGroupActive(Group group);

	List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity);

	Object getPrincipalById(String id);

	List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId, boolean includeInherited);


}
