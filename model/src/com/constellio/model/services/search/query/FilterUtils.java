package com.constellio.model.services.search.query;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.AuthorizationDetailsFilter;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;
import com.constellio.model.services.users.UserServices;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.retrieveUserTokens;
import static com.constellio.model.entities.schemas.Schemas.COLLECTION;
import static com.constellio.model.entities.schemas.Schemas.DENY_TOKENS;
import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.schemas.Schemas.TOKENS_OF_HIERARCHY;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.entities.security.SecurityModelUtils.hasNegativeAccessOnSecurisedRecord;

public class FilterUtils {

	public static String multiCollectionUserReadFilter(UserCredential userCredential, UserServices userServices,
													   SecurityTokenManager securityTokenManager) {
		StringBuilder filter = new StringBuilder();
		if (userCredential.getCollections().isEmpty() || !userCredential.isActiveUser()) {
			filter.append(TOKENS.getDataStoreCode());
			filter.append(":");
			filter.append("A38");
		} else {
			filter.append(TOKENS.getDataStoreCode());
			filter.append(":");
			filter.append("A38");

			for (String collection : userCredential.getCollections()) {
				User user = userServices.getUserInCollection(userCredential.getUsername(), collection);
				if (user.hasCollectionReadAccess()) {
					filter.append(" OR ");
					filter.append(COLLECTION.getDataStoreCode());
					filter.append(":");
					filter.append(collection);
				}

				filter.append(" OR ");
				filter.append(TOKENS.getDataStoreCode());
				filter.append(":r_");
				filter.append(user.getId());

				for (String schemaType : securityTokenManager
						.getGlobalPermissionSecurableSchemaTypesVisibleBy(user, Role.READ)) {
					filter.append(" OR ");
					filter.append(SCHEMA.getDataStoreCode());
					filter.append(":");
					filter.append(schemaType);
					filter.append("_*");
				}

				filter.append(" OR (");
				filter.append(userReadFilter(user, securityTokenManager));
				filter.append(")");

			}
		}

		return filter.toString();
	}

	public static String userWriteFilter(User user, SecurityTokenManager securityTokenManager) {

		SolrFilterBuilder filterBuilder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		SecurityModel securityModel = user.getRolesDetails().getSchemasRecordsServices().getModelLayerFactory()
				.newRecordServices().getSecurityModel(user.getCollection());

		if (!user.hasCollectionWriteAccess()) {
			if (hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(user.getId(), false))) {
				filterBuilder.appendNegative(TOKENS, "nw_" + user.getId());
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)
					&& hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(aGroup, true))) {
					filterBuilder.appendNegative(TOKENS, "nw_" + aGroup);
				}
			}
		}

		filterBuilder.openORGroupReturningFalseIfEmpty();

		if (user.isActiveUser()) {
			if (user.hasCollectionWriteAccess()) {
				filterBuilder.append(COLLECTION, user.getCollection());
			}

			filterBuilder.append(Schemas.TOKENS, "w_" + user.getId());

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurableSchemaTypesVisibleBy(user, WRITE)) {
				filterBuilder.append(SCHEMA, schemaType + "_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					filterBuilder.append(Schemas.TOKENS, "w_" + aGroup);
				}
			}

			UserTokens tokens = securityTokenManager.getTokens(user);
			addRecordAuths(filterBuilder, user, false, UserAuthorizationsUtils.WRITE_ACCESS);

			for (String token : tokens.getAllowTokens()) {
				if (token.charAt(0) == 'w') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String token : tokens.getShareAllowTokens()) {
				if (token.charAt(0) == 'w') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String publicType : securityTokenManager.getSchemaTypesWithoutSecurity()) {
				filterBuilder.append(SCHEMA, publicType + "_*");
			}
		}
		filterBuilder.closeGroup();
		return filterBuilder.toString();
	}

	public static String userReadFilter(User user, SecurityTokenManager securityTokenManager) {
		SolrFilterBuilder filterBuilder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();
		SecurityModel securityModel = user.getRolesDetails().getSchemasRecordsServices().getModelLayerFactory()
				.newRecordServices().getSecurityModel(user.getCollection());

		UserTokens tokens = securityTokenManager.getTokens(user);
		for (String token : tokens.getAllowTokens()) {
			if (token.charAt(0) == 'r') {
				filterBuilder.appendNegative(DENY_TOKENS, token);
			}
		}
		if (!user.hasCollectionReadAccess() && !user.hasCollectionWriteAccess() && !user.hasCollectionDeleteAccess()) {

			if (hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(user.getId(), false))) {
				filterBuilder.appendNegative(TOKENS, "nr_" + user.getId());
			}
			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)
					&& hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(aGroup, true))) {
					filterBuilder.appendNegative(TOKENS, "nr_" + aGroup);
				}
			}
		}

		filterBuilder.openORGroupReturningFalseIfEmpty();

		if (user.isActiveUser()) {

			if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
				filterBuilder.append(COLLECTION, user.getCollection());
			}

			filterBuilder.append(TOKENS, "r_" + user.getId());

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurableSchemaTypesVisibleBy(user, Role.READ)) {
				filterBuilder.append(SCHEMA, schemaType + "_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					filterBuilder.append(TOKENS, "r_" + aGroup);
				}
			}

			addRecordAuths(filterBuilder, user, false, UserAuthorizationsUtils.READ_ACCESS);

			for (String token : tokens.getAllowTokens()) {
				if (token.charAt(0) == 'r') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String token : tokens.getShareAllowTokens()) {
				if (token.charAt(0) == 'r') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String publicType : securityTokenManager.getSchemaTypesWithoutSecurity()) {
				filterBuilder.append(SCHEMA, publicType + "_*");
			}

			filterBuilder.append(TOKENS, Record.PUBLIC_TOKEN);

		}
		filterBuilder.closeGroup();
		return filterBuilder.toString();
	}

	public static String userHierarchyFilter(User user, SecurityTokenManager securityTokenManager, String access,
											 MetadataSchemaType selectedType, boolean includeInvisible) {

		SecurityModel securityModel = user.getRolesDetails().getSchemasRecordsServices().getModelLayerFactory()
				.newRecordServices().getSecurityModel(user.getCollection());
		String selectedTypeSmallCode = null;
		if (selectedType != null) {
			selectedTypeSmallCode = selectedType.getSmallCode();
			if (selectedTypeSmallCode == null) {
				selectedTypeSmallCode = selectedType.getCode();
			}
		}

		String tokenPrefix;
		if (Role.READ.equals(access)) {
			if (selectedType == null) {
				tokenPrefix = "r";
			} else {
				tokenPrefix = "r" + selectedTypeSmallCode;
			}
		} else {
			if (selectedType == null) {
				tokenPrefix = "w";
			} else {
				tokenPrefix = "w" + selectedTypeSmallCode;
			}
		}

		SolrFilterBuilder filterBuilder = SolrFilterBuilder.createAndFilterReturningTrueIfEmpty();

		UserTokens tokens = securityTokenManager.getTokens(user);

		for (String token : tokens.getAllowTokens()) {
			if (token.charAt(0) == 'r') {
				filterBuilder.appendNegative(DENY_TOKENS, token);
			}
		}

		if (user.isActiveUser() && !user.hasCollectionAccess(access == null ? Role.READ : access)) {

			if (hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(user.getId(), false))) {
				filterBuilder.appendNegative(TOKENS_OF_HIERARCHY, "nr_" + user.getId());
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)
					&& hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(aGroup, true))) {

					filterBuilder.appendNegative(TOKENS_OF_HIERARCHY, "nr_" + aGroup);
				}
			}
		}

		filterBuilder.openORGroupReturningFalseIfEmpty();

		if (user.isActiveUser()) {

			if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
				filterBuilder.append(COLLECTION, user.getCollection());
			}

			filterBuilder.append(TOKENS_OF_HIERARCHY, tokenPrefix + "_" + user.getId());
			if (includeInvisible) {
				filterBuilder.append(TOKENS_OF_HIERARCHY, "z" + tokenPrefix + "_" + user.getId());
			}

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurableSchemaTypesVisibleBy(user, Role.READ)) {
				filterBuilder.append(SCHEMA, schemaType + "_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {

					filterBuilder.append(TOKENS_OF_HIERARCHY, tokenPrefix + "_" + aGroup);

					if (includeInvisible) {
						filterBuilder.append(TOKENS_OF_HIERARCHY, "z" + tokenPrefix + "_" + aGroup);
					}
				}
			}

			addRecordAuths(filterBuilder, user, false, UserAuthorizationsUtils.READ_ACCESS);

			for (String token : tokens.getAllowTokens()) {
				if (token.charAt(0) == 'r') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String token : tokens.getShareAllowTokens()) {
				if (token.charAt(0) == 'r') {
					filterBuilder.append(TOKENS, token);
				}
			}


			for (String publicType : securityTokenManager.getSchemaTypesWithoutSecurity()) {
				filterBuilder.append(SCHEMA, publicType + "_*");
			}

			filterBuilder.append(TOKENS_OF_HIERARCHY, Record.PUBLIC_TOKEN);

		}
		filterBuilder.closeGroup();
		return filterBuilder.toString();
	}

	public static String permissionFilter(User user, String permission) {

		if (!user.isActiveUser()) {
			return SolrFilterBuilder.createAndFilterReturningFalseIfEmpty().build();

		} else if (user.has(permission).globally()) {
			return SolrFilterBuilder.createAndFilterReturningTrueIfEmpty().build();

		} else {
			SolrFilterBuilder filterBuilder = SolrFilterBuilder.createOrFilterReturningFalseIfEmpty();
			List<String> rolesGivingPermission = Role.toCodes(user.getRolesDetails().getRolesGivingPermission(permission));
			for (String role : rolesGivingPermission) {

				filterBuilder.append(TOKENS, role + "_" + user.getId());

				for (String aGroup : user.getUserGroups()) {
					if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
						filterBuilder.append(TOKENS, role + "_" + aGroup);
					}
				}
			}

			addRecordAuths(filterBuilder, user, false,
					UserAuthorizationsUtils.anyRole(rolesGivingPermission.toArray(new String[0])));
			return filterBuilder.build();
		}

	}

	public static String userDeleteFilter(User user, SecurityTokenManager securityTokenManager) {

		SecurityModel securityModel = user.getRolesDetails().getSchemasRecordsServices().getModelLayerFactory()
				.newRecordServices().getSecurityModel(user.getCollection());

		SolrFilterBuilder filterBuilder = SolrFilterBuilder.createAndFilterReturningFalseIfEmpty();

		if (!user.hasCollectionDeleteAccess()) {

			if (hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(user.getId(), false))) {
				filterBuilder.appendNegative(TOKENS, "nd_" + user.getId());
			}
			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)
					&& hasNegativeAccessOnSecurisedRecord(securityModel.getAuthorizationsToPrincipal(aGroup, true))) {
					filterBuilder.appendNegative(TOKENS, "nd_" + aGroup);
				}
			}
		}
		filterBuilder.openORGroupReturningFalseIfEmpty();
		if (user.isActiveUser()) {

			if (user.hasCollectionDeleteAccess()) {
				filterBuilder.append(COLLECTION, user.getCollection());
			}

			filterBuilder.append(TOKENS, "d_" + user.getId());

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurableSchemaTypesVisibleBy(user, Role.DELETE)) {
				filterBuilder.append(SCHEMA, schemaType + "_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					filterBuilder.append(TOKENS, "d_" + aGroup);
				}
			}

			UserTokens tokens = securityTokenManager.getTokens(user);
			addRecordAuths(filterBuilder, user, true, UserAuthorizationsUtils.DELETE_ACCESS);

			for (String token : tokens.getAllowTokens()) {
				if (token.charAt(0) == 'd') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String token : tokens.getShareAllowTokens()) {
				if (token.charAt(0) == 'd') {
					filterBuilder.append(TOKENS, token);
				}
			}

			for (String publicType : securityTokenManager.getSchemaTypesWithoutSecurity()) {
				filterBuilder.append(SCHEMA, publicType + "_*");
			}

		}
		filterBuilder.closeGroup();
		return filterBuilder.build();
	}


	private static void addRecordAuths(SolrFilterBuilder filterBuilder, User user,
									   boolean includeSpecifics,
									   AuthorizationDetailsFilter filter) {

		//Specific auths are excluded, they are handled with tokens
		KeySetMap<String, String> removedAuthsGroupedByTarget = retrieveUserTokens(user, includeSpecifics, filter);

		for (Map.Entry<String, Set<String>> token : removedAuthsGroupedByTarget.getNestedMap().entrySet()) {
			filterBuilder.openANDGroupRemovedIfEmpty();
			filterBuilder.append(Schemas.ATTACHED_ANCESTORS, token.getKey());
			//TODO Tester!
			for (Iterator<String> iterator = token.getValue().iterator(); iterator.hasNext(); ) {
				String removedAuth = iterator.next();
				filterBuilder.appendNegative(Schemas.ALL_REMOVED_AUTHS, removedAuth);
			}

			filterBuilder.closeGroup();
		}

	}

	public static String statusFilter(StatusFilter status) {
		if (status == StatusFilter.ACTIVES) {
			return "(*:* -deleted_s:__TRUE__)";
		} else if (status == StatusFilter.DELETED) {
			return "deleted_s:__TRUE__";
		} else {
			return null;
		}
	}

}
