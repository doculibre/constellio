package com.constellio.model.entities.records.wrappers;

import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationDetails;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.security.SecurityTokenManager;

public class UserAuthorizationsUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthorizationsUtils.class);

	public static boolean containsAnyUserGroupTokens(User user, Record record, String role) {

		String prefix;

		if (READ.equals(role)) {
			prefix = "r_";
		} else if (WRITE.equals(role)) {
			prefix = "w_";
		} else if (DELETE.equals(role)) {
			prefix = "d_";
		} else {
			prefix = role;
		}

		List<String> tokens = record.getList(TOKENS);

		if (tokens.contains(prefix + user.getId())) {
			return true;
		}

		for (String aGroup : user.getUserGroups()) {
			if (tokens.contains(prefix + aGroup)) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsAUserToken(User user, Record record) {
		SecurityTokenManager securityTokenManager = user.getRolesDetails().getSchemasRecordsServices().getModelLayerFactory()
				.getSecurityTokenManager();
		for (String token : securityTokenManager.getTokens(user).getAllowTokens()) {
			if (record.getList(TOKENS).contains(token)) {
				return true;
			}
		}

		return false;
	}

	public static interface AuthorizationDetailsFilter {

		boolean isIncluded(AuthorizationDetails details);

	}

	public static AuthorizationDetailsFilter anyRole(final String... roles) {

		return new AuthorizationDetailsFilter() {

			@Override
			public boolean isIncluded(AuthorizationDetails details) {
				for (String role : roles) {
					if (details.getRoles().contains(role)) {
						return true;
					}
				}

				return false;
			}
		};

	}

	public static AuthorizationDetailsFilter allRoles(final String... roles) {

		return new AuthorizationDetailsFilter() {

			@Override
			public boolean isIncluded(AuthorizationDetails details) {
				for (String role : roles) {
					if (!details.getRoles().contains(role)) {
						return false;
					}
				}

				return true;
			}
		};

	}

	public static AuthorizationDetailsFilter ROLE_AUTHS = new AuthorizationDetailsFilter() {

		@Override
		public boolean isIncluded(AuthorizationDetails details) {
			for (String role : details.getRoles()) {
				if (!isAccessRole(role)) {
					return true;
				}
			}
			return false;
		}
	};

	public static AuthorizationDetailsFilter READ_ACCESS = new AuthorizationDetailsFilter() {

		@Override
		public boolean isIncluded(AuthorizationDetails details) {
			return details.getRoles().contains(READ) || details.getRoles().contains(WRITE) || details.getRoles().contains(DELETE);
		}
	};

	private static boolean isAccessRole(String role) {
		return role.equals(READ) || role.equals(WRITE) || role.equals(DELETE);
	}

	public static AuthorizationDetailsFilter WRITE_ACCESS = new AuthorizationDetailsFilter() {

		@Override
		public boolean isIncluded(AuthorizationDetails details) {
			return details.getRoles().contains(WRITE);
		}
	};

	public static AuthorizationDetailsFilter DELETE_ACCESS = new AuthorizationDetailsFilter() {

		@Override
		public boolean isIncluded(AuthorizationDetails details) {
			return details.getRoles().contains(DELETE);
		}
	};

	public static KeySetMap<String, String> retrieveUserTokens(User user, AuthorizationDetailsFilter filter) {

		KeySetMap<String, String> tokens = new KeySetMap<>();

		for (String authId : user.getAllUserAuthorizations()) {
			try {
				AuthorizationDetails authorizationDetails = user.getAuthorizationDetail(authId);
				if (authorizationDetails.isActiveAuthorization() && filter.isIncluded(authorizationDetails)) {
					tokens.add(authorizationDetails.getTarget(), authId);
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("User " + user.getUsername() + "' has an authorization without details : " + authId);
			}
		}

		return tokens;
	}

	public static Set<String> getRolesOnRecord(User user, Record record) {
		Set<String> auths = getMatchingAuthorization(user, record, ROLE_AUTHS);

		Set<String> roles = new HashSet<>();
		for (String authId : auths) {
			AuthorizationDetails authorizationDetails = user.getAuthorizationDetail(authId);
			for (String role : authorizationDetails.getRoles()) {
				if (!isAccessRole(role)) {
					roles.add(role);
				}
			}
		}

		return roles;
	}

	public static boolean hasMatchingAuthorization(User user, Record record, AuthorizationDetailsFilter filter) {
		KeySetMap<String, String> tokens = retrieveUserTokens(user, filter);

		List<String> attachedAncestors = record.<String>getList(Schemas.ATTACHED_ANCESTORS);
		List<String> allRemovedAuths = record.<String>getList(Schemas.ALL_REMOVED_AUTHS);

		for (Map.Entry<String, Set<String>> token : tokens.getMapEntries()) {
			if (attachedAncestors.contains(token.getKey())) {
				for (String auth : token.getValue()) {
					if (!allRemovedAuths.contains(auth)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static Set<String> getMatchingAuthorization(User user, Record record, AuthorizationDetailsFilter filter) {
		KeySetMap<String, String> tokens = retrieveUserTokens(user, filter);

		Set<String> authIds = new HashSet<>();
		List<String> attachedAncestors = record.<String>getList(Schemas.ATTACHED_ANCESTORS);
		List<String> allRemovedAuths = record.<String>getList(Schemas.ALL_REMOVED_AUTHS);

		for (Map.Entry<String, Set<String>> token : tokens.getMapEntries()) {
			if (attachedAncestors.contains(token.getKey())) {
				for (String auth : token.getValue()) {
					if (!allRemovedAuths.contains(auth)) {
						authIds.add(auth);
					}
				}
			}
		}

		return authIds;
	}

}


