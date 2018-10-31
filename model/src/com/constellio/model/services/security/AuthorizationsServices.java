package com.constellio.model.services.security;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.TransactionRecordsReindexation;
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
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.logging.LoggingServices;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
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
import org.joda.time.LocalDate;
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
		AuthorizationDetails authDetails = getDetails(collection, id);
		if (authDetails == null) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithId(id);
		}
		List<String> grantedToPrincipals = authDetails.getPrincipals();
		return new Authorization(authDetails, grantedToPrincipals);
	}

	public List<User> getUsersWithGlobalPermissionInCollection(String permission, String collection) {
		return getUsersWithGlobalPermissionInCollectionExcludingRoles(permission, collection, new ArrayList<String>());
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
			if (auth.getDetail().getRoles().contains(role)) {
				List<String> principals = auth.getGrantedToPrincipals();
				List<Record> principalRecords = recordServices.getRecordsById(auth.getDetail().getCollection(), principals);
				if (principals.size() != principalRecords.size()) {
					throw new InvalidPrincipalsIds(principalRecords, principals);
				}
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

		SolrAuthorizationDetails details = newAuthorizationDetails(request.getCollection(), request.getId(), request.getRoles(),
				request.getStart(), request.getEnd(), request.isOverridingInheritedAuths(), request.isNegative())
				.setTarget(request.getTarget()).setTargetSchemaType(record.getTypeCode());
		details.setPrincipals(principalToRecordIds(schemas(record.getCollection()), request.getPrincipals()));
		return add(new Authorization(details, request.getPrincipals()), request.getExecutedBy());
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
	private String add(Authorization authorization, User userAddingTheAuth) {

		AuthTransaction transaction = new AuthTransaction();

		SolrAuthorizationDetails authorizationDetail = (SolrAuthorizationDetails) authorization.getDetail();
		authorizationDetail.setTarget(authorization.getGrantedOnRecord());
		Record record = recordServices.getDocumentById(authorization.getGrantedOnRecord());
		authorizationDetail.setTargetSchemaType(record.getTypeCode());
		authorizationDetail.setPrincipals(authorization.getGrantedToPrincipals());
		transaction.add(authorizationDetail);
		String authId = authorizationDetail.getId();

		validateDates(authorizationDetail.getStartDate(), authorizationDetail.getEndDate());
		authorizationDetail.setPrincipals(authorization.getGrantedToPrincipals());

		if (!transaction.getRecordIds().contains(authorization.getGrantedOnRecord())) {
			transaction.add(record);
		}
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());

		executeTransaction(transaction);

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();

		if (userAddingTheAuth != null) {
			loggingServices.grantPermission(authorization, userAddingTheAuth);
		}

		return authId;
	}

	public void execute(AuthorizationDeleteRequest request) {
		AuthTransaction transaction = new AuthTransaction();
		AuthorizationDetails removedAuthorization = execute(request, transaction);
		String grantedOnRecord = removedAuthorization.getTarget();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		if (!transaction.getRecordIds().contains(grantedOnRecord)) {
			try {
				transaction.add(recordServices.getDocumentById(grantedOnRecord));
			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
				LOGGER.info("Failed to invalidate hasChildrenCache after deletion of authorization", e);
			}
		}

		executeTransaction(transaction);
		if (grantedOnRecord != null) {
			//			try {
			////				refreshCaches(recordServices.getDocumentById(grantedOnRecord),
			////						new ArrayList<String>(), request.get);
			//			} catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
			//				LOGGER.info("Failed to invalidate hasChildrenCache after deletion of authorization", e);
			//			}

		}

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();
	}

	private static class AuthTransaction extends Transaction {

		Set<String> recordsToResetIfNoAuths = new HashSet<>();

		List<SolrAuthorizationDetails> authsDetailsToDelete = new ArrayList<>();

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

	private AuthorizationDetails execute(AuthorizationDeleteRequest request, AuthTransaction transaction) {

		List<String> authId = asList(request.getAuthId());
		Authorization auth = getAuthorization(request.getCollection(), request.getAuthId());
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(request.getCollection())
				//TODO Really necessary Authorizations ?
				.where(REMOVED_AUTHORIZATIONS).isContaining(authId)
				.orWhere(Schemas.ATTACHED_ANCESTORS).isEqualTo(auth.getGrantedOnRecord()));
		List<Record> recordsWithRemovedAuth = searchServices.search(query);

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

		AuthorizationDetails removedAuthorization = null;
		try {
			removedAuthorization = getDetails(request.getCollection(), request.getAuthId());
			if (removedAuthorization != null) {
				transaction.authsDetailsToDelete.add((SolrAuthorizationDetails) removedAuthorization);
				transaction.add(((SolrAuthorizationDetails) removedAuthorization).getWrappedRecord()
						.set(Schemas.LOGICALLY_DELETED_STATUS, true));

			}

			try {
				Record target = recordServices.getDocumentById(removedAuthorization.getTarget());
				if (request.isReattachIfLastAuthDeleted() && Boolean.TRUE == target.get(Schemas.IS_DETACHED_AUTHORIZATIONS)) {
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
	 *
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

		modelLayerFactory.getTaxonomiesSearchServicesCache().invalidateAll();

		return response;
	}

	private List<AuthorizationDetails> getInheritedAuths(Record record) {
		SchemasRecordsServices schemas = schemas(record.getCollection());

		List<AuthorizationDetails> authorizationDetails = new ArrayList<>();

		Set<String> recordsIdsWithPosibleAuths = new HashSet<>();
		recordsIdsWithPosibleAuths.addAll(record.<String>getList(ATTACHED_ANCESTORS));
		recordsIdsWithPosibleAuths.remove(record.getId());

		for (String ancestorId : record.<String>getList(ATTACHED_ANCESTORS)) {
			if (!ancestorId.equals(record.getId())) {

				Record ancestor = recordServices.getDocumentById(ancestorId);
				MetadataSchema schema = schemasManager.getSchemaOf(ancestor);
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.isRelationshipProvidingSecurity()) {
						recordsIdsWithPosibleAuths.addAll(ancestor.<String>getValues(metadata));
					}
				}
			}
		}

		for (SolrAuthorizationDetails authorizationDetail : schemas.getAllAuthorizationsInUnmodifiableState()) {
			if (recordsIdsWithPosibleAuths.contains(authorizationDetail.getTarget())) {
				authorizationDetails.add(authorizationDetail.getCopyOfOriginalRecord());
			}
		}


		return authorizationDetails;
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

		AuthTransaction transaction = new AuthTransaction();
		transaction.getRecordUpdateOptions().setForcedReindexationOfMetadatas(TransactionRecordsReindexation.ALL());
		transaction.add(record);
		String authTarget = authorization.getDetail().getTarget();
		String authId = authorization.getDetail().getId();
		boolean directlyTargetted = authTarget.equals(record.getId());
		boolean inherited = !directlyTargetted && record.getList(ATTACHED_ANCESTORS).contains(authTarget);
		boolean nonTaxonomyAuth = record.<String>getList(Schemas.NON_TAXONOMY_AUTHORIZATIONS).contains(authId);
		if (!directlyTargetted && !inherited && !nonTaxonomyAuth) {
			throw new AuthorizationsServicesRuntimeException.NoSuchAuthorizationWithIdOnRecord(authId, record);
		}

		AuthorizationModificationResponse response;
		if (request.isRemovedOnRecord()) {
			if (directlyTargetted) {
				execute(authorizationDeleteRequest(authorization.getDetail()).setExecutedBy(request.getExecutedBy()),
						transaction);

			} else {
				removeInheritedAuthorizationOnRecord(authId, record);
			}

			response = new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

		} else {

			if (directlyTargetted) {
				transaction.add((SolrAuthorizationDetails) authorization.getDetail());
				executeOnAuthorization(transaction, request, authorization.getDetail(), record,
						authorization.getGrantedToPrincipals());
				response = new AuthorizationModificationResponse(false, null, Collections.<String, String>emptyMap());

			} else {
				AuthorizationDetails copy = inheritedToSpecific(transaction, record, record.getCollection(),
						authorization.getDetail().getId());
				record.addValueToList(REMOVED_AUTHORIZATIONS, authorization.getDetail().getId());

				executeOnAuthorization(transaction, request, copy, record, authorization.getGrantedToPrincipals());

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
	 * @param record A securized record to detach
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
		List<String> authIds;
		if (User.DEFAULT_SCHEMA.equals(record.getSchemaCode())) {
			authIds = new ArrayList<>(getAuthsReceivedBy(schemas.wrapUser(record)));

		} else if (Group.DEFAULT_SCHEMA.equals(record.getSchemaCode())) {
			authIds = new ArrayList<>(getAuthsReceivedBy(schemas.wrapGroup(record), schemas));

		} else {
			authIds = new ArrayList<>();
			for (AuthorizationDetails authorizationDetails : schemas.getAllAuthorizationsInUnmodifiableState()) {

				boolean targettingRecordOrAncestor =
						(record.getList(ATTACHED_ANCESTORS).contains(authorizationDetails.getTarget())
						 || record.getId().equals(authorizationDetails.getTarget()))
						&& !record.getList(ALL_REMOVED_AUTHS).contains(authorizationDetails.getId());

				if (targettingRecordOrAncestor) {
					authIds.add(authorizationDetails.getId());
				}
			}

		}

		List<Authorization> authorizations = new ArrayList<>();
		for (String authId : authIds) {
			AuthorizationDetails authDetails = getDetails(record.getCollection(), authId);
			if (authDetails != null) {
				List<String> grantedToPrincipals = authDetails.getPrincipals();
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
	 *
	 * @param record The securized record
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

		for (AuthorizationDetails details : transaction.authsDetailsToDelete) {
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

		for (AuthorizationDetails authorizationDetails : schemas.searchSolrAuthorizationDetailss(
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
										AuthorizationDetails authorizationDetails, Record record,
										List<String> actualPrincipals) {

		SchemasRecordsServices schemas = new SchemasRecordsServices(record.getCollection(), modelLayerFactory);

		if (request.getNewPrincipalIds() != null) {

			if (request.getNewPrincipalIds().isEmpty()) {
				throw new CannotAddUpdateWithoutPrincipalsAndOrTargetRecords();
			}

			List<String> newPrincipalIds = principalToRecordIds(schemas, request.getNewPrincipalIds());
			((SolrAuthorizationDetails) authorizationDetails).setPrincipals(newPrincipalIds);

		}
		if (request.getNewAccessAndRoles() != null) {
			List<String> accessAndRoles = new ArrayList<String>(request.getNewAccessAndRoles());
			if (accessAndRoles.contains(Role.DELETE) && !accessAndRoles.contains(Role.READ)) {
				accessAndRoles.add(0, Role.READ);
			}
			if (accessAndRoles.contains(Role.WRITE) && !accessAndRoles.contains(Role.READ)) {
				accessAndRoles.add(0, Role.READ);
			}

			transaction.add((SolrAuthorizationDetails) authorizationDetails).setRoles(accessAndRoles);
		}

		if (request.getNewStartDate() != null || request.getNewEndDate() != null) {
			LocalDate startDate = request.getNewStartDate() == null ?
								  authorizationDetails.getStartDate() : request.getNewStartDate();
			LocalDate endDate = request.getNewEndDate() == null ? authorizationDetails.getEndDate() : request.getNewEndDate();
			validateDates(startDate, endDate);
			transaction.add((SolrAuthorizationDetails) authorizationDetails).setStartDate(startDate).setEndDate(endDate);
		}

		if (request.getNewOverridingInheritedAuths() != null) {
			transaction.add(((SolrAuthorizationDetails) authorizationDetails))
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

			for (AuthorizationDetails authToDelete : schemas.searchSolrAuthorizationDetailss(
					where(schemas.authorizationDetails.endDate()).isLessThan(TimeProvider.getLocalDate()))) {

				execute(authorizationDeleteRequest(authToDelete));
			}

		}
	}

	private Map<String, String> setupAuthorizationsForDetachedRecord(AuthTransaction transaction, Record record) {
		Map<String, String> originalToCopyMap = new HashMap<>();
		List<AuthorizationDetails> inheritedAuthorizations = getInheritedAuths(record);
		List<String> removedAuthorizations = record.getList(REMOVED_AUTHORIZATIONS);

		for (AuthorizationDetails inheritedAuthorization : inheritedAuthorizations) {
			if (!removedAuthorizations.contains(inheritedAuthorization.getId())) {
				AuthorizationDetails copy = inheritedToSpecific(transaction, record, record.getCollection(),
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

	private AuthorizationDetails inheritedToSpecific(AuthTransaction transaction, Record record, String collection,
													 String oldAuthId) {
		AuthorizationDetails inherited = getDetails(collection, oldAuthId);
		SolrAuthorizationDetails detail = newAuthorizationDetails(collection, null, inherited.getRoles(),
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
															 LocalDate startDate, LocalDate endDate,
															 boolean overrideInherited, boolean negative) {
		SolrAuthorizationDetails details = id == null ? schemas(collection).newSolrAuthorizationDetails()
													  : schemas(collection).newSolrAuthorizationDetailsWithId(id);

		return details.setRoles(roles).setStartDate(startDate).setEndDate(endDate).setOverrideInherited(overrideInherited).setNegative(negative);
	}

}
