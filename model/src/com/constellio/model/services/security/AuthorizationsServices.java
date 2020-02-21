package com.constellio.model.services.security;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.EmailToSend;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.entities.security.global.AuthorizationDeleteRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationRequest;
import com.constellio.model.entities.security.global.AuthorizationModificationResponse;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.structures.EmailAddress;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.AuthServices_RecordServicesException;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotAddAuhtorizationInNonPrincipalTaxonomy;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.CannotDetachConcept;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidPrincipalsIds;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.InvalidTargetRecordId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId;
import com.constellio.model.services.security.AuthorizationsServicesRuntimeException.NoSuchPrincipalWithUsername;
import com.constellio.model.services.security.roles.Roles;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.data.utils.LangUtils.withoutDuplicatesAndNulls;
import static com.constellio.model.entities.records.wrappers.UserAuthorizationsUtils.getAuthsReceivedBy;
import static com.constellio.model.entities.schemas.Schemas.ALL_REMOVED_AUTHS;
import static com.constellio.model.entities.schemas.Schemas.ATTACHED_ANCESTORS;
import static com.constellio.model.entities.schemas.Schemas.IS_DETACHED_AUTHORIZATIONS;
import static com.constellio.model.entities.schemas.Schemas.REMOVED_AUTHORIZATIONS;
import static com.constellio.model.entities.security.global.AuthorizationDeleteRequest.authorizationDeleteRequest;
import static com.constellio.model.services.records.RecordUtils.unwrap;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public class AuthorizationsServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationsServices.class);

	ModelLayerFactory modelLayerFactory;
	MetadataSchemasManager schemasManager;
	private LoggingServices loggingServices;
	RolesManager rolesManager;
	TaxonomiesManager taxonomiesManager;
	RecordServices recordServices;
	SearchServices searchServices;

	public AuthorizationsServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.rolesManager = modelLayerFactory.getRolesManager();
		this.taxonomiesManager = modelLayerFactory.getTaxonomiesManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.loggingServices = modelLayerFactory.newLoggingServices();

	}

	SchemasRecordsServices schemas(String collection) {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	public Authorization getAuthorization(String collection, String id) {
		if (collection == null) {
			throw new IllegalArgumentException("Collection is null");
		}
		if (id == null) {
			throw new IllegalArgumentException("id is null");
		}
		Authorization authDetails = getDetails(collection, id);
		if (authDetails == null) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId(id);
		}
		return authDetails;
	}

	public Authorization getAuthorization(User user, String recordId) {
		if (user == null) {
			throw new IllegalArgumentException("user is null");
		}
		if (recordId == null) {
			throw new IllegalArgumentException("record id is null");
		}
		Authorization authDetails = getDetails(user, recordId);

		return authDetails;
	}

	public List<User> getUsersWithGlobalPermissionInCollection(String permission, String collection) {
		return getUsersWithGlobalPermissionInCollectionExcludingRoles(permission, collection, new ArrayList<String>());
	}

	public List<User> getUsersWithGlobalAccessInCollection(String collection) {
		List<User> allUsersInCollection = modelLayerFactory.newUserServices().getAllUsersInCollection(collection);
		List<User> returnedUsers = new ArrayList<>();

		for (User currentUser : allUsersInCollection) {
			if (StringUtils.isNotBlank(currentUser.getEmail()) && currentUser.getStatus() == UserCredentialStatus.ACTIVE &&
				(currentUser.hasCollectionReadAccess() || currentUser.hasCollectionWriteAccess() || currentUser.hasCollectionDeleteAccess())) {
				returnedUsers.add(currentUser);
			}
		}
		return returnedUsers;
	}

	public List<String> getUsersIdsWithGlobalReadRightInCollection(String collection) {
		List<User> allUsersInCollection = modelLayerFactory.newUserServices().getAllUsersInCollection(collection);
		List<String> returnedUsers = new ArrayList<>();

		for (User currentUser : allUsersInCollection) {
			if (StringUtils.isNotBlank(currentUser.getEmail()) && currentUser.getStatus() == UserCredentialStatus.ACTIVE && currentUser.hasCollectionReadAccess()) {
				returnedUsers.add(currentUser.getId());
			}
		}
		return returnedUsers;
	}

	public List<User> getUsersWithGlobalPermissionInCollectionExcludingRoles(String permission, String collection,
																			 List<String> excludingRoles) {

		Roles roles = rolesManager.getCollectionRoles(collection, modelLayerFactory);
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

		List<User> returnedUsers = new ArrayList<>();

		for (User user : schemas(concept.getCollection()).getAllUsersInUnmodifiableState()) {
			if (user.has(permission).on(concept)) {
				returnedUsers.add(user.getCopyOfOriginalRecord());
			}
		}

		return returnedUsers;
	}

	public List<String> getUserIdsWithPermissionOnRecord(String permission, Record concept) {

		List<String> returnedUsers = new ArrayList<>();

		for (User user : schemas(concept.getCollection()).getAllUsers()) {
			if (user.has(permission).on(concept) && !user.isLogicallyDeletedStatus()) {
				returnedUsers.add(user.getId());
			}
		}
		return returnedUsers;

	}

	public List<User> getUsersWithPermissionOnRecordExcludingRecordInheritedAuthorizations(String permission,
																						   Record concept) {

		List<User> returnedUsers = new ArrayList<>();

		for (User user : schemas(concept.getCollection()).getAllUsersInUnmodifiableState()) {
			if (user.has(permission).specificallyOn(concept)) {
				returnedUsers.add(user.getCopyOfOriginalRecord());
			}
		}

		return returnedUsers;

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
			List<String> returnedIds = new ArrayList<>();
			for (MetadataSchemaType type : types.getSchemaTypesWithCode(principalTaxonomy.getSchemaTypes())) {
				for (Record record : searchServices.getAllRecordsInUnmodifiableState(type)) {
					if (user.has(permission).on(record)) {
						returnedIds.add(record.getId());
					}
				}

			}
			return returnedIds;
		}

	}


	public List<User> getUsersWithRoleForRecord(String role, Record record) {
		List<User> users = new ArrayList<>();
		List<Authorization> recordAuths = getRecordAuthorizations(record);
		for (Authorization auth : recordAuths) {
			if (auth.getRoles().contains(role)) {
				List<String> principals = auth.getPrincipals();
				List<Record> principalRecords = recordServices.getRecordsById(auth.getCollection(), principals);
				MetadataSchemaTypes types = schemasManager.getSchemaTypes(record.getCollection());
				Roles roles = rolesManager.getCollectionRoles(record.getCollection(), modelLayerFactory);
				for (Record principalRecord : principalRecords) {
					if (principalRecord.getSchemaCode().equals(Group.SCHEMA_TYPE + "_default")) {
						List<Record> usersInGroupRecord = getUserRecordsInGroup(principalRecord);
						for (Record userRecord : usersInGroupRecord) {
							users.add(new User(userRecord, types, roles));
						}
					} else if (principalRecord.getSchemaCode().equals(User.SCHEMA_TYPE + "_default")) {
						User user = new User(principalRecord, types, roles);
						UserCredential userCredential = modelLayerFactory.newUserServices().getUserCredential(user.getUsername());

						if (userCredential.getStatus() == UserCredentialStatus.ACTIVE) {
							users.add(user);
						}
					}
				}
			}
		}
		return users;
	}

	public List<String> getUsersIdsWithRoleForRecord(String role, Record record) {
		List<User> usersWithRoleForRecord = getUsersWithRoleForRecord(role, record);
		ArrayList<String> usersIds = new ArrayList<>();

		for (User user : usersWithRoleForRecord) {
			usersIds.add(user.getId());
		}
		return usersIds;
	}

	public List<Record> getUserRecordsInGroup(Record groupRecord) {

		SchemasRecordsServices schemas = new SchemasRecordsServices(groupRecord.getCollection(), modelLayerFactory);
		Group group = schemas.wrapGroup(groupRecord);
		return unwrap(schemas.getAllUsersInGroup(group, true, true));
	}

	/**
	 * Add an authorization on a record
	 *
	 * @param request
	 * @return
	 */
	public String add(AuthorizationAddRequest request) {

		if (request.getTarget() == null) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}

		Record record;
		try {
			record = recordServices.getDocumentById(request.getTarget());
			validateCanAssignAuthorization(record);
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			throw new InvalidTargetRecordId(request.getTarget());
		}

		Authorization details = newAuthorizationDetails(request.getCollection(), request.getId(), request.getRoles(),
				request.getStart(), request.getEnd(), request.isOverridingInheritedAuths(), request.isNegative())
				.setTarget(request.getTarget()).setTargetSchemaType(record.getTypeCode());
		details.setPrincipals(principalToRecordIds(schemas(record.getCollection()), request.getPrincipals()));
		details.setSharedBy(request.getSharedBy());
		return add(details, request.getCollection(), request.getExecutedBy());
	}

	public String add(AuthorizationAddRequest request, User userAddingTheAuth) {
		return add(request.setExecutedBy(userAddingTheAuth));
	}

	/**
	 * Add an authorization on a record. The authorization will be logged as created by the given user
	 *
	 * @param authorization     Authorization to add
	 * @param userAddingTheAuth
	 * @return The new authorization's id
	 */
	private String add(Authorization authorization, String collection, User userAddingTheAuth) {

		AuthTransaction transaction = new AuthTransaction();

		Authorization authorizationDetail = (Authorization) authorization;
		authorizationDetail.setTarget(authorization.getTarget());
		Record record = recordServices.getDocumentById(authorization.getTarget());
		authorizationDetail.setTargetSchemaType(record.getTypeCode());
		authorizationDetail.setPrincipals(authorization.getPrincipals());
		transaction.add(authorizationDetail);
		String authId = authorizationDetail.getId();

		validateDates(authorizationDetail.getStartDate(), authorizationDetail.getEndDate());
		authorizationDetail.setPrincipals(authorization.getPrincipals());

		if (!transaction.getRecordIds().contains(authorization.getTarget())) {
			transaction.add(record);
		}
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());

		executeTransaction(transaction);

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();

		if (userAddingTheAuth != null) {
			loggingServices.grantPermission(authorization, userAddingTheAuth, authorization.getSharedBy() != null);
		}

		if (authorization.getSharedBy() != null && authorization.getSharedBy() != "") {
			alertUsers(collection, authorizationDetail.getTargetSchemaType(), record, authorization.getStartDate(),
					authorization.getEndDate(), authorization.getSharedBy(), authorization.getPrincipals());
		}
		return authId;
	}

	public void execute(AuthorizationDeleteRequest request) {
		AuthTransaction transaction = new AuthTransaction();
		Authorization removedAuthorization = execute(request, transaction);
		String grantedOnRecord = removedAuthorization.getTarget();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		if (!transaction.getRecordIds().contains(grantedOnRecord)) {
			try {
				transaction.add(recordServices.getDocumentById(grantedOnRecord));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.info("Failed to removeFromAllCaches hasChildrenCache after deletion of authorization", e);
			}
		}

		executeTransaction(transaction);
		if (grantedOnRecord != null) {
			//			try {
			////				refreshCaches(recordServices.getDocumentById(grantedOnRecord),
			////						new ArrayList<String>(), request.get);
			//			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//				LOGGER.info("Failed to removeFromAllCaches hasChildrenCache after deletion of authorization", e);
			//			}

		}

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();
	}

	private static class AuthTransaction extends Transaction {

		Set<String> recordsToResetIfNoAuths = new HashSet<>();

		List<Authorization> authsDetailsToDelete = new ArrayList<>();

		@Override
		public Record add(Record addUpdateRecord) {
			if (getRecordIds().contains(addUpdateRecord.getId())) {
				Record currentTransactionRecord = getRecord(addUpdateRecord.getId());
				if (currentTransactionRecord != addUpdateRecord) {
					throw new RuntimeException("Cannot add the same record twice : " + addUpdateRecord.getId());
				}
				return addUpdateRecord;
			} else {
				return super.add(addUpdateRecord);
			}
		}
	}

	private Authorization execute(AuthorizationDeleteRequest request, AuthTransaction transaction) {

		List<String> authId = asList(request.getAuthId());
		Authorization auth = getAuthorization(request.getCollection(), request.getAuthId());

		LogicalSearchCondition condition = fromAllSchemasIn(request.getCollection())
				.where(REMOVED_AUTHORIZATIONS).isContaining(authId);

		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(request.getCollection());

		if (principalTaxonomy == null || !principalTaxonomy.getSchemaTypes().contains(auth.getTargetSchemaType())) {
			condition = condition.orWhere(Schemas.ATTACHED_ANCESTORS).isEqualTo(auth.getTarget());
		}

		List<Record> recordsWithRemovedAuth = searchServices.search(new LogicalSearchQuery(condition));

		if (request.getExecutedBy() != null) {
			try {
				loggingServices.deletePermission(auth, request.getExecutedBy());
			} catch (NoSuchAuthorizationWithId e) {
				//No problemo
			}
		}

		for (Record record : recordsWithRemovedAuth) {
			boolean newRecordInTransaction = !transaction.getRecordIds().contains(record.getId());
			record = newRecordInTransaction ? record : transaction.getRecord(record.getId());

			removeRemovedAuthorizationOnRecord(request.getAuthId(), record);

			if (newRecordInTransaction) {
				transaction.add(record);
			}

		}

		Authorization removedAuthorization = null;
		try {
			removedAuthorization = getDetails(request.getCollection(), request.getAuthId());
			if (removedAuthorization != null) {
				transaction.authsDetailsToDelete.add((Authorization) removedAuthorization);
				transaction.add(((Authorization) removedAuthorization).getWrappedRecord()
						.set(Schemas.LOGICALLY_DELETED_STATUS, true));

			}

			try {
				Record target = recordServices.getDocumentById(removedAuthorization.getTarget());
				if (request.isReattachIfLastAuthDeleted() && Boolean.TRUE.equals(target.get(Schemas.IS_DETACHED_AUTHORIZATIONS))) {
					transaction.recordsToResetIfNoAuths.add(target.getId());
				}
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				//Record does not exist. So nothing to do
			}
		} catch (NoSuchAuthorizationWithId e) {
			//No problemo
		}

		return removedAuthorization;

	}

	private Authorization getDetails(String collection, String id) {
		try {
			return schemas(collection).getSolrAuthorizationDetails(id);
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			throw new NoSuchAuthorizationWithId(id);
		}
	}

	private Authorization getDetails(User user, String recordId) {
		try {
			String userId = user.getId();

			SchemasRecordsServices schemas = schemas(user.getCollection());

			Metadata sharedByMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.SHARED_BY);
			Metadata principalMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.PRINCIPALS);
			Metadata targetMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.TARGET);

			LogicalSearchCondition condition = from(schemas.authorizationDetails.schemaType())
					.whereAllConditions(anyConditions(where(sharedByMeta).isEqualTo(user.getId()),
							where(principalMeta).isContainingText(user.getId())), where(targetMeta).isEqualTo(recordId));


			List<Record> records = searchServices.search(new LogicalSearchQuery(condition));

			if (records.size() > 0) {
				return schemas(user.getCollection()).wrapSolrAuthorizationDetails(records.get(0));
			} else {
				return null;
			}
		} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			throw new NoSuchAuthorizationWithId(recordId);
		}
	}

	private void remove(Authorization details) {
		Record record = ((Authorization) details).getWrappedRecord();
		recordServices.logicallyDelete(record, User.GOD);
		recordServices.physicallyDelete(record, User.GOD);
	}

	/**
	 * Modify an authorization on a specific record. The request will be handled differently depending
	 * if the record is or not the root target of the authorization. This service will never detach/reattach or reset records.
	 *
	 * @param request The request to execute
	 * @return A response with some informations
	 */
	public AuthorizationModificationResponse execute(AuthorizationModificationRequest request) {

		Authorization authorizationBefore = getAuthorization(request.getCollection(), request.getAuthorizationId());
		Authorization authorization = getAuthorization(request.getCollection(), request.getAuthorizationId());
		Record record = recordServices.getDocumentById(request.getRecordId());

		AuthorizationModificationResponse response = executeWithoutLogging(request, authorization, record);

		if (request.getExecutedBy() != null) {

			Authorization authorizationAfter;
			if (response.getIdOfAuthorizationCopy() != null) {
				authorizationAfter = getAuthorization(request.getCollection(), response.getIdOfAuthorizationCopy());
			} else {
				authorizationAfter = getAuthorization(request.getCollection(), authorization.getId());
			}

			try {
				loggingServices.modifyPermission(authorizationBefore, authorizationAfter, record, request.getExecutedBy(),
						authorization.getSharedBy() != null);
			} catch (NoSuchAuthorizationWithId e) {
				//No problemo
			}
		}

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();

		return response;
	}

	private List<Authorization> getInheritedAuths(Record record) {
		SchemasRecordsServices schemas = schemas(record.getCollection());

		List<Authorization> authorizationDetails = new ArrayList<>();

		Set<String> recordsIdsWithPosibleAuths = new HashSet<>();
		recordsIdsWithPosibleAuths.addAll(record.<String>getList(ATTACHED_ANCESTORS));
		recordsIdsWithPosibleAuths.remove(record.getId());

		for (String ancestorId : record.<String>getList(ATTACHED_ANCESTORS)) {
			if (!ancestorId.equals(record.getId()) && !ancestorId.startsWith("-")) {

				Record ancestor = recordServices.getDocumentById(ancestorId);
				MetadataSchema schema = schemasManager.getSchemaOf(ancestor);
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.isRelationshipProvidingSecurity()) {
						recordsIdsWithPosibleAuths.addAll(ancestor.<String>getValues(metadata));
					}
				}
			}
		}

		for (Authorization authorizationDetail : schemas.getAllAuthorizationsInUnmodifiableState()) {
			if (recordsIdsWithPosibleAuths.contains(authorizationDetail.getTarget())) {
				authorizationDetails.add(authorizationDetail.getCopyOfOriginalRecord());
			}
		}


		return authorizationDetails;
	}

	private AuthorizationModificationResponse executeWithoutLogging(AuthorizationModificationRequest request,
																	Authorization authorization,
																	Record record) {

		AuthTransaction transaction = new AuthTransaction();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		transaction.add(record);
		String authTarget = authorization.getTarget();
		String authId = authorization.getId();
		boolean directlyTargetted = authTarget.equals(record.getId());
		boolean inherited = !directlyTargetted && record.getList(ATTACHED_ANCESTORS).contains(authTarget);
		if (!directlyTargetted && !inherited && Authorization.isSecurableSchemaType(authorization.getTargetSchemaType())) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord(authId, record);
		}

		AuthorizationModificationResponse response;
		if (request.isRemovedOnRecord()) {
			if (directlyTargetted) {
				execute(authorizationDeleteRequest(authorization).setExecutedBy(request.getExecutedBy()),
						transaction);

			} else {
				removeInheritedAuthorizationOnRecord(authId, record);
			}

			response = new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

		} else {

			if (directlyTargetted) {
				transaction.add((Authorization) authorization);
				executeOnAuthorization(transaction, request, authorization, record,
						authorization.getPrincipals());
				response = new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

			} else {
				Authorization copy = inheritedToSpecific(transaction, record, record.getCollection(),
						authorization.getId());
				record.addValueToList(REMOVED_AUTHORIZATIONS, authorization.getId());

				executeOnAuthorization(transaction, request, copy, record, authorization.getPrincipals());

				response = new AuthorizationModificationResponse(false, copy.getId(), singletonMap(authId, copy.getId()));

			}
		}

		executeTransaction(transaction);
		return response;
	}

	/**
	 * Detach a record from its parent, creating specific authorizations that are equal to those before the detach.
	 * - Future modifications on a parent record won't affect the detached record.
	 * - If the detached record is reassigned to a new parent, there will be no effects on authorizations
	 *
	 * @param record A securable record to detach
	 * @return A mapping of previous authorization ids to the new authorizations created by this service
	 */
	public Map<String, String> detach(Record record) {
		recordServices.refresh(record);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(record.getCollection());
		if (principalTaxonomy.getSchemaTypes().contains(record.getTypeCode())) {
			throw new CannotDetachConcept(record.getId());
		}

		if (Boolean.TRUE.equals(record.get(Schemas.IS_DETACHED_AUTHORIZATIONS))) {
			return Collections.emptyMap();

		} else {
			AuthTransaction transaction = new AuthTransaction();
			Map<String, String> originalToCopyMap = setupAuthorizationsForDetachedRecord(transaction, record);
			transaction.add(record);
			executeTransaction(transaction);
			return originalToCopyMap;
		}
	}

	public List<Authorization> getRecordsAuthorizations(List<Record> records) {

		List<Authorization> authorizations = new ArrayList<>();
		for (Record record : records) {
			authorizations.addAll(getRecordAuthorizations(record));
		}
		return authorizations;
	}

	/**
	 * Return all authorizations targetting a given Record, which may be a user or securised Record.
	 * Authorizations may be inherited or assigned directly to the record
	 *
	 * @param recordWrapper User or a securised record
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
		SchemasRecordsServices schemas = schemas(record.getCollection());

		List<Authorization> authorizations = new ArrayList<>();
		if (User.DEFAULT_SCHEMA.equals(record.getSchemaCode())) {
			User user = schemas.wrapUser(record);
			SecurityModel securityModel = user.getRolesDetails().getSecurityModel();

			Set<String> ids = new HashSet<>();

			for (SecurityModelAuthorization authorization : securityModel.getAuthorizationsToPrincipal(user.getId(), true)) {
				if (!ids.contains(authorization.getDetails().getId())) {
					authorizations.add(authorization.getDetails());
					ids.add(authorization.getDetails().getId());
				}
			}

		} else if (Group.DEFAULT_SCHEMA.equals(record.getSchemaCode())) {
			List<String> authIds = new ArrayList<>(getAuthsReceivedBy(schemas.wrapGroup(record), schemas));

			for (String authId : authIds) {
				Authorization authDetails = getDetails(record.getCollection(), authId);
				if (authDetails != null) {
					authorizations.add(authDetails);
				} else {
					LOGGER.error("Missing authorization '" + authId + "'");
				}
			}

		} else {
			List<String> authIds = new ArrayList<>();
			for (Authorization authorizationDetails : schemas.getAllAuthorizationsInUnmodifiableState()) {

				boolean targettingRecordOrAncestor =
						(record.getList(ATTACHED_ANCESTORS).contains(authorizationDetails.getTarget())
						 || record.getId().equals(authorizationDetails.getTarget()))
						&& !record.getList(ALL_REMOVED_AUTHS).contains(authorizationDetails.getId());

				if (targettingRecordOrAncestor) {
					authIds.add(authorizationDetails.getId());
				}
			}

			for (String authId : authIds) {
				Authorization authDetails = getDetails(record.getCollection(), authId);
				if (authDetails != null) {
					authorizations.add(authDetails);
				} else {
					LOGGER.error("Missing authorization '" + authId + "'");
				}
			}

		}


		return authorizations;
	}

	public boolean itemIsSharedByUser(Record record, User user) {
		String userId = user.getId();

		SchemasRecordsServices schemas = schemas(user.getCollection());
		Metadata sharedByMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.SHARED_BY);
		Metadata targetMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.TARGET);
		LogicalSearchCondition condition = from(schemas.authorizationDetails.schemaType())
				.where(sharedByMeta).isEqualTo(userId).andWhere(targetMeta).isEqualTo(record.getId());

		List<Record> recordExist = searchServices.search(new LogicalSearchQuery(condition));

		return recordExist != null && recordExist.size() > 0;
	}

	public Authorization getRecordShareAuthorization(Record record, User user) {
		String userId = user.getId();

		SchemasRecordsServices schemas = schemas(user.getCollection());
		Metadata sharedByMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.SHARED_BY);
		Metadata targetMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.TARGET);
		LogicalSearchCondition condition = from(schemas.authorizationDetails.schemaType())
				.where(sharedByMeta).isEqualTo(userId).andWhere(targetMeta).isEqualTo(record.getId());

		List<Record> records = searchServices.search(new LogicalSearchQuery(condition));

		if (records.size() > 0) {
			Authorization authorization = schemas.wrapSolrAuthorizationDetails(records.get(0));
			return authorization;
		} else {
			return null;
		}
	}

	/**
	 * Return all records a user authorized as shared.
	 * Authorizations may be inherited or assigned directly to the record
	 *
	 * @param user User sharing
	 * @return Records
	 */
	public List<Authorization> getAllAuthorizationUserShared(User user) {
		String userId = user.getId();

		SchemasRecordsServices schemas = schemas(user.getCollection());
		Metadata sharedByMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.SHARED_BY);
		LogicalSearchCondition condition = from(schemas.authorizationDetails.schemaType())
				.where(sharedByMeta).isEqualTo(userId);

		List<Record> recordsSharedByUser = searchServices.search(new LogicalSearchQuery(condition));
		List<Authorization> authorizations = schemas.wrapSolrAuthorizationDetailss(recordsSharedByUser);

		return authorizations;
	}

	/**
	 * Return all records a user authorized was shared to him.
	 * Authorizations may be inherited or assigned directly to the record
	 *
	 * @param user User sharing
	 * @return Records
	 */
	public List<Authorization> getAllUserSharedRecords(User user) {
		String userId = user.getId();

		SchemasRecordsServices schemas = schemas(user.getCollection());

		Metadata principalsMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.PRINCIPALS);
		Metadata sharedByMeta = schemas.authorizationDetails.schema().getMetadata(Authorization.SHARED_BY);
		LogicalSearchCondition condition = from(schemas.authorizationDetails.schemaType())
				.where(principalsMeta).isContainingText(userId).andWhere(sharedByMeta).isNotNull();


		List<Record> recordsSharedToUser = searchServices.search(new LogicalSearchQuery(condition));


		List<Authorization> authorizations = schemas.wrapSolrAuthorizationDetailss(recordsSharedToUser);

		return authorizations;
	}

	/**
	 * Reset a securable record.
	 * The resetted record will be reattached (inheriting all authorizations) and all its specific authorizations will be lost
	 *
	 * @param record The securable record
	 */
	public void reset(Record record) {
		AuthTransaction transaction = new AuthTransaction();
		reset(record, transaction);
		executeTransaction(transaction);

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();
	}

	private void executeTransaction(AuthTransaction transaction) {
		transaction.setOptions(RecordUpdateOptions.validationExceptionSafeOptions()
				.setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL()));
		try {
			recordServices.execute(transaction);
		} catch (Exception e) {
			try {
				recordServices.executeHandlingImpactsAsync(transaction);
			} catch (com.constellio.model.services.records.RecordServicesException e2) {
				throw new AuthServices_RecordServicesException(e2);
			}
		}

		for (Authorization details : transaction.authsDetailsToDelete) {
			remove(details);
		}
		AuthTransaction transaction2 = new AuthTransaction();
		transaction2.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		for (String recordIdToResetIfNoAuth : transaction.recordsToResetIfNoAuths) {
			Record recordToResetIfNoAuth = recordServices.getDocumentById(recordIdToResetIfNoAuth);
			if (getRecordAuthorizations(recordToResetIfNoAuth).isEmpty()) {
				reset(recordToResetIfNoAuth, transaction2);
			}
		}
		try {
			recordServices.executeHandlingImpactsAsync(transaction2);
		} catch (com.constellio.model.services.records.RecordServicesException e) {
			throw new AuthServices_RecordServicesException(e);
		}
	}

	public void reset(Record record, AuthTransaction transaction) {
		SchemasRecordsServices schemas = schemas(record.getCollection());
		record.set(REMOVED_AUTHORIZATIONS, null);
		record.set(IS_DETACHED_AUTHORIZATIONS, false);
		transaction.add(record);

		for (Authorization authorizationDetails : schemas.searchSolrAuthorizationDetailss(
				where(schemas.authorizationDetails.target()).isEqualTo(record.getId()))) {
			execute(authorizationDeleteRequest(authorizationDetails), transaction);
		}

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();
	}

	public boolean hasDeletePermissionOnPrincipalConceptHierarchy(User user, Record principalTaxonomyConcept,
																  boolean includeRecords, List<Record> recordsHierarchy,
																  MetadataSchemasManager schemasManager) {
		if (user == User.GOD) {
			return true;
		}
		List<String> paths = principalTaxonomyConcept.getList(Schemas.PATH);
		Taxonomy principalTaxonomy = taxonomiesManager.getPrincipalTaxonomy(user.getCollection());
		validateRecordIsAPrincipalTaxonomyConcept(principalTaxonomyConcept, paths, principalTaxonomy);

		return hasPermissionOnHierarchy(user, principalTaxonomyConcept, recordsHierarchy, false);
	}

	public boolean hasDeletePermissionOnHierarchy(User user, Record record, List<Record> recordsHierarchy) {
		return hasPermissionOnHierarchy(user, record, recordsHierarchy, false);
	}

	public boolean hasRestaurationPermissionOnHierarchy(User user, Record record, List<Record> recordsHierarchy) {
		return hasPermissionOnHierarchy(user, record, recordsHierarchy, true);
	}

	public boolean hasDeletePermissionOnHierarchyNoMatterTheStatus(User user, Record record,
																   List<Record> recordsHierarchy) {
		return hasPermissionOnHierarchy(user, record, recordsHierarchy, null);
	}

	/**
	 * Use user.hasReadAccess().on(record) instead
	 */
	@Deprecated
	public boolean canRead(User user, Record record) {
		return user.hasReadAccess().on(record);
	}

	/**
	 * Use user.hasWriteAccess().on(record) instead
	 */
	@Deprecated
	public boolean canWrite(User user, Record record) {
		return user.hasWriteAccess().on(record);
	}

	/**
	 * Use user.hasDeleteAccess().on(record) instead
	 */
	@Deprecated
	public boolean canDelete(User user, Record record) {
		return user.hasDeleteAccess().on(record);
	}

	private void executeOnAuthorization(AuthTransaction transaction, AuthorizationModificationRequest request,
										Authorization authorizationDetails, Record record,
										List<String> actualPrincipals) {

		SchemasRecordsServices schemas = new SchemasRecordsServices(record.getCollection(), modelLayerFactory);

		if (request.getNewPrincipalIds() != null) {

			if (request.getNewPrincipalIds().isEmpty()) {
				throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
			}

			List<String> newPrincipalIds = principalToRecordIds(schemas, request.getNewPrincipalIds());
			((Authorization) authorizationDetails).setPrincipals(newPrincipalIds);

		}
		if (request.getNewAccessAndRoles() != null) {
			List<String> accessAndRoles = new ArrayList<String>(request.getNewAccessAndRoles());
			if (accessAndRoles.contains(Role.DELETE) && !accessAndRoles.contains(Role.READ)) {
				accessAndRoles.add(0, Role.READ);
			}
			if (accessAndRoles.contains(Role.WRITE) && !accessAndRoles.contains(Role.READ)) {
				accessAndRoles.add(0, Role.READ);
			}

			transaction.add((Authorization) authorizationDetails).setRoles(accessAndRoles);
		}

		if (request.getNewStartDate() != null || request.getNewEndDate() != null) {
			LocalDate startDate = request.getNewStartDate() == null ?
								  authorizationDetails.getStartDate() : request.getNewStartDate();
			LocalDate endDate = request.getNewEndDate() == null ? authorizationDetails.getEndDate() : request.getNewEndDate();
			validateDates(startDate, endDate);
			transaction.add((Authorization) authorizationDetails).setStartDate(startDate).setEndDate(endDate);
		}

		if (request.getNewOverridingInheritedAuths() != null) {
			transaction.add(((Authorization) authorizationDetails))
					.setOverrideInherited(request.getNewOverridingInheritedAuths());
		}
	}

	private List<String> principalToRecordIds(SchemasRecordsServices schemas, List<String> principals) {
		List<String> returnedRecordIds = new ArrayList<>();

		if (principals == null || principals.isEmpty()) {
			throw new AuthorizationsServicesRuntimeException.CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}

		for (String principal : principals) {
			try {
				returnedRecordIds.add(recordServices.getDocumentById(principal).getId());
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {

				List<MetadataSchemaType> types = asList(schemas.user.schemaType(), schemas.group.schemaType());
				Record record = searchServices.searchSingleResult(from(types).where(schemas.user.username()).isEqualTo(principal)
						.orWhere(schemas.group.code()).isEqualTo(principal));

				if (record == null) {
					throw new NoSuchPrincipalWithUsername(principal);

				} else {
					returnedRecordIds.add(record.getId());
				}
			}
		}

		return returnedRecordIds;
	}


	private boolean hasPermissionOnHierarchy(User user, Record record, List<Record> recordsHierarchy, Boolean deleted) {

		if (user == User.GOD || user.hasCollectionDeleteAccess()) {
			return true;
		}

		List<String> paths = record.getList(Schemas.PATH);
		if (paths.isEmpty()) {
			return canDelete(user, record);
		}

		for (Record aHierarchyRecord : recordsHierarchy) {
			if (!user.hasDeleteAccess().on(aHierarchyRecord)) {
				return false;
			}

			if (deleted != null) {
				Boolean logicallyDeletedStatus = record.get(Schemas.LOGICALLY_DELETED_STATUS);
				if (deleted) {
					if (!Boolean.TRUE.equals(logicallyDeletedStatus)) {
						return false;
					}

				} else {
					if (Boolean.TRUE.equals(logicallyDeletedStatus)) {
						return false;
					}
				}
			}
		}

		return true;
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
		if (authorization.getPrincipals() == null) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}

		List<String> principalIds = withoutDuplicatesAndNulls(authorization.getPrincipals());
		if (principalIds.isEmpty() && !authorization.isSynced()) {
			throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
		}
		List<Record> records = recordServices.getRecordsById(authorization.getCollection(), principalIds);
		if (principalIds.size() != records.size()) {
			throw new InvalidPrincipalsIds(records, principalIds);
		}
		return records;
	}

	private void validateCanAssignAuthorization(Record record) {
		List<String> secondaryTaxonomySchemaTypes = taxonomiesManager.getSecondaryTaxonomySchemaTypes(record.getCollection());

		String schemaType = newSchemaUtils().getSchemaTypeCode(record.getSchemaCode());
		if (secondaryTaxonomySchemaTypes.contains(schemaType)) {

			boolean hasMetadataProvidingSecurityFromThisType = false;
			for (Metadata metadata : modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection())
					.getAllMetadatas().onlyReferencesToType(schemaType)) {
				hasMetadataProvidingSecurityFromThisType |= metadata.isRelationshipProvidingSecurity();
			}

			if (!hasMetadataProvidingSecurityFromThisType) {
				throw new CannotAddAuhtorizationInNonPrincipalTaxonomy();
			}
		}

	}

	private List<String> toRolesCodes(List<Role> rolesGivingPermission) {
		List<String> roleCodes = new ArrayList<>();

		for (Role role : rolesGivingPermission) {
			roleCodes.add(role.getCode());
		}

		return roleCodes;
	}

	void refreshActivationForAllAuths(List<String> collections) {
		for (String collection : collections) {
			SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);

			for (Authorization authToDelete : schemas.searchSolrAuthorizationDetailss(
					where(schemas.authorizationDetails.endDate()).isLessThan(TimeProvider.getLocalDate()))) {

				execute(authorizationDeleteRequest(authToDelete));
			}

		}
	}

	private Map<String, String> setupAuthorizationsForDetachedRecord(AuthTransaction transaction, Record record) {
		Map<String, String> originalToCopyMap = new HashMap<>();
		List<Authorization> inheritedAuthorizations = getInheritedAuths(record);
		List<String> removedAuthorizations = record.getList(REMOVED_AUTHORIZATIONS);

		for (Authorization inheritedAuthorization : inheritedAuthorizations) {
			if (!removedAuthorizations.contains(inheritedAuthorization.getId())) {
				Authorization copy = inheritedToSpecific(transaction, record, record.getCollection(),
						inheritedAuthorization.getId());
				if (copy != null) {
					originalToCopyMap.put(inheritedAuthorization.getId(), copy.getId());
				}
			}
		}

		record.set(REMOVED_AUTHORIZATIONS, new ArrayList<>());
		record.set(IS_DETACHED_AUTHORIZATIONS, true);
		return originalToCopyMap;
	}

	private Authorization inheritedToSpecific(AuthTransaction transaction, Record record, String collection,
											  String oldAuthId) {
		Authorization inherited = getDetails(collection, oldAuthId);
		Authorization detail = newAuthorizationDetails(collection, null, inherited.getRoles(),
				inherited.getStartDate(), inherited.getEndDate(), false, inherited.isNegative());
		detail.setTarget(record.getId());
		detail.setTargetSchemaType(record.getTypeCode());
		detail.setPrincipals(new ArrayList<>(inherited.getPrincipals()));
		transaction.add(detail);

		return detail;
	}

	private void removeRemovedAuthorizationOnRecord(String authorizationId, Record record) {
		List<Object> recordAuths = new ArrayList<>();
		recordAuths.addAll(record.getList(REMOVED_AUTHORIZATIONS));
		recordAuths.remove(authorizationId);
		record.set(REMOVED_AUTHORIZATIONS, recordAuths);
	}

	private void removeInheritedAuthorizationOnRecord(String authorizationId, Record record) {
		List<Object> removedAuths = new ArrayList<>();
		removedAuths.addAll(record.getList(REMOVED_AUTHORIZATIONS));
		removedAuths.add(authorizationId);
		record.set(REMOVED_AUTHORIZATIONS, removedAuths);
	}

	private void validateDates(LocalDate startDate, LocalDate endDate) {
		LocalDate now = TimeProvider.getLocalDate();
		if ((startDate != null && endDate != null) && startDate.isAfter(endDate)) {
			throw new AuthorizationsServicesRuntimeException.StartDateGreaterThanEndDate(startDate, endDate);
		}
		if (endDate != null && endDate.isBefore(now)) {
			throw new AuthorizationsServicesRuntimeException.EndDateLessThanCurrentDate(endDate.toString());
		}
	}

	SchemaUtils newSchemaUtils() {
		return new SchemaUtils();
	}

	private Authorization newAuthorizationDetails(String collection, String id, List<String> roles,
												  LocalDate startDate, LocalDate endDate,
												  boolean overrideInherited, boolean negative) {
		Authorization details = id == null ? schemas(collection).newSolrAuthorizationDetails()
										   : schemas(collection).newSolrAuthorizationDetailsWithId(id);

		return details.setRoles(roles).setStartDate(startDate).setEndDate(endDate).setOverrideInherited(overrideInherited).setNegative(negative);
	}

	private void alertUsers(String collection, String schemaType, Record record, LocalDate sharedDate,
							LocalDate expirationDate, String sharedBy, List<String> recipientUser) {

		User sharer = schemas(collection).getUser(sharedBy);
		List<User> sendTo = new ArrayList<>();
		for (String recipient : recipientUser) {
			try {
				User user = schemas(collection).getUser(recipient);
				sendTo.add(user);
			} catch (RecordWrapperRuntimeException.WrappedRecordMustMeetRequirements ex) {
				Group group = schemas(collection).getGroup(recipient);
				sendTo.addAll(schemas(collection).getAllUsersInGroup(group, true, true));
			}
		}
		alertUsers(collection, schemaType, record, sharedDate, expirationDate, sharer, sendTo);
	}

	private void alertUsers(String collection, String schemaType, Record record, LocalDate sharedDate,
							LocalDate expirationDate, User sharedBy, List<User> recipientUser) {

		try {
			String displayURL = schemaType.equals("folder") ? "displayFolder" : "displayDocument";
			String subject = record.getTitle();
			List<String> parameters = new ArrayList<>();
			Transaction transaction = new Transaction();
			EmailToSend emailToSend = newEmailToSend(collection);
			List<EmailAddress> toAddresses = new ArrayList<>();
			for (User user : recipientUser) {
				EmailAddress toAddress = new EmailAddress();

				toAddress = new EmailAddress(user.getTitle(), user.getEmail());
				parameters.add("recipientUser" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(user.getFirstName() + " " + user.getLastName() +
																												 " (" + user.getUsername() + ")"));
				parameters.add("sharedDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(sharedDate));
				parameters.add("expirationDate" + EmailToSend.PARAMETER_SEPARATOR + formatDateToParameter(expirationDate));
				toAddresses.add(toAddress);
			}

			LocalDateTime sendDate = TimeProvider.getLocalDateTime();
			emailToSend.setTo(toAddresses);
			emailToSend.setSendOn(sendDate);
			emailToSend.setSubject(subject);
			parameters.add("subject" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(subject));
			String recordTitle = record.getTitle();
			parameters.add("title" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(recordTitle) + " (" + record.getId() + ")");

			parameters.add("sharedBy" + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(sharedBy.getFirstName() + " " + sharedBy.getLastName() +
																										" (" + sharedBy.getUsername() + ")"));
			String constellioUrl = new ConstellioEIMConfigs(modelLayerFactory.getSystemConfigurationsManager()).getConstellioUrl();
			parameters.add("constellioURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl);
			parameters.add("recordURL" + EmailToSend.PARAMETER_SEPARATOR + constellioUrl + "#!" + displayURL + "/" + record
					.getId());
			Map<Language, String> labels = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection).getSchemaType(schemaType)
					.getLabels();
			for (Map.Entry<Language, String> label : labels.entrySet()) {
				parameters.add("recordType" + "_" + label.getKey().getCode() + EmailToSend.PARAMETER_SEPARATOR + StringEscapeUtils.escapeHtml4(label.getValue().toLowerCase()));
			}

			emailToSend.setParameters(parameters);
			transaction.add(emailToSend);

			recordServices.execute(transaction);

		} catch (RecordServicesException e) {
			LOGGER.error("Cannot alert user", e);
		}

	}

	private String formatDateToParameter(LocalDate date) {
		if (date == null) {
			return "";
		}
		return date.toString("yyyy-MM-dd");
	}

	private EmailToSend newEmailToSend(String collection) {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collection);
		MetadataSchema schema = types.getSchemaType(EmailToSend.SCHEMA_TYPE).getDefaultSchema();
		Record emailToSendRecord = recordServices.newRecordWithSchema(schema);
		return new EmailToSend(emailToSendRecord, types);
	}
}
