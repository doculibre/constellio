package com.constellio.model.services.security;

import static com.constellio.data.utils.LangUtils.withoutDuplicatesAndNulls;
import static com.constellio.model.entities.schemas.Schemas.ALL_REMOVED_AUTHS;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_ANCESTORS;
import static com.constellio.model.entities.schemas.Schemas.AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.Schemas.IDENTIFIER;
import static com.constellio.model.entities.schemas.Schemas.IS_DETACHED_AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.Schemas.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasExcept;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy;
import static com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords;
import static com.constellio.model.services.security.AuthorizationsServicesRuntimeException.RecordServicesErrorDuringOperation;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationDetails;
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
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordId;
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
	//AuthorizationDetailsManager manager;
	RolesManager rolesManager;
	TaxonomiesManager taxonomiesManager;
	RecordServices recordServices;
	SearchServices searchServices;
	UniqueIdGenerator uniqueIdGenerator;

	public AuthorizationsServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		//this.manager = modelLayerFactory.getAuthorizationDetailsManager();
		this.rolesManager = modelLayerFactory.getRolesManager();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.userServices = modelLayerFactory.newUserServices();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.loggingServices = modelLayerFactory.newLoggingServices();
		this.uniqueIdGenerator = modelLayerFactory.getDataLayerFactory().getUniqueIdGenerator();

	}

	SchemasRecordsServices schemas(String collection) {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	@Deprecated
	//TODO Remove this method with new auth system
	public String getAuthorizationIdByIdWithoutPrefix(String collection, String idWithoutPrefix) {

		SchemasRecordsServices schemas = schemas(collection);
		List<SolrAuthorizationDetails> authorizationDetails = schemas.searchSolrAuthorizationDetailss(
				where(IDENTIFIER).isEndingWithText(idWithoutPrefix));

		if (authorizationDetails.isEmpty()) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId(idWithoutPrefix);
		}
		return authorizationDetails.get(0).getId();
	}

	public Authorization getAuthorization(String collection, String id) {
		if (collection == null) {
			throw new IllegalArgumentException("Collection is null");
		}
		if (id == null) {
			throw new IllegalArgumentException("id is null");
		}
		AuthorizationDetails authDetails = getDetails(collection, id);
		if (authDetails == null) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId(id);
		}
		List<String> grantedToPrincipals = findAllPrincipalIdsWithAuthorization(authDetails);
		return new Authorization(authDetails, grantedToPrincipals);
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
		SchemasRecordsServices schemas = schemas(concept.getCollection());
		Roles roles = rolesManager.getCollectionRoles(concept.getCollection());
		List<Role> rolesGivingPermission = roles.getRolesGivingPermission(permission);
		List<String> rolesCodeGivingPermission = Role.toCodes(rolesGivingPermission);

		//TODO tester avec des end/starts
		List<String> authsGivingRoleOnConcept = new ArrayList<>();
		authsGivingRoleOnConcept.addAll(searchServices.searchRecordIds(from(schemas.authorizationDetails.schemaType())
				.where(schemas.authorizationDetails.target()).isIn(concept.getList(ATTACHED_ANCESTORS))
				.andWhere(schemas.authorizationDetails.roles()).isIn(rolesCodeGivingPermission)));

		//TODO tester avec des remove
		//authsGivingRoleOnConcept.removeAll(concept.<String>getList(ALL_REMOVED_AUTHS));

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(concept.getCollection());

		List<Record> foundRecords = searchServices.search(new LogicalSearchQuery().setCondition(from(schemas.user.schemaType())
				.where(schemas.user.alluserauthorizations()).isIn(authsGivingRoleOnConcept)
				.orWhere(schemas.user.allroles()).isIn(toRolesCodes(rolesGivingPermission))));

		List<User> users = new ArrayList<>();
		for (Record record : foundRecords) {
			users.add(new User(record, types, roles));
		}
		return users;
	}

	public List<User> getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(String permission, Record concept) {

		SchemasRecordsServices schemas = schemas(concept.getCollection());
		Roles roles = rolesManager.getCollectionRoles(concept.getCollection());
		List<Role> rolesGivingPermission = roles.getRolesGivingPermission(permission);
		List<String> rolesCodeGivingPermission = Role.toCodes(rolesGivingPermission);

		//TODO tester avec des end/starts
		List<String> authsGivingRoleOnConcept = new ArrayList<>();
		authsGivingRoleOnConcept.addAll(searchServices.searchRecordIds(from(schemas.authorizationDetails.schemaType())
				.where(schemas.authorizationDetails.target()).isEqualTo(concept.getId())
				.andWhere(schemas.authorizationDetails.roles()).isIn(rolesCodeGivingPermission)));

		//TODO tester avec des remove
		//authsGivingRoleOnConcept.removeAll(concept.<String>getList(ALL_REMOVED_AUTHS));

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(concept.getCollection());

		List<Record> foundRecords = searchServices.search(new LogicalSearchQuery().setCondition(from(schemas.user.schemaType())
				.where(schemas.user.alluserauthorizations()).isIn(authsGivingRoleOnConcept)));

		List<User> users = new ArrayList<>();
		for (Record record : foundRecords) {
			users.add(new User(record, types, roles));
		}
		return users;

	}

	public List<String> getConceptsForWhichUserHasPermission(String permission, User user) {

		if (permission == null) {
			throw new IllegalArgumentException("Permission must not be null");
		}

		if (user == null) {
			throw new IllegalArgumentException("User must not be null");
		}

		SchemasRecordsServices schemas = schemas(user.getCollection());
		MetadataSchemaTypes types = schemas.getTypes();
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(user.getCollection());

		if (principalTaxonomy == null) {
			return new ArrayList<>();
		} else {
			List<MetadataSchemaType> schemaTypes = types.getSchemaTypesWithCode(principalTaxonomy.getSchemaTypes());

			LogicalSearchQuery query = new LogicalSearchQuery();
			query.filteredWithUser(user, permission);
			query.setCondition(from(schemaTypes).returnAll());

			return searchServices.searchRecordIds(query);
		}

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
	public String add(AuthorizationAddRequest request) {

		if (request.getTarget() == null) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}

		try {
			Record record = recordServices.getDocumentById(request.getTarget());
			validateCanAssignAuthorization(record);
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			throw new InvalidTargetRecordId(request.getTarget());
		}

		SolrAuthorizationDetails details = newAuthorizationDetails(request.getCollection(), request.getId(), request.getRoles(),
				request.getStart(), request.getEnd()).setTarget(request.getTarget());
		return add(new Authorization(details, request.getPrincipals()), request.getExecutedBy());
	}

	public String add(AuthorizationAddRequest request, User userAddingTheAuth) {
		return add(request.setExecutedBy(userAddingTheAuth));
	}

	/**
	 * Add an authorization on a record. The authorization will be logged as created by the given user
	 * @param authorization Authorization to add
	 * @param userAddingTheAuth
	 * @return The new authorization's id
	 */
	private String add(Authorization authorization, User userAddingTheAuth) {
		List<Record> principals = getAuthorizationGrantedToPrincipals(authorization);

		SolrAuthorizationDetails authorizationDetail = (SolrAuthorizationDetails) authorization.getDetail();
		authorizationDetail.setTarget(authorization.getGrantedOnRecord());
		String authId = authorizationDetail.getId();

		validateDates(authorizationDetail.getStartDate(), authorizationDetail.getEndDate());
		try {
			recordServices.add(authorizationDetail);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		addAuthorizationToPrincipals(principals, authId);
		saveRecordsTargettedByAuthorization(principals);

		if (userAddingTheAuth != null) {
			loggingServices.grantPermission(authorization, userAddingTheAuth);
		}

		return authId;
	}

	public void execute(AuthorizationDeleteRequest request) {

		List<String> authId = asList(request.getAuthId());
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(request.getCollection())
				.where(Schemas.AUTHORIZATIONS).isContaining(authId)
				.orWhere(REMOVED_AUTHORIZATIONS).isContaining(authId));
		List<Record> recordsWithRemovedAuth = searchServices.search(query);

		if (request.getExecutedBy() != null) {
			try {
				Authorization auth = getAuthorization(request.getCollection(), request.getAuthId());
				loggingServices.deletePermission(auth, request.getExecutedBy());
			} catch (NoSuchAuthorizationWithId e) {
				//No problemo
			}
		}

		for (Record record : recordsWithRemovedAuth) {
			if (record.getTypeCode().equals(User.SCHEMA_TYPE) || record.getTypeCode().equals(Group.SCHEMA_TYPE)) {
				removeAuthorizationToPrincipal(request.getAuthId(), record);
			} else {
				removeRemovedAuthorizationOnRecord(request.getAuthId(), record);
			}
		}
		try {
			recordServices.executeHandlingImpactsAsync(new Transaction(recordsWithRemovedAuth));
		} catch (RecordServicesException e) {
			throw new RecordServicesErrorDuringOperation("delete", e);
		}

		try {
			AuthorizationDetails details = getDetails(request.getCollection(), request.getAuthId());
			if (details != null) {
				remove(details);
			}

			Record target = recordServices.getDocumentById(details.getTarget());
			if (request.isReattachIfLastAuthDeleted() && getRecordAuthorizations(target).isEmpty()
					&& Boolean.TRUE == target.get(Schemas.IS_DETACHED_AUTHORIZATIONS)) {
				reset(target);
			}
		} catch (NoSuchAuthorizationWithId e) {
			//No problemo
		}

	}

	private AuthorizationDetails getDetails(String collection, String id) {
		try {
			return schemas(collection).getSolrAuthorizationDetails(id);
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			throw new NoSuchAuthorizationWithId(id);
		}
	}

	private void remove(AuthorizationDetails details) {
		Record record = ((SolrAuthorizationDetails) details).getWrappedRecord();
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD);
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

	private List<AuthorizationDetails> getInheritedAuths(Record record) {
		SchemasRecordsServices schemas = schemas(record.getCollection());
		return (List) schemas.searchSolrAuthorizationDetailss(
				where(schemas.authorizationDetails.target()).isNotEqual(record.getId())
						.andWhere(schemas.authorizationDetails.target()).isIn(record.getList(ATTACHED_ANCESTORS)));
	}

	private List<String> toIds(List<AuthorizationDetails> authorizationDetailses) {
		List<String> ids = new ArrayList<>();

		for (AuthorizationDetails authorizationDetails : authorizationDetailses) {
			ids.add(authorizationDetails.getId());
		}

		return ids;
	}

	private AuthorizationModificationResponse executeWithoutLogging(AuthorizationModificationRequest request,
			Authorization authorization, Record record) {

		String authTarget = authorization.getDetail().getTarget();
		String authId = authorization.getDetail().getId();
		boolean directlyTargetted = authTarget.equals(record.getId());
		boolean inherited = !directlyTargetted && record.getList(ATTACHED_ANCESTORS).contains(authTarget);

		if (!directlyTargetted && !inherited) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord(authId, record);
		}

		if (request.isRemovedOnRecord()) {
			Transaction transaction = new Transaction(record);

			if (directlyTargetted) {
				execute(authorizationDeleteRequest(authorization.getDetail()).setExecutedBy(request.getExecutedBy()));

			} else {
				removeInheritedAuthorizationOnRecord(authId, record);
			}

			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new RecordServicesErrorDuringOperation("removeAuthorizationOnRecord", e);
			}
			return new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

		} else {

			if (directlyTargetted) {
				executeOnAuthorization(request, authorization, record);
				return new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

			} else {
				String copyId = inheritedToSpecific(record.getId(), record.getCollection(), authorization.getDetail().getId());
				record.addValueToList(REMOVED_AUTHORIZATIONS, authorization.getDetail().getId());

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
			saveRecordsTargettedByAuthorization(asList(record));
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

		} else if (Group.DEFAULT_SCHEMA.equals(record.getSchemaCode())) {
			authIds = record.getList(Schemas.ALL_AUTHORIZATIONS);

		} else {
			SchemasRecordsServices schemas = schemas(record.getCollection());
			authIds = searchServices.searchRecordIds(from(schemas.authorizationDetails.schemaType())
					.where(schemas.authorizationDetails.target()).isIn(record.<String>getList(ATTACHED_ANCESTORS))
					.andWhere(Schemas.IDENTIFIER).isNotIn(record.getList(ALL_REMOVED_AUTHS)));

		}

		List<Authorization> authorizations = new ArrayList<>();
		for (String authId : authIds) {
			AuthorizationDetails authDetails = getDetails(record.getCollection(), authId);
			if (authDetails != null) {
				List<String> grantedToPrincipals = findAllPrincipalIdsWithAuthorization(authDetails);
				authorizations.add(new Authorization(authDetails, grantedToPrincipals));
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
		SchemasRecordsServices schemas = schemas(record.getCollection());
		List<AuthorizationDetails> authorizationDetailses = new ArrayList<>();
		record.set(REMOVED_AUTHORIZATIONS, null);
		record.set(IS_DETACHED_AUTHORIZATIONS, false);

		try {
			recordServices.update(record);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}

		for (AuthorizationDetails authorizationDetails : schemas.searchSolrAuthorizationDetailss(
				where(schemas.authorizationDetails.target()).isEqualTo(record.getId()))) {
			execute(authorizationDeleteRequest(authorizationDetails));
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

	/**
	 *Use user.hasReadAccess().on(record) instead
	 */
	@Deprecated
	public boolean canRead(User user, Record record) {
		return user.hasReadAccess().on(record);
	}

	/**
	 *Use user.hasWriteAccess().on(record) instead
	 */
	@Deprecated
	public boolean canWrite(User user, Record record) {
		return user.hasWriteAccess().on(record);
	}

	/**
	 *Use user.hasDeleteAccess().on(record) instead
	 */
	@Deprecated
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
		SchemasRecordsServices schemas = schemas(detail.getCollection());
		return searchServices.search(new LogicalSearchQuery(from(asList(schemas.user.schemaType(), schemas.group.schemaType()))
				.where(AUTHORIZATIONS).isEqualTo(detail.getId())));
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
			List<String> rolesId = getDetails(user.getCollection(), id).getRoles();
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

		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(groupSchema).where(IDENTIFIER).is(groupId);

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
		if (authorization.getGrantedToPrincipals() == null) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}

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

	private List<String> toRolesCodes(List<Role> rolesGivingPermission) {
		List<String> roleCodes = new ArrayList<>();

		for (Role role : rolesGivingPermission) {
			roleCodes.add(role.getCode());
		}

		return roleCodes;
	}

	private void saveRecordsTargettedByAuthorization(List<Record> principals) {
		Transaction transaction = new Transaction();
		transaction.addUpdate(principals);
		try {
			recordServices.executeHandlingImpactsAsync(transaction);
		} catch (RecordServicesException e) {
			throw new RecordServicesErrorDuringOperation("saveRecordsTargettedByAuthorization", e);
		}
	}

	private void addAuthorizationToPrincipals(List<Record> principals, String authId) {
		for (Record principal : principals) {
			addAuthorizationToPrincipal(authId, principal);
		}
	}

	void refreshActivationForAllAuths(List<String> collections) {
		for (String collection : collections) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);

			for (AuthorizationDetails authToDelete : schemas.searchSolrAuthorizationDetailss(
					where(schemas.authorizationDetails.endDate()).isLessThan(TimeProvider.getLocalDate()))) {

				execute(authorizationDeleteRequest(authToDelete));
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
			execute(authorizationDeleteRequest(authDetail));
		}

	}

	void changeAuthorizationCode(AuthorizationDetails authorization, String newCode) {
		//		String oldAuthCode = authorization.getId();
		//		List<Record> recordsWithAuth = getRecordsWithAuth(authorization.getCollection(), oldAuthCode);
		//		for (Record record : recordsWithAuth) {
		//			removeAuthorizationOnRecord(oldAuthCode, record, false);
		//			addAuthorizationToRecord(newCode, record);
		//		}
		//		remove(authorization);
		//
		//		SolrAuthorizationDetails newDetails = schemas(authorization.getCollection()).newSolrAuthorizationDetails()
		//		new SolrAuthorizationDetails(authorization.getCollection(), newCode,
		//				authorization.getRoles(), authorization.getStartDate(), authorization.getEndDate(), false);
		//		newDetails;
		//		try {
		//			recordServices.execute(new Transaction(recordsWithAuth));
		//		} catch (RecordServicesException e) {
		//			throw new RecordServicesErrorDuringOperation("changeAuthorizationCode", e);
		//		}
	}

	Record getRecordWithAuth(String collection, String oldAuthCode) {
		return recordServices.getDocumentById(schemas(collection).getSolrAuthorizationDetails(oldAuthCode).getTarget());
	}

	Map<String, String> setupAuthorizationsForDetachedRecord(Record record) {
		Map<String, String> originalToCopyMap = new HashMap<>();
		List<AuthorizationDetails> inheritedAuthorizations = getInheritedAuths(record);
		List<String> removedAuthorizations = record.getList(REMOVED_AUTHORIZATIONS);

		for (AuthorizationDetails inheritedAuthorization : inheritedAuthorizations) {
			if (!removedAuthorizations.contains(inheritedAuthorization.getId())) {
				String copyId = inheritedToSpecific(record.getId(), record.getCollection(), inheritedAuthorization.getId());
				if (copyId != null) {
					originalToCopyMap.put(inheritedAuthorization.getId(), copyId);
				}
			}
		}

		record.set(REMOVED_AUTHORIZATIONS, new ArrayList<>());
		record.set(IS_DETACHED_AUTHORIZATIONS, true);
		return originalToCopyMap;
	}

	String inheritedToSpecific(String recordId, String collection, String id) {
		AuthorizationDetails inherited = getDetails(collection, id);
		SolrAuthorizationDetails detail = newAuthorizationDetails(collection, null, inherited.getRoles(),
				inherited.getStartDate(), inherited.getEndDate());
		detail.setTarget(recordId);
		try {
			recordServices.add(detail);
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
		List<Record> principals = findAllPrincipalsWithAuthorization(inherited);
		if (principals.isEmpty()) {
			return null;
		} else {
			addAuthorizationToPrincipals(principals, detail.getId());
			saveRecordsTargettedByAuthorization(principals);
			return detail.getId();
		}
	}

	void addAuthorizationToPrincipal(String authorizationId, Record record) {
		List<Object> recordAuths = new ArrayList<>();
		recordAuths.addAll(record.getList(AUTHORIZATIONS));
		recordAuths.add(authorizationId);
		record.set(AUTHORIZATIONS, recordAuths);
	}

	void removeAuthorizationToPrincipal(String authorizationId, Record record) {
		List<Object> recordAuths = new ArrayList<>();
		recordAuths.addAll(record.getList(AUTHORIZATIONS));
		recordAuths.remove(authorizationId);
		record.set(AUTHORIZATIONS, recordAuths);
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

	void validateDates(LocalDate startDate, LocalDate endDate) {
		LocalDate now = TimeProvider.getLocalDate();
		if ((startDate != null && endDate != null) && startDate.isAfter(endDate)) {
			throw new AuthorizationDetailsManagerRuntimeException.StartDateGreaterThanEndDate(startDate, endDate);
		}
		if (endDate != null && endDate.isBefore(now)) {
			throw new AuthorizationDetailsManagerRuntimeException.EndDateLessThanCurrentDate(endDate.toString());
		}
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}

	private SolrAuthorizationDetails newAuthorizationDetails(String collection, String id, List<String> roles,
			LocalDate startDate, LocalDate endDate) {
		SolrAuthorizationDetails details;

		if (id == null) {
			id = UUIDV1Generator.newRandomId();
		}

		boolean read = false;
		boolean write = false;
		boolean delete = false;
		List<String> rolesCodes = new ArrayList<>();
		List<String> operationRolesCodes = new ArrayList<>();
		for (String role : roles) {
			rolesCodes.add(role);

			boolean readAccess = role.equals(Role.READ);
			boolean writeAccess = role.equals(Role.WRITE) || role.equals(Role.DELETE);
			boolean deleteAccess = role.equals(Role.DELETE);

			read |= readAccess;
			write |= writeAccess;
			delete |= deleteAccess;
			if (!readAccess && !writeAccess && !deleteAccess) {
				operationRolesCodes.add(role);
			}
		}

		StringBuilder idBuilder = new StringBuilder();
		if (read || write || delete) {
			idBuilder.append("r");
		}
		if (write) {
			idBuilder.append("w");
		}
		if (delete) {
			idBuilder.append("d");
		}
		idBuilder.append("_");
		idBuilder.append(StringUtils.join(operationRolesCodes, ","));
		idBuilder.append("_");
		idBuilder.append(id);

		details = schemas(collection).newSolrAuthorizationDetailsWithId(idBuilder.toString());

		//		if (request.getId() == null) {
		//			details = schemas(request.getCollection()).newSolrAuthorizationDetails();
		//		} else {
		//			details = schemas(request.getCollection()).newSolrAuthorizationDetailsWithId(request.getId());
		//		}

		details.setRoles(roles).setStartDate(startDate).setEndDate(endDate);
		return details;
	}

}
