package com.constellio.model.services.search.query;

import java.util.List;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.security.SecurityTokenManager.UserTokens;
import com.constellio.model.services.users.UserServices;

public class FilterUtils {

	public static String multiCollectionUserReadFilter(UserCredential user, UserServices userServices,
			SecurityTokenManager securityTokenManager) {
		StringBuilder filter = new StringBuilder();
		if (user.getCollections().isEmpty()) {
			addTokenA38(filter);
		} else {
			filter.append("(");
			boolean firstCollection = true;
			for (String collection : user.getCollections()) {
				if (!firstCollection) {
					filter.append(" OR ");
				}
				firstCollection = false;
				User userInCollection = userServices.getUserInCollection(user.getUsername(), collection);
				filter.append("(");
				filter.append(userReadFilter(userInCollection, securityTokenManager));
				filter.append(")");

			}
			filter.append(")");
		}
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

		UserTokens tokens = securityTokenManager.getTokens(user);
		addTokens(stringBuilder, tokens.getAllowTokens(), 'w');
		addTokens(stringBuilder, tokens.getShareAllowTokens(), 'w');
		addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		return stringBuilder.toString();
	}

	public static String userReadFilter(User user, SecurityTokenManager securityTokenManager) {
		StringBuilder stringBuilder = new StringBuilder();

		addTokenA38(stringBuilder);
		if (user.hasCollectionReadAccess() || user.hasCollectionDeleteAccess() || user.hasCollectionWriteAccess()) {
			stringBuilder.append(" OR ");
			stringBuilder.append(Schemas.COLLECTION.getDataStoreCode());
			stringBuilder.append(":");
			stringBuilder.append(user.getCollection());
		}

		UserTokens tokens = securityTokenManager.getTokens(user);
		addTokens(stringBuilder, tokens.getAllowTokens(), 'r');
		addTokens(stringBuilder, tokens.getShareAllowTokens(), 'r');
		addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		stringBuilder.append(" OR ");
		stringBuilder.append(Schemas.TOKENS.getDataStoreCode());
		stringBuilder.append(":");
		stringBuilder.append(Record.PUBLIC_TOKEN);
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

		UserTokens tokens = securityTokenManager.getTokens(user);
		addTokens(stringBuilder, tokens.getAllowTokens(), 'd');
		addTokens(stringBuilder, tokens.getShareAllowTokens(), 'd');
		addPublicTypes(stringBuilder, securityTokenManager.getSchemaTypesWithoutSecurity());
		return stringBuilder.toString();
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
