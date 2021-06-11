package com.constellio.model.entities.records.wrappers;

import com.constellio.data.dao.dto.records.RecordDTOMode;
import com.constellio.data.dao.dto.records.RecordId;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.SecurityTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.records.Record.GetMetadataOption.DIRECT_GET_FROM_DTO;
import static com.constellio.model.entities.records.Record.GetMetadataOption.RARELY_HAS_VALUE;
import static com.constellio.model.entities.schemas.Schemas.TOKENS;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class UserAuthorizationsUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthorizationsUtils.class);

	public static boolean containsAnyUserGroupTokens(User user, Record record, String role) {

		List<String> tokens = record.getList(TOKENS, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE);

		if (tokens.isEmpty()) {
			return false;
		}

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

		if (tokens.contains(prefix + user.getId())) {
			return true;
		}

		for (String aGroup : user.getUserGroups()) {
			if (tokens.contains(prefix + aGroup) && user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup, user.getCollection())) {
				return true;
			}
		}

		return false;
	}

	public static boolean containsNoNegativeUserGroupTokens(User user, Record record, String role) {

		if (user.getRolesDetails().getSecurityModel().hasNoNegativeAuth()) {
			return true;
		}

		List<String> tokens = record.getList(TOKENS, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE);
		if (tokens.isEmpty()) {
			return true;
		}

		List<String> negativeTokensToCheck = Collections.emptyList();

		if (READ.equals(role)) {
			negativeTokensToCheck = Collections.singletonList("nr_");

		} else if (WRITE.equals(role)) {
			negativeTokensToCheck = asList("nr_", "nw_");

		} else if (DELETE.equals(role)) {
			negativeTokensToCheck = asList("nr_", "nd_");
		}

		for (String negativeTokenToCheck : negativeTokensToCheck) {
			if (tokens.contains(negativeTokenToCheck + user.getId())) {
				return false;
			}

			for (String aGroup : user.getUserGroups()) {
				if (tokens.contains(negativeTokenToCheck + aGroup) && user.getRolesDetails().getSchemasRecordsServices().isGroupActive(aGroup, user.getCollection())) {
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

		default int getCacheKey() {
			return -1;
		}

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
		public int getCacheKey() {
			return 0;
		}

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
		public int getCacheKey() {
			return 1;
		}

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

	public static List<String> getPrincipalsIdsGivingAuthsTo(User user) {
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

	public static List<String> getPrincipalsIdsGivingAuthsTo(Group group, SchemasRecordsServices schemas) {

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
				principalsIdsToInclude.add(group.getId());
				principalsIdsToInclude.addAll(group.getAncestors());
			}
		}

		return principalsIdsToInclude;
	}

	private static List<String> getActiveChildrensIn(Group group, SchemasRecordsServices schemas) {
		List<String> ids = new ArrayList<>();

		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.group.schemaType())
				.where(schemas.group.parent()).isEqualTo(group.getId()));
		for (Group aGroup : schemas.searchGroups(query)) {
			if (group.getId().equals(aGroup.getParent())) {
				if (schemas.isGroupActive(aGroup)) {
					ids.add(aGroup.getId());
					ids.addAll(getActiveChildrensIn(aGroup, schemas));
				}
			}
		}

		return ids;
	}

	public static KeySetMap<String, String> retrieveUserTokens(User user,
															   boolean includeSpecifics,
															   AuthorizationDetailsFilter filter) {


		SecurityModel securityModel = user.getRolesDetails().getSecurityModel();
		return securityModel.retrieveUserTokens(user, includeSpecifics, filter);

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

		//if (record.getRecordDTOMode() == RecordDTOMode.SUMMARY) {

		List<Integer> attachedAncestorsIntIDs = record.getList(Schemas.ATTACHED_PRINCIPAL_ANCESTORS_INT_IDS, DIRECT_GET_FROM_DTO);
		List<String> allRemovedAuths = record.getList(Schemas.ALL_REMOVED_AUTHS, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE);

		for (Map.Entry<String, Set<String>> token : tokens.getMapEntries()) {
			if (attachedAncestorsIntIDs.contains(RecordId.toId(token.getKey()).intValue())) {
				for (String auth : token.getValue()) {
					if (!allRemovedAuths.contains(auth)) {
						return true;
					}
				}
			}
		}

		//		} else {
		//			List<String> attachedAncestors = record.<String>getList(Schemas.ATTACHED_ANCESTORS);
		//			List<String> allRemovedAuths = record.<String>getList(Schemas.ALL_REMOVED_AUTHS);
		//
		//			for (Map.Entry<String, Set<String>> token : tokens.getMapEntries()) {
		//				if (attachedAncestors.contains(token.getKey())) {
		//					for (String auth : token.getValue()) {
		//						if (!allRemovedAuths.contains(auth)) {
		//							return true;
		//						}
		//					}
		//				}
		//			}
		//		}
		return false;
	}

	public static Set<String> getMatchingAuthorizationIncludingSpecifics(User user, Record record,
																		 AuthorizationDetailsFilter filter) {
		KeySetMap<String, String> tokens = retrieveUserTokens(user, true, filter);
		Set<String> authIds = new HashSet<>();
		if (record.getRecordDTOMode() == RecordDTOMode.SUMMARY) {

			List<Integer> attachedAncestorsIntIDs = record.getList(Schemas.PRINCIPALS_ANCESTORS_INT_IDS);
			List<String> allRemovedAuths = record.<String>getList(Schemas.ALL_REMOVED_AUTHS);

			for (Map.Entry<String, Set<String>> token : tokens.getMapEntries()) {
				if (attachedAncestorsIntIDs.contains(RecordId.toId(token.getKey()).intValue())) {
					for (String auth : token.getValue()) {
						if (!allRemovedAuths.contains(auth)) {
							authIds.add(auth);
						}
					}
				}
			}
		} else {
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
		}
		return authIds;
	}

}


