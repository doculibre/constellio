package com.constellio.model.entities.records.wrappers;

import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.SecurityTokenManager;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static java.util.Arrays.asList;

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
			if (tokens.contains(prefix + aGroup) && user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsNoNegativeUserGroupTokens(User user, Record record, String role) {

		List<String> negativeTokensToCheck = Collections.emptyList();

		if (READ.equals(role)) {
			negativeTokensToCheck = Collections.singletonList("nr_");

		} else if (WRITE.equals(role)) {
			negativeTokensToCheck = asList("nr_", "nw_");

		} else if (DELETE.equals(role)) {
			negativeTokensToCheck = asList("nr_", "nd_");
		}

		List<String> tokens = record.getList(TOKENS);

		for (String negativeTokenToCheck : negativeTokensToCheck) {
			if (tokens.contains(negativeTokenToCheck + user.getId())) {
				return false;
			}

			for (String aGroup : user.getUserGroups()) {
				if (tokens.contains(negativeTokenToCheck + aGroup) && user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup)) {
					return false;
				}
			}
		}
		return true;
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

		boolean isIncluded(Authorization details);

	}

	public static AuthorizationDetailsFilter anyRole(final String... roles) {

		return new AuthorizationDetailsFilter() {

			@Override
			public boolean isIncluded(Authorization details) {
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
			public boolean isIncluded(Authorization details) {
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
		public boolean isIncluded(Authorization details) {
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
		public boolean isIncluded(Authorization details) {
			return details.getRoles().contains(READ) || details.getRoles().contains(WRITE) || details.getRoles().contains(DELETE);
		}
	};

	private static boolean isAccessRole(String role) {
		return role.equals(READ) || role.equals(WRITE) || role.equals(DELETE);
	}

	public static AuthorizationDetailsFilter WRITE_ACCESS = new AuthorizationDetailsFilter() {

		@Override
		public boolean isIncluded(Authorization details) {
			return details.getRoles().contains(WRITE);
		}
	};

	public static AuthorizationDetailsFilter DELETE_ACCESS = new AuthorizationDetailsFilter() {

		@Override
		public boolean isIncluded(Authorization details) {
			return details.getRoles().contains(DELETE);
		}
	};

	private static List<String> getPrincipalsIdsGivingAuthsTo(User user) {
		SchemasRecordsServices schemas = user.getRolesDetails().getSchemasRecordsServices();

		GroupAuthorizationsInheritance inheritance = schemas.getModelLayerFactory().getSystemConfigs()
				.getGroupAuthorizationsInheritance();

		List<String> principalsIdsToInclude = new ArrayList<>();
		principalsIdsToInclude.add(user.getId());
		if (inheritance == GroupAuthorizationsInheritance.FROM_CHILD_TO_PARENT) {
			for (String groupId : user.getUserGroups()) {
				Group group = schemas.getGroup(groupId);
				if (schemas.isGroupActive(group)) {
					principalsIdsToInclude.add(group.getId());
					principalsIdsToInclude.addAll(getActiveChildrensIn(group, schemas));
				}
			}
		} else {
			for (String groupId : user.getUserGroups()) {
				Group group = schemas.getGroup(groupId);
				if (schemas.isGroupActive(group)) {
					principalsIdsToInclude.addAll(group.getAncestors());
				}
			}
		}

		return principalsIdsToInclude;
	}

	private static List<String> getPrincipalsIdsGivingAuthsTo(Group group, SchemasRecordsServices schemas) {

		GroupAuthorizationsInheritance inheritance = schemas.getModelLayerFactory().getSystemConfigs()
				.getGroupAuthorizationsInheritance();

		List<String> principalsIdsToInclude = new ArrayList<>();
		if (inheritance == GroupAuthorizationsInheritance.FROM_CHILD_TO_PARENT) {
			if (schemas.isGroupActive(group)) {
				principalsIdsToInclude.add(group.getId());
				principalsIdsToInclude.addAll(getActiveChildrensIn(group, schemas));
			}
		} else {
			if (schemas.isGroupActive(group)) {
				principalsIdsToInclude.addAll(group.getAncestors());
			}
		}

		return principalsIdsToInclude;
	}

	private static List<String> getActiveChildrensIn(Group group, SchemasRecordsServices schemas) {
		List<String> ids = new ArrayList<>();

		for (Group aGroup : schemas.getAllGroups()) {
			if (group.getId().equals(aGroup.getParent())) {
				if (schemas.isGroupActive(aGroup)) {
					ids.add(aGroup.getId());
					ids.addAll(getActiveChildrensIn(aGroup, schemas));
				}
			}
		}

		return ids;
	}

	public static Set<String> getAuthsReceivedBy(User user) {
		List<String> principalsIdsToInclude = getPrincipalsIdsGivingAuthsTo(user);
		Set<String> authsId = new HashSet<>();

		//TODO Security model improvement
		for (Authorization auth : user.getRolesDetails().getSchemasRecordsServices().getAllAuthorizationsInUnmodifiableState()) {
			if (CollectionUtils.containsAny(auth.getPrincipals(), principalsIdsToInclude)) {
				authsId.add(auth.getId());
			}
		}

		return authsId;
	}

	public static Set<String> getAuthsReceivedBy(Group group, SchemasRecordsServices schemas) {
		List<String> principalsIdsToInclude = getPrincipalsIdsGivingAuthsTo(group, schemas);
		Set<String> authsId = new HashSet<>();

		//TODO Security model improvement
		for (Authorization auth : schemas.getAllAuthorizationsInUnmodifiableState()) {
			if (CollectionUtils.containsAny(auth.getPrincipals(), principalsIdsToInclude)) {
				authsId.add(auth.getId());
			}
		}

		return authsId;
	}

	public static KeySetMap<String, String> retrieveUserTokens(User user,
															   boolean includeSpecifics,
															   AuthorizationDetailsFilter filter) {

		Set<String> authsId = getAuthsReceivedBy(user);
		KeySetMap<String, String> tokens = new KeySetMap<>();

		for (String authId : authsId) {
			try {
				Authorization authorizationDetails = user.getAuthorizationDetail(authId);
				if (authorizationDetails.isActiveAuthorization() && filter.isIncluded(authorizationDetails)
					&& (!Authorization.isSecurizedSchemaType(authorizationDetails.getTargetSchemaType()) || includeSpecifics)) {
					tokens.add(authorizationDetails.getTarget(), authId);
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.warn("User " + user.getUsername() + "' has an authorization without details : " + authId);
			}
		}

		return tokens;
	}


	public static Set<String> getRolesOnRecord(User user, Record record) {
		Set<String> auths = getMatchingAuthorizationIncludingSpecifics(user, record, ROLE_AUTHS);

		Set<String> roles = new HashSet<>();
		for (String authId : auths) {
			Authorization authorizationDetails = user.getAuthorizationDetail(authId);
			for (String role : authorizationDetails.getRoles()) {
				if (!isAccessRole(role)) {
					roles.add(role);
				}
			}
		}

		return roles;
	}

	public static Set<String> getRolesSpecificallyOnRecord(User user, Record record) {
		Set<String> auths = getMatchingAuthorizationIncludingSpecifics(user, record, ROLE_AUTHS);

		Set<String> roles = new HashSet<>();
		for (String authId : auths) {
			Authorization authorizationDetails = user.getAuthorizationDetail(authId);
			for (String role : authorizationDetails.getRoles()) {
				if (!isAccessRole(role) && record.getId().equals(authorizationDetails.getTarget())) {
					roles.add(role);
				}
			}
		}

		return roles;
	}

	public static boolean hasMatchingAuthorizationIncludingSpecifics(User user, Record record,
																	 AuthorizationDetailsFilter filter) {
		KeySetMap<String, String> tokens = retrieveUserTokens(user, true, filter);

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

	public static Set<String> getMatchingAuthorizationIncludingSpecifics(User user, Record record,
																		 AuthorizationDetailsFilter filter) {
		KeySetMap<String, String> tokens = retrieveUserTokens(user, true, filter);

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


