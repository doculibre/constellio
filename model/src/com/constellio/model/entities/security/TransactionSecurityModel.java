package com.constellio.model.entities.security;

import com.constellio.data.utils.Provider;
import com.constellio.model.entities.calculators.DynamicDependencyValues;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordProvider;
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
	final RecordProvider recordProvider;

	public TransactionSecurityModel(MetadataSchemaTypes types, Roles roles, SingletonSecurityModel nestedSecurityModel,
									Transaction transaction) {
		this.nestedSecurityModel = nestedSecurityModel;
		this.transaction = transaction;
		this.roles = roles;
		this.types = types;
		this.recordProvider = new RecordProvider(null, nestedSecurityModel.recordProvider, null, transaction);
		this.modifiedAuths = new ArrayList<>();


		for (Record record : transaction.getRecords()) {
			if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Authorization auth = Authorization.wrapNullable(record, types);
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
			if (Authorization.SCHEMA_TYPE.equals(record.getTypeCode())) {
				Authorization authorization = Authorization.wrapNullable(record, types);
				if (id.equals(authorization.getTarget())) {

					if (indexMap == null) {
						indexMap = new HashMap<>();
						for (int i = 0; i < nestedSecurityModelAuths.size(); i++) {
							indexMap.put(nestedSecurityModelAuths.get(i).details.getId(), i);
						}
					}

					Integer index = indexMap.get(authorization.getId());
					ajustedAuths.add(id);
					if (index == null) {
						returnedAuths.add(wrap(authorization));
					} else {

						SecurityModelAuthorization newVersion = wrapExistingAuthUsingModifiedUsersAndGroups(
								nestedSecurityModel.groupAuthorizationsInheritance,
								nestedSecurityModel.principalTaxonomy,
								authorization,
								nestedSecurityModel.getUsers(),
								nestedSecurityModel.getGroups());

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
						nestedSecurityModel.principalTaxonomy,
						returnedAuth.getDetails(),
						nestedSecurityModel.getUsers(),
						nestedSecurityModel.getGroups());

				returnedAuths.set(i, newVersion);
			}
		}

		List<SecurityModelAuthorization> nonDeletedReturnedAuths = new ArrayList<>();

		for (SecurityModelAuthorization auth : returnedAuths) {
			if (!Boolean.TRUE.equals(((Authorization) auth.getDetails()).get(Schemas.LOGICALLY_DELETED_STATUS))) {
				nonDeletedReturnedAuths.add(auth);
			}
		}

		return nonDeletedReturnedAuths;
	}

	private SecurityModelAuthorization wrap(Authorization details) {
		SecurityModelAuthorization authorization = wrapNewAuthWithoutUsersAndGroups(
				nestedSecurityModel.groupAuthorizationsInheritance,
				nestedSecurityModel.principalTaxonomy,
				details);

		for (String principalId : details.getPrincipals()) {
			Object principal = getPrincipalById(principalId);

			if (principal instanceof User) {
				authorization.users.add((User) principal);

			} else if (principal instanceof Group) {
				authorization.groups.add((Group) principal);
			}
		}

		return authorization;
	}

	@Override
	public SecurityModelAuthorization getAuthorizationWithId(String authId) {

		SecurityModelAuthorization nestedAuthorization = nestedSecurityModel.getAuthorizationWithId(authId);

		for (Record record : transaction.getRecords()) {
			if (record.getId().equals(authId)) {
				Authorization authorization = Authorization.wrapNullable(record, types);
				if (nestedAuthorization == null) {
					return wrap(authorization);

				} else {
					return wrapExistingAuthUsingModifiedUsersAndGroups(
							nestedSecurityModel.groupAuthorizationsInheritance,
							nestedSecurityModel.principalTaxonomy,
							authorization,
							nestedSecurityModel.getUsers(),
							nestedSecurityModel.getGroups());

				}
			}
		}
		if (nestedAuthorization == null) {
			return null;
		} else {
			return wrapExistingAuthUsingModifiedUsersAndGroups(
					nestedSecurityModel.groupAuthorizationsInheritance,
					nestedSecurityModel.principalTaxonomy,
					nestedAuthorization.getDetails(),
					nestedSecurityModel.getUsers(),
					nestedSecurityModel.getGroups());
		}
	}

	@Override
	public List<Group> getGroupsInheritingAuthorizationsFrom(Group group) {
		//TODO Handle group inheritance modifications in transaction
		return nestedSecurityModel.getGroupsInheritingAuthorizationsFrom(group);
	}

	@Override
	public boolean isGroupActive(Group group) {
		//TODO Handle group inheritance modifications in transaction
		return nestedSecurityModel.isGroupActive(group);
	}

	@Override
	public List<SecurityModelAuthorization> getAuthorizationDetailsOnMetadatasProvidingSecurity(
			DynamicDependencyValues metadatasProvidingSecurity) {

		return SecurityModelUtils.getAuthorizationDetailsOnMetadatasProvidingSecurity(
				metadatasProvidingSecurity, recordProvider, this, new Provider<String, SecurityModelAuthorization>() {
					@Override
					public SecurityModelAuthorization get(String authId) {
						return getAuthorizationWithId(authId);
					}
				});

	}

	@Override
	public Object getPrincipalById(String id) {

		Record record = transaction.getRecord(id);
		if (record != null) {

			if (User.SCHEMA_TYPE.equals(record.getTypeCode())) {
				return User.wrapNullable(record, types, roles);
			} else {
				return Group.wrapNullable(record, types);
			}

		} else {
			return nestedSecurityModel.getPrincipalById(id);
		}

	}

	@Override
	public List<SecurityModelAuthorization> getInheritedAuthorizationsTargettingSecurizedRecords(
			String securizedRecordId) {
		return null;
	}

	protected void addActiveAuthorizations(List<String> returnedIds,
										   List<SecurityModelAuthorization> metadataAuths) {
		for (SecurityModelAuthorization auth : metadataAuths) {
			Authorization authorizationDetails = (Authorization) auth.getDetails();
			if (authorizationDetails.isActiveAuthorization()) {
				returnedIds.add(authorizationDetails.getId());
			}
		}
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
