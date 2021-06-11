package com.constellio.model.entities.security;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.structures.NestedRecordAuthorizations;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.AuthorizationDetailsFilter;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.security.roles.Roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.security.SecurityModelAuthorization.wrapExistingAuthUsingModifiedUsersAndGroups;
import static com.constellio.model.entities.security.SecurityModelAuthorization.wrapNewAuthWithoutUsersAndGroups;

public class TransactionSecurityModel implements SecurityModel {

	SingletonSecurityModel nestedSecurityModel;

	Transaction transaction;

	Roles roles;
	MetadataSchemaTypes types;

	List<Authorization> modifiedAuths;

	public TransactionSecurityModel(MetadataSchemaTypes types, Roles roles, SingletonSecurityModel nestedSecurityModel,
									Transaction transaction) {
		this.nestedSecurityModel = nestedSecurityModel;
		this.transaction = transaction;
		this.roles = roles;
		this.types = types;
		this.modifiedAuths = new ArrayList<>();


		for (Record record : transaction.getRecords()) {
			if (RecordAuthorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Authorization auth = RecordAuthorization.wrapNullable(record, types);
				modifiedAuths.add(auth);
			}
		}

	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsOnTarget(String id) {

		final List<SecurityModelAuthorization> nestedSecurityModelAuths = nestedSecurityModel.getAuthorizationsOnTarget(id);
		final List<SecurityModelAuthorization> returnedAuths = new ArrayList<>(nestedSecurityModelAuths);

		if (modifiedAuths.isEmpty()) {
			return returnedAuths;
		}

		Set<String> ajustedAuths = new HashSet<>();
		Map<String, Integer> indexMap = null;
		for (Record record : transaction.getRecords()) {
			if (RecordAuthorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Authorization authorization = RecordAuthorization.wrapNullable(record, types);
				if (id.equals(authorization.getTarget())) {

					if (indexMap == null) {
						indexMap = new HashMap<>();
						for (int i = 0; i < nestedSecurityModelAuths.size(); i++) {
							indexMap.put(nestedSecurityModelAuths.get(i).getDetails().getId(), i);
						}
					}

					Integer index = indexMap.get(authorization.getId());
					ajustedAuths.add(id);
					if (index == null) {
						returnedAuths.add(wrap(authorization));
					} else {

						SecurityModelAuthorization newVersion = wrapExistingAuthUsingModifiedUsersAndGroups(
								nestedSecurityModel.groupAuthorizationsInheritance,
								nestedSecurityModel.securableRecordSchemaTypes.contains(authorization.getTargetSchemaType()),
								authorization,
								nestedSecurityModel.getGroupIds());

						SecurityModelAuthorization oldVersion = nestedSecurityModelAuths.get(index);

						returnedAuths.set(index, newVersion);
					}
				}
			}
		}

		for (int i = 0; i < nestedSecurityModelAuths.size(); i++) {
			SecurityModelAuthorization returnedAuth = returnedAuths.get(i);
			if (!ajustedAuths.contains(returnedAuth.getDetails().getId())) {
				SecurityModelAuthorization newVersion = wrapExistingAuthUsingModifiedUsersAndGroups(
						nestedSecurityModel.groupAuthorizationsInheritance,
						returnedAuth.isSecurableRecord(),
						returnedAuth.getDetails(),
						nestedSecurityModel.getGroupIds());

				returnedAuths.set(i, newVersion);
			}
		}

		List<SecurityModelAuthorization> nonDeletedReturnedAuths = new ArrayList<>();

		for (SecurityModelAuthorization auth : returnedAuths) {
			if (!Boolean.TRUE.equals(((RecordAuthorization) auth.getDetails()).get(Schemas.LOGICALLY_DELETED_STATUS))) {
				nonDeletedReturnedAuths.add(auth);
			}
		}

		return nonDeletedReturnedAuths;
	}

	@Override
	public List<SecurityModelAuthorization> wrapNestedAuthorizationsOnTarget(
			NestedRecordAuthorizations authorizations) {
		return nestedSecurityModel.wrapNestedAuthorizationsOnTarget(authorizations);
	}

	private SecurityModelAuthorization wrap(Authorization details) {
		SecurityModelAuthorization authorization = wrapNewAuthWithoutUsersAndGroups(
				nestedSecurityModel.groupAuthorizationsInheritance,
				nestedSecurityModel.securableRecordSchemaTypes.contains(details.getTargetSchemaType()),
				details);

		for (String principalId : details.getPrincipals()) {

			if (nestedSecurityModel.getGroupIds().contains(principalId)) {
				authorization.addGroupId(principalId);
			} else {
				authorization.addUserId(principalId);
			}
		}

		return authorization;
	}

	@Override
	public SecurityModelAuthorization getAuthorizationWithId(String authId) {

		SecurityModelAuthorization nestedAuthorization = nestedSecurityModel.getAuthorizationWithId(authId);

		for (Record record : transaction.getRecords()) {
			if (record.getId().equals(authId)) {
				Authorization authorization = RecordAuthorization.wrapNullable(record, types);
				if (nestedAuthorization == null) {
					return wrap(authorization);

				} else {
					return wrapExistingAuthUsingModifiedUsersAndGroups(
							nestedSecurityModel.groupAuthorizationsInheritance,
							nestedAuthorization.isSecurableRecord(),
							authorization,
							nestedSecurityModel.getGroupIds());

				}
			}
		}
		if (nestedAuthorization == null) {
			return null;
		} else {
			return wrapExistingAuthUsingModifiedUsersAndGroups(
					nestedSecurityModel.groupAuthorizationsInheritance,
					nestedAuthorization.isSecurableRecord(),
					nestedAuthorization.getDetails(),
					nestedSecurityModel.getGroupIds());
		}
	}

	@Override
	public List<String> getGroupsInheritingAuthorizationsFrom(String groupId) {
		return nestedSecurityModel.getGroupsInheritingAuthorizationsFrom(groupId);
	}

	@Override
	public boolean isGroupActive(String groupId) {
		return nestedSecurityModel.isGroupActive(groupId);
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity) {

		return SecurityModelUtils.getAuthorizationDetailsOnMetadatasProvidingSecurity(
				metadatasProvidingSecurity, this);

	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationsToPrincipal(String principalId,
																		 boolean includeInheritance) {
		throw new UnsupportedOperationException("Only supported on singleton security model");
	}

	@Override
	public List<String> getGroupsGivingAccessToUser(String userId) {
		return nestedSecurityModel.getGroupsGivingAccessToUser(userId);
	}

	@Override
	public KeySetMap<String, String> retrieveUserTokens(User user, boolean includeSpecifics,
														AuthorizationDetailsFilter filter) {
		return nestedSecurityModel.computeRetrieveUserTokens(user, includeSpecifics, filter);
	}

	@Override
	public boolean hasNoNegativeAuth() {
		return false;
	}

	@Override
	public long getVersion() {
		return -1L;
	}


	public static boolean hasActiveOverridingAuth(List<SecurityModelAuthorization> authorizations) {
		for (SecurityModelAuthorization auth : authorizations) {
			if (auth.getDetails().isActiveAuthorization() && auth.getDetails().isOverrideInherited()) {
				return true;
			}
		}
		return false;
	}
}
