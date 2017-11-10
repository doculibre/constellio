package com.constellio.model.services.search.query;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils;
import com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.AuthorizationDetailsFilter;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;
import com.constellio.model.services.users.UserServices;

public class FilterUtils {

	public static String multiCollectionUserReadFilter(UserCredential userCredential, UserServices userServices,
			SecurityTokenManager securityTokenManager) {
		StringBuilder filter = new StringBuilder();
		if (userCredential.getCollections().isEmpty()) {
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
					filter.append(" OR ");
					filter.append(Schemas.TOKENS.getDataStoreCode());
					filter.append(":r_");
					filter.append(aGroup);
				}

				filter.append(" OR (");
				filter.append(userReadFilter(user, securityTokenManager));
				filter.append(")");

			}
		}
		System.out.println(filter.toString());
		return filter.toString();
	}

	public static String userWriteFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();

		addTokenA38(stringBuilder);
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
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":w_");
			stringBuilder.append(aGroup);
		}

		UserTokens tokens = securityTokenManager.getTokens(user);
		addAuthsTokens(stringBuilder, user, UserAuthorizationsUtils.WRITE_ACCESS);
		addTokens(stringBuilder, tokens.getAllowTokens(), 'w');
		addTokens(stringBuilder, tokens.getShareAllowTokens(), 'w');
		addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		return stringBuilder.toString();
	}

	public static String userReadFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();
		UserTokens tokens = securityTokenManager.getTokens(user);
		addDenyTokens(stringBuilder, tokens.getAllowTokens(), 'r');
		boolean deny = stringBuilder.length() > 0;
		if (deny) {
			stringBuilder.append(" AND (");
		}
		addTokenA38(stringBuilder);
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
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":r_");
			stringBuilder.append(aGroup);
		}

		addAuthsTokens(stringBuilder, user, UserAuthorizationsUtils.READ_ACCESS);
		addTokens(stringBuilder, tokens.getAllowTokens(), 'r');
		addTokens(stringBuilder, tokens.getShareAllowTokens(), 'r');
		addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		stringBuilder.append(" OR ");
		stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
		stringBuilder.append(":");
		stringBuilder.append(Record.PUBLIC_TOKEN);
		if (deny) {
			stringBuilder.append(")");
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

		if (!user.has(permission).globally()) {
			addTokenA38(stringBuilder);

			List<String> rolesGivingPermission = Role.toCodes(user.getRolesDetails().getRolesGivingPermission(permission));
			for (String role : rolesGivingPermission) {

				stringBuilder.append(" OR ");
				stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
				stringBuilder.append(":" + role + "_");
				stringBuilder.append(user.getId());

				for (String aGroup : user.getUserGroups()) {
					stringBuilder.append(" OR ");
					stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
					stringBuilder.append(":" + role + "_");
					stringBuilder.append(aGroup);
				}
			}

			addAuthsTokens(stringBuilder, user, UserAuthorizationsUtils.anyRole(rolesGivingPermission.toArray(new String[0])));
		}
		return stringBuilder.toString();
	}

	public static String userDeleteFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();

		addTokenA38(stringBuilder);

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
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
			stringBuilder.append(":d_");
			stringBuilder.append(aGroup);
		}

		UserTokens tokens = securityTokenManager.getTokens(user);
		addAuthsTokens(stringBuilder, user, UserAuthorizationsUtils.DELETE_ACCESS);
		addTokens(stringBuilder, tokens.getAllowTokens(), 'd');
		addTokens(stringBuilder, tokens.getShareAllowTokens(), 'd');
		addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		return stringBuilder.toString();
	}

	private static void addAuthsTokens(StringBuilder stringBuilder, User user, AuthorizationDetailsFilter filter) {

		//Specific auths are excluded, they are handled with tokens
		KeySetMap<String, String> tokens = UserAuthorizationsUtils.retrieveUserTokens(user, false, filter);

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
