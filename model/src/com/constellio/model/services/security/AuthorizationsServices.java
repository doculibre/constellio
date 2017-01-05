package com.constellio.model.services.security;

import static com.constellio.data.utils.LangUtils.withoutDuplicatesAndNulls;
import static com.constellio.model.entities.schemas.Schemas.AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.Schemas.IS_DETACHED_AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.Schemas.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorization;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasExcept;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy;
import static com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords;
import static com.constellio.model.services.security.AuthorizationsServicesRuntimeException.RecordServicesErrorDuringOperation;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.AuthorizationDetails;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationResponse;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.RecordUtils;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotDetachConcept;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidPrincipalsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchPrincipalWithUsername;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.services.users.UserServices;

public class AuthorizationsServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationsServices.class);

	ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager schemasManager;
	private LoggingServices loggingServices;
	UserServices userServices;
	AuthorizationDetailsManager manager;
	RolesManager rolesManager;
	TaxonomiesManager taxonomiesManager;
	RecordServices recordServices;
	SearchServices searchServices;
	UniqueIdGenerator uniqueIdGenerator;

	public AuthorizationsServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.manager = modelLayerFactory.getAuthorizationDetailsManager();
		this.rolesManager = modelLayerFactory.getRolesManager();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.userServices = modelLayerFactory.newUserServices();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.uniqueIdGenerator = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator();

	}

	@Deprecated
	public String getAuthorizationIdByIdWithoutPrefix(String collection, String idWithoutPrefix) {
		AuthorizationDetails authDetails = manager.getByIdWithoutPrefix(collection, idWithoutPrefix);
		if (authDetails == null) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId(idWithoutPrefix);
		}
		return authDetails.getId();
	}

	public Authorization getAuthorization(String collection, String id) {
		if (collection == null) {
			throw new IllegalArgumentException("Collection is null");
		}
		if (id == null) {
			throw new IllegalArgumentException("id is null");
		}
		AuthorizationDetails authDetails = manager.get(collection, id);
		if (authDetails == null) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId(id);
		}
		List<String> grantedToPrincipals = findAllPrincipalIdsWithAuthorization(authDetails);
		List<String> grantedOnRecords = findAllRecordIdsWithAuthorizations(authDetails);
		return new Authorization(authDetails, grantedToPrincipals, grantedOnRecords);
	}

	public List<User> getUsersWithGlobalPermissionInCollection(String permission, String collection) {
		return getUsersWithGlobalPermissionInCollectionExcludingRoles(permission, collection, new ArrayList<String>());
	}

	public List<User> getUsersWithGlobalPermissionInCollectionExcludingRoles(String permission, String collection,
			List<String> excludingRoles) {

		Roles roles = rolesManager.getCollectionRoles(collection);
		List<String> rolesGivingPermission = toRolesCodes(roles.getRolesGivingPermission(permission));
		rolesGivingPermission.removeAll(excludingRoles);

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);

		MetadataSchemaType userSchemaType = types.getSchemaType(User.SCHEMA_TYPE);
		Metadata userAllRoles = userSchemaType.getDefaultSchema().getMetadata(User.ALL_ROLES);

		List<Record> foundRecords = searchServices.search(new LogicalSearchQuery()
				.setCondition(from(userSchemaType).where(userAllRoles).isIn(rolesGivingPermission)));

		List<User> users = new ArrayList<>();
		for (Record record : foundRecords) {
			users.add(new User(record, types, roles));
		}
		return users;
	}

	public List<User> getUsersWithPermissionOnRecord(String permission, Record concept) {
		Roles roles = rolesManager.getCollectionRoles(concept.getCollection());
		List<Role> rolesGivingPermission = roles.getRolesGivingPermission(permission);
		List<String> tokens = concept.get(Schemas.TOKENS);
		List<String> tokensReceivingPermission = new ArrayList<>();
		for (String userToken : tokens) {
			for (String authorizationRoleCode : userToken.split("_")[1].split(",")) {
				Role role = roles.getRole(authorizationRoleCode);
				if (role != null && role.getOperationPermissions().contains(permission)) {
					tokensReceivingPermission.add(userToken);
				}
			}
		}

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(concept.getCollection());
		MetadataSchemaType userSchemaType = types.getSchemaType(User.SCHEMA_TYPE);
		Metadata userTokens = userSchemaType.getDefaultSchema().getMetadata(User.USER_TOKENS);
		Metadata userAllRoles = userSchemaType.getDefaultSchema().getMetadata(User.ALL_ROLES);

		List<Record> foundRecords = searchServices.search(new LogicalSearchQuery()
				.setCondition(from(userSchemaType).where(userTokens).isIn(tokensReceivingPermission)
						.orWhere(userAllRoles).isIn(toRolesCodes(rolesGivingPermission))));

		List<User> users = new ArrayList<>();
		for (Record record : foundRecords) {
			users.add(new User(record, types, roles));
		}
		return users;
	}

	public List<User> getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(String permission, Record concept) {
		Roles roles = rolesManager.getCollectionRoles(concept.getCollection());
		List<String> authorizationsGivingPermission = new ArrayList<>();
		List<String> authorizationIds = concept.get(AUTHORIZATIONS);
		for (String authorizationId : authorizationIds) {
			Authorization authorization = getAuthorization(concept.getCollection(), authorizationId);
			for (String authorizationRoleCode : authorization.getDetail().getRoles()) {
				Role role = roles.getRole(authorizationRoleCode);
				if (role != null && role.getOperationPermissions().contains(permission)) {
					authorizationsGivingPermission.add(authorizationId);
					break;
				}
			}

		}

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(concept.getCollection());
		MetadataSchemaType userSchemaType = types.getSchemaType(User.SCHEMA_TYPE);
		Metadata userAllAuthorizations = userSchemaType.getDefaultSchema().getMetadata(User.ALL_USER_AUTHORIZATIONS);

		List<User> users = new ArrayList<>();

		for (Record record : searchServices.search(new LogicalSearchQuery()
				.setCondition(from(userSchemaType).where(userAllAuthorizations).isIn(authorizationsGivingPermission)))) {

			User user = new User(record, types, roles);
			if (!user.has(permission).on(record)) {
				throw new RuntimeException("has method is failing");
			}

			users.add(user);
		}
		return users;
	}

	public List<String> getConceptsForWhichUserHasPermission(String permission, User user) {
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(user.getCollection());
		if (principalTaxonomy == null) {
			return new ArrayList<>();
		}
		List<MetadataSchemaType> conceptTypes = schemasManager.getSchemaTypes(user.getCollection())
				.getSchemaTypesWithCode(principalTaxonomy.getSchemaTypes());

		if (user.has(permission).globally()) {
			return searchServices.searchRecordIds(new LogicalSearchQuery(from(conceptTypes).returnAll()));
		}

		Roles roles = rolesManager.getCollectionRoles(user.getCollection());
		List<String> userTokens = user.getUserTokens();
		List<String> tokensGivingPermission = new ArrayList<>();
		for (String userToken : userTokens) {
			for (String authorizationRoleCode : userToken.split("_")[1].split(",")) {
				Role role = roles.getRole(authorizationRoleCode);
				if (role != null && role.getOperationPermissions().contains(permission)) {
					tokensGivingPermission.add(userToken);
				}
			}
		}

		return searchServices.searchRecordIds(new LogicalSearchQuery()
				.setCondition(from(conceptTypes).where(Schemas.TOKENS).isIn(tokensGivingPermission)));
	}

	public List<User> getUsersWithRoleForRecord(String role, Record record) {
		List<User> users = new ArrayList<>();
		List<Authorization> recordAuths = getRecordAuthorizations(record);
		for (Authorization auth : recordAuths) {
			if (auth.getDetail().getRoles().contains(role)) {
				List<String> principals = auth.getGrantedToPrincipals();
				List<Record> principalRecords = recordServices.getRecordsById(auth.getDetail().getCollection(), principals);
				if (principals.size() != principalRecords.size()) {
					throw new InvalidPrincipalsIds(principalRecords, principals);
				}
				MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
				Roles roles = rolesManager.getCollectionRoles(record.getCollection());
				for (Record principalRecord : principalRecords) {
					if (principalRecord.getSchemaCode().equals(Group.SCHEMA_TYPE + "_default")) {
						List<Record> usersInGroupRecord = getUserRecordsInGroup(principalRecord);
						for (Record userRecord : usersInGroupRecord) {
							users.add(new User(userRecord, types, roles));
						}
					} else if (principalRecord.getSchemaCode().equals(User.SCHEMA_TYPE + "_default")) {
						users.add(new User(principalRecord, types, roles));
					}
				}
			}
		}
		return users;
	}

	public List<Record> getUserRecordsInGroup(Record groupRecord) {
		MetadataSchema userSchema = schemasManager.getSchemaTypes(groupRecord.getCollection())
				.getSchema(User.SCHEMA_TYPE + "_default");
		Metadata userGroupsMetadata = userSchema.getMetadata(User.GROUPS);
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(userSchema)
				.where(userGroupsMetadata).isContaining(asList(groupRecord.getId()));
		return searchServices.search(new LogicalSearchQuery(condition));
	}

	/**
	 * Add an authorization on a record
	 * @param authorization
	 * @return
	 */
	public String add(Authorization authorization) {
		return add(authorization, null);
	}

	/**
	 * Add an authorization on a record. The authorization will be logged as created by the given user
	 * @param authorization Authorization to add
	 * @param userAddingTheAuth
	 * @return The new authorization's id
	 */
	public String add(Authorization authorization, User userAddingTheAuth) {

		List<Record> records = getAuthorizationGrantedOnRecords(authorization);
		List<Record> principals = getAuthorizationGrantedToPrincipals(authorization);

		AuthorizationDetails authorizationDetail = authorization.getDetail();
		String authId = authorizationDetail.getId();

		if (authorizationDetail.isFutureAuthorization()) {
			authId = "-" + authId;
			authorizationDetail = new AuthorizationDetails(authorizationDetail.getCollection(), authId,
					authorizationDetail.getRoles(), authorizationDetail.getStartDate(), authorizationDetail.getEndDate(), false);
		}
		manager.add(authorizationDetail);

		addAuthorizationToRecords(records, authId);
		addAuthorizationToPrincipals(principals, authId);
		saveRecordsTargettedByAuthorization(records, principals);

		if (userAddingTheAuth != null) {
			loggingServices.grantPermission(authorization, userAddingTheAuth);
		}

		return authId;
	}

	public void delete(AuthorizationDeleteRequest request) {

		List<String> authId = asList(request.getAuthId());
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(request.getCollection())
				.where(AUTHORIZATIONS).isContaining(authId)
				.orWhere(REMOVED_AUTHORIZATIONS).isContaining(authId));
		List<Record> records = searchServices.search(query);

		if (request.getExecutedBy() != null) {
			try {
				Authorization auth = getAuthorization(request.getCollection(), request.getAuthId());
				loggingServices.deletePermission(auth, request.getExecutedBy());
			} catch (NoSuchAuthorizationWithId e) {
				//No problemo
			}
		}

		for (Record record : records) {
			removeAuthorizationOnRecord(request.getAuthId(), record, request.isReattachIfLastAuthDeleted());
			removeRemovedAuthorizationOnRecord(request.getAuthId(), record);
		}
		try {
			recordServices.executeHandlingImpactsAsync(new Transaction(records));
		} catch (RecordServicesException e) {
			throw new RecordServicesErrorDuringOperation("delete", e);
		}

		try {
			AuthorizationDetails details = manager.get(request.getCollection(), request.getAuthId());
			if (details != null) {
				manager.remove(details);
			}
		} catch (NoSuchAuthorizationWithId e) {
			//No problemo
		}
	}

	/**
	 * Modify an authorization on a specific record. The request will be handled differently depending
	 * if the record is or not the root target of the authorization. This service will never detach/reattach or reset records.
	 * @param request The request to execute
	 * @return A response with some informations
	 */

	public AuthorizationModificationResponse execute(AuthorizationModificationRequest request) {

		Authorization authorization = getAuthorization(request.getCollection(), request.getAuthorizationId());
		Record record = recordServices.getDocumentById(request.getRecordId());

		AuthorizationModificationResponse response = executeWithoutLogging(request, authorization, record);

		if (request.getExecutedBy() != null) {

			Authorization authorizationAfter;
			if (response.getIdOfAuthorizationCopy() != null) {
				authorizationAfter = getAuthorization(request.getCollection(), response.getIdOfAuthorizationCopy());
			} else {
				authorizationAfter = getAuthorization(request.getCollection(), authorization.getDetail().getId());
			}

			try {
				loggingServices.modifyPermission(authorization, authorizationAfter, record, request.getExecutedBy());
			} catch (NoSuchAuthorizationWithId e) {
				//No problemo
			}
		}

		return response;
	}

	private AuthorizationModificationResponse executeWithoutLogging(AuthorizationModificationRequest request,
			Authorization authorization, Record record) {
		if (request.isRemovedOnRecord()) {
			String authId = authorization.getDetail().getId();

			if (record.getList(AUTHORIZATIONS).contains(authId)) {
				removeAuthorizationOnRecord(authId, record, true);
			}
			List<Object> inheritedAuths = record.getList(Schemas.INHERITED_AUTHORIZATIONS);
			if (inheritedAuths.contains(authId)) {
				removeInheritedAuthorizationOnRecord(authId, record);
			}

			try {
				recordServices.execute(new Transaction(record));
			} catch (RecordServicesException e) {
				throw new RecordServicesErrorDuringOperation("removeAuthorizationOnRecord", e);
			}
			return new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

		} else {

			List<String> recordAuthorizations = record.getList(Schemas.ALL_AUTHORIZATIONS);
			if (!recordAuthorizations.contains(request.getAuthorizationId())) {
				throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord(request.getAuthorizationId(),
						record);
			}

			if (request.getRecordId().equals(authorization.getGrantedOnRecord())) {
				executeOnAuthorization(request, authorization, record);
				return new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

			} else {
				String copyId = inheritedToSpecific(record.getCollection(), authorization.getDetail().getId());
				record.addValueToList(REMOVED_AUTHORIZATIONS, authorization.getDetail().getId());
				record.addValueToList(AUTHORIZATIONS, copyId);

				Transaction transaction = new Transaction();
				transaction.getRecordUpdateOptions().setValidationsEnabled(false);
				transaction.add(record);
				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}

				Authorization authorizationCopy = getAuthorization(record.getCollection(), copyId);
				executeOnAuthorization(request, authorizationCopy, record);

				return new AuthorizationModificationResponse(false, copyId,
						Collections.singletonMap(authorization.getDetail().getId(), copyId));

			}
		}
	}

	/**
	 * Detach a record from its parent, creating specific authorizations that are equal to those before the detach.
	 * - Future modifications on a parent record won't affect the detached record.
	 * - If the detached record is reassigned to a new parent, there will be no effects on authorizations
	 *
	 * @param record A securized record to detach
	 * @return A mapping of previous authorization ids to the new authorizations created by this service
	 */
	public Map<String, String> detach(Record record) {
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
		if (principalTaxonomy.getSchemaTypes().contains(record.getTypeCode())) {
			throw new CannotDetachConcept(record.getId());
		}

		if (Boolean.TRUE.equals(record.get(Schemas.IS_DETACHED_AUTHORIZATIONS))) {
			return Collections.emptyMap();

		} else {
			Map<String, String> originalToCopyMap = setupAuthorizationsForDetachedRecord(record);
			saveRecordsTargettedByAuthorization(Arrays.asList(record), new ArrayList<Record>());
			return originalToCopyMap;
		}
	}

	/**
	 * Return all authorizations targetting a given Record, which may be a user or securised Record.
	 * Authorizations may be inherited or assigned directly to the record
	 *
	 * @param record User or a securised record
	 * @return Authorizations
	 */
	public List<Authorization> getRecordAuthorizations(RecordWrapper recordWrapper) {
		return getRecordAuthorizations(recordWrapper.getWrappedRecord());
	}

	/**
	 * Return all authorizations targetting a given Record, which may be a user or securised Record.
	 * Authorizations may be inherited or assigned directly to the record
	 *
	 * @param record User or a securised record
	 * @return Authorizations
	 */
	public List<Authorization> getRecordAuthorizations(Record record) {

		List<String> authIds;
		if (User.DEFAULT_SCHEMA.equals(record.getSchemaCode())) {

			Metadata allUserAuthorizations = schemasManager.getSchemaTypes(record.getCollection()).getSchema(User.DEFAULT_SCHEMA)
					.getMetadata(User.ALL_USER_AUTHORIZATIONS);

			authIds = record.getList(allUserAuthorizations);

		} else {
			authIds = record.getList(Schemas.ALL_AUTHORIZATIONS);

		}

		List<Authorization> authorizations = new ArrayList<>();
		for (String authId : authIds) {
			AuthorizationDetails authDetails = manager.get(record.getCollection(), authId);
			if (authDetails != null) {
				List<String> grantedToPrincipals = findAllPrincipalIdsWithAuthorization(authDetails);
				List<String> grantedOnRecords = findAllRecordIdsWithAuthorizations(authDetails);
				authorizations.add(new Authorization(authDetails, grantedToPrincipals, grantedOnRecords));
			} else {
				LOGGER.error("Missing authorization '" + authId + "'");
			}
		}
		return authorizations;
	}

	/**
	 * Reset a securized record.
	 * The resetted record will be reattached (inheriting all authorizations) and all its specific authorizations will be lost
	 * @param record The securized record
	 */
	public void reset(Record record) {

		List<String> authorizationIds = record.getList(AUTHORIZATIONS);
		List<AuthorizationDetails> authorizationDetailses = new ArrayList<>();
		record.set(AUTHORIZATIONS, null);
		record.set(REMOVED_AUTHORIZATIONS, null);
		record.set(IS_DETACHED_AUTHORIZATIONS, false);

		Transaction transaction = new Transaction();
		transaction.add(record);

		Set<String> principalIds = new HashSet<>();
		for (String authId : authorizationIds) {
			Authorization authorization = getAuthorization(record.getCollection(), authId);
			authorizationDetailses.add(authorization.getDetail());
			principalIds.addAll(authorization.getGrantedToPrincipals());
		}

		for (String principalId : principalIds) {
			Record principal = recordServices.getDocumentById(principalId);
			List<String> newAuth = new ArrayList<>(principal.<String>getList(AUTHORIZATIONS));
			newAuth.removeAll(authorizationIds);
			transaction.add(principal.set(Schemas.AUTHORIZATIONS, newAuth));
		}

		try {
			recordServices.execute(transaction);
		} catch (RecordServicesException e) {
			throw new RecordServicesErrorDuringOperation("reset", e);
		}

		for (AuthorizationDetails authorizationDetails : authorizationDetailses) {
			manager.remove(authorizationDetails);
		}

	}

	public boolean hasDeletePermissionOnPrincipalConceptHierarchy(User user, Record principalTaxonomyConcept,
			boolean includeRecords, MetadataSchemasManager schemasManager) {
		if (user == User.GOD) {
			return true;
		}
		List<String> paths = principalTaxonomyConcept.getList(Schemas.PATH);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(user.getCollection());
		validateRecordIsAPrincipalTaxonomyConcept(principalTaxonomyConcept, paths, principalTaxonomy);
		int numberOfRecords = 0;
		int numberOfRecordsWithUser = 0;

		LogicalSearchCondition condition;
		if (!includeRecords) {
			for (String schemaType : principalTaxonomy.getSchemaTypes()) {
				LogicalSearchQuery query = new LogicalSearchQuery();
				condition = from(schemasManager.getSchemaTypes(user.getCollection()).getSchemaType(schemaType))
						.where(Schemas.PATH).isStartingWithText(paths.get(0)).andWhere(Schemas.LOGICALLY_DELETED_STATUS)
						.isFalseOrNull();
				query.setCondition(condition);
				numberOfRecords += searchServices.searchRecordIds(query).size();
				query.filteredWithUserDelete(user);
				numberOfRecordsWithUser += searchServices.searchRecordIds(query).size();
			}
			return numberOfRecords == numberOfRecordsWithUser;
		} else {
			return hasPermissionOnHierarchy(user, principalTaxonomyConcept, false);
		}
	}

	public boolean hasDeletePermissionOnHierarchy(User user, Record record) {
		return hasPermissionOnHierarchy(user, record, false);
	}

	public boolean hasRestaurationPermissionOnHierarchy(User user, Record record) {
		return hasPermissionOnHierarchy(user, record, true);
	}

	public boolean hasDeletePermissionOnHierarchyNoMatterTheStatus(User user, Record record) {
		return hasPermissionOnHierarchy(user, record, null);
	}

	@Deprecated
	//After user.hasReadAccess instead
	public boolean canRead(User user, Record record) {
		return user.hasReadAccess().on(record);
	}

	@Deprecated
	//After user.hasWriteAccess instead
	public boolean canWrite(User user, Record record) {
		return user.hasWriteAccess().on(record);
	}

	@Deprecated
	//After user.hasDeleteAccess instead
	public boolean canDelete(User user, Record record) {
		return user.hasDeleteAccess().on(record);
	}

	private void executeOnAuthorization(AuthorizationModificationRequest request,
			Authorization authorization, Record record) {

		SchemasRecordsServices schemas = new SchemasRecordsServices(record.getCollection(), modelLayerFactory);

		if (request.getNewPrincipalIds() != null) {
			Transaction transaction = new Transaction();

			if (request.getNewPrincipalIds().isEmpty()) {
				throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
			}

			List<Record> newPrincipalsRecords = principalToRecords(schemas, request.getNewPrincipalIds());

			List<String> actualPrincipals = authorization.getGrantedToPrincipals();
			List<String> newPrincipals = new RecordUtils().toIdList(newPrincipalsRecords);

			RecordProvider recordProvider = new RecordProvider(recordServices, null, newPrincipalsRecords, null);

			ListComparisonResults<String> results = LangUtils.compare(actualPrincipals, newPrincipals);
			for (String newPrincipal : results.getNewItems()) {
				Record principalRecord = recordProvider.getRecord(newPrincipal);
				principalRecord.addValueToList(AUTHORIZATIONS, authorization.getDetail().getId());
				transaction.add(principalRecord);
			}

			for (String removedPrincipal : results.getRemovedItems()) {
				Record principalRecord = recordProvider.getRecord(removedPrincipal);
				principalRecord.removeValueFromList(AUTHORIZATIONS, authorization.getDetail().getId());
				transaction.add(principalRecord);
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

		}
	}

	private List<Record> principalToRecords(SchemasRecordsServices schemas, List<String> principals) {
		List<Record> records = new ArrayList<>();

		for (String principal : principals) {
			try {
				records.add(recordServices.getDocumentById(principal));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {

				List<MetadataSchemaType> types = asList(schemas.user.schemaType(), schemas.group.schemaType());
				Record record = searchServices.searchSingleResult(from(types).where(schemas.user.username()).isEqualTo(principal)
						.orWhere(schemas.group.code()).isEqualTo(principal));
				if (record == null) {
					throw new NoSuchPrincipalWithUsername(principal);
				}
				records.add(record);

			}
		}

		return records;
	}

	private List<String> findAllRecordIdsWithAuthorizations(AuthorizationDetails authDetails) {
		return new RecordUtils().toIdList(findAllRecordsWithAuthorizations(authDetails));
	}

	private List<Record> findAllRecordsWithAuthorizations(AuthorizationDetails authDetails) {
		List<Record> records = new ArrayList<>();
		LogicalSearchQuery query = new LogicalSearchQuery();

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(authDetails.getCollection());
		MetadataSchemaType userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
		MetadataSchemaType groupSchemaType = schemaTypes.getSchemaType(Group.SCHEMA_TYPE);

		query.setCondition(fromAllSchemasExcept(asList(userSchemaType, groupSchemaType)).where(AUTHORIZATIONS)
				.isContaining(asList(authDetails.getId())));
		records.addAll(searchServices.search(query));
		return records;
	}

	private List<String> findAllPrincipalIdsWithAuthorization(AuthorizationDetails authDetails) {
		List<String> principals = new ArrayList<>();

		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(authDetails.getCollection());
		MetadataSchemaType userSchemaType = schemaTypes.getSchemaType(User.SCHEMA_TYPE);
		MetadataSchemaType groupSchemaType = schemaTypes.getSchemaType(Group.SCHEMA_TYPE);

		List<Record> allUsers = searchServices.cachedSearch(new LogicalSearchQuery(from(userSchemaType).returnAll()));
		List<Record> allGroups = searchServices.cachedSearch(new LogicalSearchQuery(from(groupSchemaType).returnAll()));

		if (principals != null) {
			for (Record user : allUsers) {

				if (user != null && user.getList(AUTHORIZATIONS).contains(authDetails.getId())) {
					principals.add(user.getId());
				}
			}
			for (Record group : allGroups) {
				if (group != null && group.getList(AUTHORIZATIONS).contains(authDetails.getId())) {
					principals.add(group.getId());
				}
			}
		}

		return principals;
	}

	private List<Record> findAllPrincipalsWithAuthorization(AuthorizationDetails detail) {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(detail.getCollection());
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(Arrays.asList(types.getSchemaType(User.SCHEMA_TYPE), types.getSchemaType(Group.SCHEMA_TYPE)))
						.where(AUTHORIZATIONS).isEqualTo(detail.getId()));
		return searchServices.search(query);
	}

	private List<Role> getAllAuthorizationRoleForUser(User user) {
		List<String> allAuthorizations = new ArrayList<>();
		List<Role> allRoleAuthorization = new ArrayList<>();

		allAuthorizations.addAll(user.getAllUserAuthorizations());
		for (String groupId : user.getUserGroups()) {
			Group group = getGroupInCollectionById(groupId, user.getCollection());
			allAuthorizations.addAll(group.getAllAuthorizations());
		}

		for (String id : allAuthorizations) {
			List<String> rolesId = manager.get(user.getCollection(), id).getRoles();
			allRoleAuthorization = getRolesFromId(rolesId, user.getCollection());
		}

		return allRoleAuthorization;
	}

	private List<Role> getAllGlobalRoleForUser(User user) {
		List<Role> allGlobalRole = new ArrayList<>();

		allGlobalRole.addAll(getRolesFromId(user.getAllRoles(), user.getCollection()));
		for (String groupId : user.getUserGroups()) {
			Group group = getGroupInCollectionById(groupId, user.getCollection());

			allGlobalRole.addAll(getRolesFromId(group.getRoles(), user.getCollection()));
		}

		return allGlobalRole;
	}

	private List<Role> getRolesFromId(List<String> rolesId, String collection)
			throws RolesManagerRuntimeException {
		List<Role> roles = new ArrayList<>();
		for (String roleId : rolesId) {
			Role role = rolesManager.getRole(collection, roleId);
			roles.add(role);
		}

		return roles;
	}

	private Group getGroupInCollectionById(String groupId, String collection) {
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(collection);
		MetadataSchema groupSchema = schemaTypes.getSchema(Group.SCHEMA_TYPE + "_default");

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(groupSchema).where(Schemas.IDENTIFIER).is(groupId);

		return Group.wrapNullable(searchServices.searchSingleResult(condition), schemaTypes);
	}

	private boolean hasCollectionPermission(User user) {
		return user.hasCollectionReadAccess() || user.hasCollectionWriteAccess() || user.hasCollectionDeleteAccess();
	}

	private boolean hasPermissionOnHierarchy(User user, Record record, Boolean deleted) {

		if (user == User.GOD || user.hasCollectionDeleteAccess()) {
			return true;
		}

		List<String> paths = record.getList(Schemas.PATH);
		if (paths.isEmpty()) {
			return canDelete(user, record);
		}

		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition;
		if (deleted == null) {
			condition = fromAllSchemasIn(user.getCollection()).where(Schemas.PATH).isStartingWithText(paths.get(0));

		} else if (deleted) {
			condition = fromAllSchemasIn(user.getCollection()).where(Schemas.PATH).isStartingWithText(paths.get(0))
					.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
		} else {
			condition = fromAllSchemasIn(user.getCollection()).where(Schemas.PATH).isStartingWithText(paths.get(0))
					.andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		}
		query.setCondition(condition);
		int numberOfRecords = searchServices.searchRecordIds(query).size();
		query.filteredWithUserDelete(user);
		int numberOfRecordsWithUser = searchServices.searchRecordIds(query).size();
		return (numberOfRecords == numberOfRecordsWithUser && numberOfRecords != 0);
	}

	private void validateRecordIsAPrincipalTaxonomyConcept(Record principalTaxonomyConcept, List<String> paths,
			Taxonomy principalTaxonomy) {
		String schemaTypeCode = principalTaxonomyConcept.getSchemaCode().split("_")[0];
		if (!paths.get(0).contains(principalTaxonomy.getCode()) || !principalTaxonomy.getSchemaTypes().contains(schemaTypeCode)) {
			throw new AuthorizationsServicesRuntimeException.RecordIsNotAConceptOfPrincipalTaxonomy(
					principalTaxonomyConcept.getId(), principalTaxonomy.getCode());
		}
	}

	private List<Record> getAuthorizationGrantedToPrincipals(Authorization authorization) {
		List<String> principalIds = withoutDuplicatesAndNulls(authorization.getGrantedToPrincipals());
		if (principalIds.isEmpty() && !authorization.getDetail().isSynced()) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}
		List<Record> records = recordServices.getRecordsById(authorization.getDetail().getCollection(), principalIds);
		if (principalIds.size() != records.size()) {
			throw new InvalidPrincipalsIds(records, principalIds);
		}
		return records;
	}

	private void validateCanAssignAuthorization(Record record) {
		List<String> secondaryTaxonomySchemaTypes = taxonomiesManager.getSecondaryTaxonomySchemaTypes(record.getCollection());

		String schemaType = newSchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		if (secondaryTaxonomySchemaTypes.contains(schemaType)) {
			throw new CannotAddAuhtorizationInNonPrincipalTaxonomy();
		}

	}

	private List<Record> getAuthorizationGrantedOnRecords(Authorization authorization) {
		List<String> recordIds = withoutDuplicatesAndNulls(authorization.getGrantedOnRecords());
		if (recordIds.isEmpty()) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}
		List<Record> records = recordServices.getRecordsById(authorization.getDetail().getCollection(), recordIds);
		if (recordIds.size() != records.size()) {
			throw new InvalidTargetRecordsIds(records, recordIds);
		}
		return records;
	}

	private List<String> toRolesCodes(List<Role> rolesGivingPermission) {
		List<String> roleCodes = new ArrayList<>();

		for (Role role : rolesGivingPermission) {
			roleCodes.add(role.getCode());
		}

		return roleCodes;
	}

	private void saveRecordsTargettedByAuthorization(List<Record> records, List<Record> principals) {
		Transaction transaction = new Transaction();
		transaction.addUpdate(records);
		transaction.addUpdate(principals);
		try {
			recordServices.executeHandlingImpactsAsync(transaction);
		} catch (RecordServicesException e) {
			throw new RecordServicesErrorDuringOperation("saveRecordsTargettedByAuthorization", e);
		}
	}

	private void addAuthorizationToPrincipals(List<Record> principals, String authId) {
		for (Record principal : principals) {
			addAuthorizationToRecord(authId, principal);
		}
	}

	private void addAuthorizationToRecords(List<Record> records, String authId) {
		for (Record record : records) {
			validateCanAssignAuthorization(record);
			addAuthorizationToRecord(authId, record);
		}
	}

	void refreshActivationForAllAuths(List<String> collections) {
		for (String collection : collections) {
			Map<String, AuthorizationDetails> authDetails = manager.getAuthorizationsDetails(collection);
			for (AuthorizationDetails authDetail : authDetails.values()) {
				refreshAuthorizationBasedOnDates(authDetail);
			}
		}
	}

	void refreshAuthorizationBasedOnDates(AuthorizationDetails authDetail) {
		LocalDate now = TimeProvider.getLocalDate();
		String authId = authDetail.getId();

		if (authId.startsWith("-")
				&& authDetail.getStartDate() != null
				&& !now.isBefore(authDetail.getStartDate())
				&& (authDetail.getEndDate() == null || !now.isAfter(authDetail.getEndDate()))) {
			String newCode = authId.subSequence(1, authId.length()).toString();
			changeAuthorizationCode(authDetail, newCode);
		}

		if (authDetail.getEndDate() != null && now.isAfter(authDetail.getEndDate())) {
			delete(authorization(authDetail));
		}

	}

	void changeAuthorizationCode(AuthorizationDetails authorization, String newCode) {
		String oldAuthCode = authorization.getId();
		List<Record> recordsWithAuth = getRecordsWithAuth(authorization.getCollection(), oldAuthCode);
		for (Record record : recordsWithAuth) {
			removeAuthorizationOnRecord(oldAuthCode, record, false);
			addAuthorizationToRecord(newCode, record);
		}
		manager.remove(authorization);

		AuthorizationDetails newDetails = new AuthorizationDetails(authorization.getCollection(), newCode,
				authorization.getRoles(), authorization.getStartDate(), authorization.getEndDate(), false);
		manager.add(newDetails);
		try {
			recordServices.execute(new Transaction(recordsWithAuth));
		} catch (RecordServicesException e) {
			throw new RecordServicesErrorDuringOperation("changeAuthorizationCode", e);
		}
	}

	List<Record> getRecordsWithAuth(String collection, String oldAuthCode) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition = fromAllSchemasIn(collection).where(AUTHORIZATIONS).isContaining(
				asList(oldAuthCode));
		query.setCondition(condition);
		return searchServices.search(query);
	}

	Map<String, String> setupAuthorizationsForDetachedRecord(Record record) {
		Map<String, String> originalToCopyMap = new HashMap<>();
		List<String> inheritedAuthorizations = new ArrayList<>(record.<String>getList(Schemas.INHERITED_AUTHORIZATIONS));
		List<String> removedAuthorizations = record.getList(REMOVED_AUTHORIZATIONS);
		inheritedAuthorizations.removeAll(removedAuthorizations);

		List<String> auths = new ArrayList<>(record.<String>getList(AUTHORIZATIONS));
		for (String id : inheritedAuthorizations) {
			String copyId = inheritedToSpecific(record.getCollection(), id);
			if (copyId != null) {
				auths.add(copyId);
				originalToCopyMap.put(id, copyId);
			}
		}
		auths.removeAll(removedAuthorizations);

		record.set(AUTHORIZATIONS, auths);
		record.set(REMOVED_AUTHORIZATIONS, new ArrayList<>());
		record.set(IS_DETACHED_AUTHORIZATIONS, true);
		return originalToCopyMap;
	}

	String inheritedToSpecific(String collection, String id) {
		String newId = uniqueIdGenerator.next();
		AuthorizationDetails inherited = manager.get(collection, id);
		AuthorizationDetails detail = AuthorizationDetails.create(
				newId, inherited.getRoles(), inherited.getStartDate(), inherited.getEndDate(), collection);
		manager.add(detail);
		List<Record> principals = findAllPrincipalsWithAuthorization(inherited);
		if (principals.isEmpty()) {
			return null;
		} else {
			addAuthorizationToPrincipals(principals, detail.getId());
			saveRecordsTargettedByAuthorization(new ArrayList<Record>(), principals);
			return detail.getId();
		}
	}

	void addAuthorizationToRecord(String authorizationId, Record record) {
		List<Object> recordAuths = new ArrayList<>();
		recordAuths.addAll(record.getList(AUTHORIZATIONS));
		recordAuths.add(authorizationId);
		record.set(AUTHORIZATIONS, recordAuths);
	}

	void removeAuthorizationOnRecord(String authorizationId, Record record, boolean reattachIfLastAuth) {
		List<Object> recordAuths = new ArrayList<>();
		recordAuths.addAll(record.getList(AUTHORIZATIONS));
		recordAuths.remove(authorizationId);
		record.set(AUTHORIZATIONS, recordAuths);
		if (reattachIfLastAuth && recordAuths.isEmpty() && Boolean.TRUE.equals(record.get(IS_DETACHED_AUTHORIZATIONS))) {
			record.set(IS_DETACHED_AUTHORIZATIONS, false);
		}
	}

	void removeRemovedAuthorizationOnRecord(String authorizationId, Record record) {
		List<Object> recordAuths = new ArrayList<>();
		recordAuths.addAll(record.getList(REMOVED_AUTHORIZATIONS));
		recordAuths.remove(authorizationId);
		record.set(REMOVED_AUTHORIZATIONS, recordAuths);
	}

	void removeInheritedAuthorizationOnRecord(String authorizationId, Record record) {
		List<Object> removedAuths = new ArrayList<>();
		removedAuths.addAll(record.getList(REMOVED_AUTHORIZATIONS));
		removedAuths.add(authorizationId);
		record.set(REMOVED_AUTHORIZATIONS, removedAuths);
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}

}
