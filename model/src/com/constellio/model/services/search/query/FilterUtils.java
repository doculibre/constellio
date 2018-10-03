package com.constellio.model.services.search.query;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.AuthorizationDetailsFilter;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;
import com.constellio.model.services.users.UserServices;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterUtils {

	public static String multiCollectionUserReadFilter(UserCredential userCredential, UserServices userServices,
													   SecurityTokenManager securityTokenManager) {
		StringBuilder filter = new StringBuilder();
		if (userCredential.getCollections().isEmpty() || !userCredential.isActiveUser()) {
			addTokenA38(filter);
		} else {
			addTokenA38(filter);

			for (String collection : userCredential.getCollections()) {
				User user = userServices.getUserInCollection(userCredential.getUsername(), collection);
				if (user.hasCollectionReadAccess()) {
					filter.append(" OR ");
					filter.append(Schemas.COLLECTION.getDataStoreCode());
					filter.append(":");
					filter.append(collection);
				}

				filter.append(" OR ");
				filter.append(Schemas.TOKENS.getDataStoreCode());
				filter.append(":r_");
				filter.append(user.getId());

				for (String schemaType : securityTokenManager
						.getGlobalPermissionSecurizedSchemaTypesVisibleBy(user, Role.READ)) {
					filter.append(" OR ");
					filter.append(Schemas.SCHEMA.getDataStoreCode());
					filter.append(":");
					filter.append(schemaType);
					filter.append("_*");
				}

				for (String aGroup : user.getUserGroups()) {
					if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
						filter.append(" OR ");
						filter.append(Schemas.TOKENS.getDataStoreCode());
						filter.append(":r_");
						filter.append(aGroup);
					}
				}

				filter.append(" OR (");
				filter.append(userReadFilter(user, securityTokenManager));
				filter.append(")");

			}
		}
		return filter.toString();
	}

	public static String userWriteFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();

		if (!user.hasCollectionWriteAccess()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(" AND ");
			}
			stringBuilder.append("-");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":nw_");
			stringBuilder.append(user.getId());

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" AND -");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":nw_");
					stringBuilder.append(aGroup);
				}
			}
		}
		stringBuilder.append("(");
		addTokenA38(stringBuilder);
		if (user.isActiveUser()) {
			if (user.hasCollectionWriteAccess()) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(user.getCollection());
			}

			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":w_");
			stringBuilder.append(user.getId());

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurizedSchemaTypesVisibleBy(user, Role.WRITE)) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.SCHEMA.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(schemaType);
				stringBuilder.append("_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" OR ");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":w_");
					stringBuilder.append(aGroup);
				}
			}

			UserTokens tokens = securityTokenManager.getTokens(user);
			addAuthsTokens(stringBuilder, user, false, UserAuthorizationsUtils.WRITE_ACCESS);
			addTokens(stringBuilder, tokens.getAllowTokens(), 'w');
			addTokens(stringBuilder, tokens.getShareAllowTokens(), 'w');
			addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}

	public static String userReadFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();
		UserTokens tokens = securityTokenManager.getTokens(user);
		addDenyTokens(stringBuilder, tokens.getAllowTokens(), 'r');
		if (!user.hasCollectionReadAccess() && !user.hasCollectionWriteAccess() && !user.hasCollectionDeleteAccess()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(" AND ");
			}


			stringBuilder.append("-");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":nr_");
			stringBuilder.append(user.getId());

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" AND -");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":nr_");
					stringBuilder.append(aGroup);
				}
			}
		}
		boolean denyOrNegativeTokens = stringBuilder.length() > 0;

		if (denyOrNegativeTokens) {
			stringBuilder.append(" AND (");
		}
		addTokenA38(stringBuilder);

		if (user.isActiveUser()) {

			if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(user.getCollection());
			}

			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":r_");
			stringBuilder.append(user.getId());

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurizedSchemaTypesVisibleBy(user, Role.READ)) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.SCHEMA.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(schemaType);
				stringBuilder.append("_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" OR ");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":r_");
					stringBuilder.append(aGroup);
				}
			}

			addAuthsTokens(stringBuilder, user, false, UserAuthorizationsUtils.READ_ACCESS);
			addTokens(stringBuilder, tokens.getAllowTokens(), 'r');
			addTokens(stringBuilder, tokens.getShareAllowTokens(), 'r');
			addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(Record.PUBLIC_TOKEN);

		}
		if (denyOrNegativeTokens) {
			stringBuilder.append(")");
		}
		return stringBuilder.toString();
	}

	public static String userHierarchyFilter(User user, SecurityTokenManager securityTokenManager, String access,
											 MetadataSchemaType selectedType, boolean includeInvisible) {

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

		StringBuilder stringBuilder = new StringBuilder();
		UserTokens tokens = securityTokenManager.getTokens(user);
		addDenyTokens(stringBuilder, tokens.getAllowTokens(), 'r');

		if (user.isActiveUser()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(" AND ");
			}
			stringBuilder.append("-");
			stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
			stringBuilder.append(":nw_");
			stringBuilder.append(user.getId());

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" AND -");
					stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
					stringBuilder.append(":nw_");
					stringBuilder.append(aGroup);
				}
			}
		}

		boolean denyOrNegativeTokens = stringBuilder.length() > 0;
		if (denyOrNegativeTokens) {
			stringBuilder.append(" AND (");
		}
		addTokenA38(stringBuilder);

		if (user.isActiveUser()) {

			if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(user.getCollection());
			}

			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
			stringBuilder.append(":" + tokenPrefix + "_");
			stringBuilder.append(user.getId());
			if (includeInvisible) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
				stringBuilder.append(":" + "z" + tokenPrefix + "_");
				stringBuilder.append(user.getId());
			}

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurizedSchemaTypesVisibleBy(user, Role.READ)) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.SCHEMA.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(schemaType);
				stringBuilder.append("_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" OR ");
					stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
					stringBuilder.append(":" + tokenPrefix + "_");
					stringBuilder.append(aGroup);
					if (includeInvisible) {
						stringBuilder.append(" OR ");
						stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
						stringBuilder.append(":z" + tokenPrefix + "_");
						stringBuilder.append(aGroup);
					}
				}
			}

			addAuthsTokens(stringBuilder, user, false, UserAuthorizationsUtils.READ_ACCESS);
			addTokens(stringBuilder, tokens.getAllowTokens(), 'r');
			addTokens(stringBuilder, tokens.getShareAllowTokens(), 'r');
			addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS_OF_HIERARCHY.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(Record.PUBLIC_TOKEN);
			if (denyOrNegativeTokens) {
				stringBuilder.append(")");
			}
		}
		return stringBuilder.toString();
	}

	private static void addDenyTokens(StringBuilder stringBuilder, List<String> tokens, Character type) {
		for (String token : tokens) {
			if (token.charAt(0) == type) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(" AND ");
				}
				stringBuilder.append("-");
				stringBuilder.append(Schemas.DENY_TOKENS.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(token);
			}
		}
	}

	public static String permissionFilter(User user, String permission) {
		StringBuilder stringBuilder = new StringBuilder();

		if (!user.isActiveUser()) {
			addTokenA38(stringBuilder);

		} else if (!user.has(permission).globally()) {
			addTokenA38(stringBuilder);

			List<String> rolesGivingPermission = Role.toCodes(user.getRolesDetails().getRolesGivingPermission(permission));
			for (String role : rolesGivingPermission) {

				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
				stringBuilder.append(":" + role + "_");
				stringBuilder.append(user.getId());

				for (String aGroup : user.getUserGroups()) {
					if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
						stringBuilder.append(" OR ");
						stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
						stringBuilder.append(":" + role + "_");
						stringBuilder.append(aGroup);
					}
				}
			}

			addAuthsTokens(stringBuilder, user, false,
					UserAuthorizationsUtils.anyRole(rolesGivingPermission.toArray(new String[0])));
		}
		return stringBuilder.toString();
	}

	public static String userDeleteFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();

		if (!user.hasCollectionDeleteAccess()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(" AND ");
			}
			stringBuilder.append("-");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":nd_");
			stringBuilder.append(user.getId());

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" AND -");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":nd_");
					stringBuilder.append(aGroup);
				}
			}
		}
		stringBuilder.append("(");
		addTokenA38(stringBuilder);

		if (user.isActiveUser()) {

			if (user.hasCollectionDeleteAccess()) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(user.getCollection());
			}

			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":d_");
			stringBuilder.append(user.getId());

			for (String schemaType : securityTokenManager.getGlobalPermissionSecurizedSchemaTypesVisibleBy(user, Role.DELETE)) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.SCHEMA.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(schemaType);
				stringBuilder.append("_*");
			}

			for (String aGroup : user.getUserGroups()) {
				if (user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					stringBuilder.append(" OR ");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":d_");
					stringBuilder.append(aGroup);
				}
			}

			UserTokens tokens = securityTokenManager.getTokens(user);
			addAuthsTokens(stringBuilder, user, true, UserAuthorizationsUtils.DELETE_ACCESS);
			addTokens(stringBuilder, tokens.getAllowTokens(), 'd');
			addTokens(stringBuilder, tokens.getShareAllowTokens(), 'd');
			addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());

		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}

	private static void addAuthsTokens(StringBuilder stringBuilder, User user, boolean includeSpecifics,
									   AuthorizationDetailsFilter filter) {

		//Specific auths are excluded, they are handled with tokens
		KeySetMap<String, String> tokens = UserAuthorizationsUtils.retrieveUserTokens(user, includeSpecifics, filter);

		for (Map.Entry<String, Set<String>> token : tokens.getNestedMap().entrySet()) {
			stringBuilder.append(" OR (");
			stringBuilder.append(Schemas.ATTACHED_ANCESTORS.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(token.getKey());
			stringBuilder.append(" AND -(");
			//TODO Tester!
			for (Iterator<String> iterator = token.getValue().iterator(); iterator.hasNext(); ) {
				String removedAuth = iterator.next();
				stringBuilder.append(Schemas.ALL_REMOVED_AUTHS.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(removedAuth);
				if (iterator.hasNext()) {
					stringBuilder.append(" AND ");
				}

			}
			stringBuilder.append(")");
			stringBuilder.append(")");
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

	private static void addTokenA38(StringBuilder stringBuilder) {
		stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
		stringBuilder.append(":");
		stringBuilder.append("A38");
	}

	private static void addTokens(StringBuilder stringBuilder, List<String> tokens, Character type) {
		for (String token : tokens) {
			if (token.charAt(0) == type) {
				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
				stringBuilder.append(":");
				stringBuilder.append(token);
			}
		}
	}

	private static void addPublicTypes(StringBuilder stringBuilder, List<String> publicTypes) {
		for (String publicType : publicTypes) {
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.SCHEMA.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(publicType + "_*");
		}
	}
}
