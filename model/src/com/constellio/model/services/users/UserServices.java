package com.constellio.model.services.users;

import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.enums.GroupAuthorizationsInheritance;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder;
import com.constellio.model.entities.records.ConditionnedActionExecutorInBatchBuilder.RecordScript;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.RecordUpdateOptions;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.GroupAddUpdateRequest;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.entities.security.global.UserSyncMode;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordPhysicalDeleteOptions;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.validators.EmailValidator;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserCredentialsManagerRuntimeException.UserCredentialsManagerRuntimeException_CannotExecuteTransaction;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_AtLeastOneCollectionRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotAssignUserToGroupsInOtherCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotExcuteTransaction;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveSyncedGroupFromSyncedCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_EmailRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_FirstNameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidCollectionForGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidCollectionForUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidToken;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUserNameOrPassword;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUsername;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_LastNameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NameRequired;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserPermissionDeniedToDelete;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.records.wrappers.Group.wrapNullable;
import static com.constellio.model.entities.schemas.Schemas.ALL_REFERENCES;
import static com.constellio.model.entities.schemas.Schemas.CREATED_BY;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.entities.schemas.Schemas.MODIFIED_BY;
import static com.constellio.model.entities.security.global.UserCredentialStatus.ACTIVE;
import static com.constellio.model.entities.security.global.UserCredentialStatus.DISABLED;
import static com.constellio.model.entities.security.global.UserCredentialStatus.PENDING;
import static com.constellio.model.entities.security.global.UserCredentialStatus.SUSPENDED;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.GROUP_AUTHORIZATIONS_INHERITANCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.users.UserUtils.cleanUsername;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class UserServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServices.class);
	public static final String ADMIN = "admin";
	public static final String GROUP_CANNOT_BE_DELETED = "groupCannotBeDeleted";
	public static final String USER_CANNOT_BE_DELETED = "userCannotBeDeleted";
	private final CollectionsListManager collectionsListManager;
	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final MetadataSchemasManager metadataSchemasManager;
	private final AuthenticationService authenticationService;
	private final LDAPConfigurationManager ldapConfigurationManager;
	private final RolesManager rolesManager;
	private final ModelLayerConfiguration modelLayerConfiguration;
	private final UniqueIdGenerator secondaryUniqueIdGenerator;
	private final AuthorizationsServices authorizationsServices;
	private final ModelLayerFactory modelLayerFactory;
	private final SchemasRecordsServices schemas;

	public UserServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.recordServices = modelLayerFactory.newRecordServices();
		this.searchServices = modelLayerFactory.newSearchServices();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.authenticationService = modelLayerFactory.newAuthenticationService();
		this.modelLayerConfiguration = modelLayerFactory.getConfiguration();
		this.ldapConfigurationManager = modelLayerFactory.getLdapConfigurationManager();
		this.rolesManager = modelLayerFactory.getRolesManager();
		this.secondaryUniqueIdGenerator = modelLayerFactory.getDataLayerFactory().getSecondaryUniqueIdGenerator();
		this.authorizationsServices = modelLayerFactory.newAuthorizationsServices();
		this.schemas = SchemasRecordsServices.usingMainModelLayerFactory(com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION, modelLayerFactory);
	}

	public GroupAddUpdateRequest newGlobalGroup(String code) {
		return new GroupAddUpdateRequest(code);
	}

	public void execute(String username,
						Consumer<com.constellio.model.services.users.UserAddUpdateRequest> requestConsumer) {
		com.constellio.model.services.users.UserAddUpdateRequest request = addUpdate(username);
		requestConsumer.accept(request);
		execute(request);
	}

	public void createUser(String username,
						   Consumer<com.constellio.model.services.users.UserAddUpdateRequest> requestConsumer) {

		UserCredential userCredential = getUserCredential(username);
		com.constellio.model.services.users.UserAddUpdateRequest request;
		if (userCredential == null) {
			if (!cleanUsername(username).equals(username)) {
				throw new UserServicesRuntimeException_InvalidUsername(username);
			}
			request = new com.constellio.model.services.users.UserAddUpdateRequest(cleanUsername(username), Collections.emptyList(), Collections.emptyList());
		} else {
			throw new UserServicesRuntimeException.UserServicesRuntimeException_UserAlreadyExists(username);

		}

		request.setStatusForAllCollections(ACTIVE);
		requestConsumer.accept(request);
		execute(request);
	}

	//Created from refact
	public boolean hasUsedSystem(User user) {
		boolean referencedByRecords = searchServices.hasResults(fromAllSchemasIn(user.getCollection())
				.whereAny(ALL_REFERENCES, CREATED_BY, MODIFIED_BY).isEqualTo(user));

		boolean referencedByAuth = !modelLayerFactory.newRecordServices().getSecurityModel(user.getCollection())
				.getAuthorizationsToPrincipal(user.getId(), false).isEmpty();

		return referencedByRecords || referencedByAuth;

	}

	public boolean hasUsedSystem(Group group) {
		boolean referencedByRecords = searchServices.hasResults(fromAllSchemasIn(group.getCollection())
				.whereAny(ALL_REFERENCES).isEqualTo(group));

		boolean referencedByAuth = !modelLayerFactory.newRecordServices().getSecurityModel(group.getCollection())
				.getAuthorizationsToPrincipal(group.getId(), false).isEmpty();

		return referencedByRecords || referencedByAuth;
	}

	//Created from refact
	private SystemWideUserInfos systemWideUserInfosOrNull(String username) {
		UserCredential credential = existingUserCredentialOrNull(username);
		return credential == null ? null : toSystemWideUserInfos(credential);
	}



	//Created from refact
	private UserCredential existingUserCredentialOrNull(String username) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(SYSTEM_COLLECTION, modelLayerFactory);
		Record userRecord = searchServices.searchSingleResult(from(schemas.credentialSchemaType())
				.where(schemas.credentialUsername()).isEqualTo(username));
		return userRecord == null ? null : schemas.wrapUserCredential(userRecord);
	}

	//Created from refact
	private User existingUserOrNull(String username, String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		Record userRecord = searchServices.searchSingleResult(from(schemas.user.schemaType())
				.where(schemas.user.username()).isEqualTo(username));
		return userRecord == null ? null : schemas.wrapUser(userRecord);
	}

	//Created from refact
	private User existingUserInAnyCollectionOrNull(String username) {
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {
			User user = existingUserOrNull(username, collection);
			if (user != null) {
				return user;
			}
		}

		return null;
	}

	//Created from refact
	private SchemasRecordsServices schemas(String collection) {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	//Created from refact
	private boolean isSyncedUserMetadata(Metadata metadata) {
		return asList(User.USERNAME, User.FIRSTNAME, User.LASTNAME, User.EMAIL, User.PERSONAL_EMAILS, User.SYSTEM_ADMIN,
				User.JOB_TITLE, User.PHONE, User.FAX, User.ADDRESS, User.STATUS).contains(metadata.getLocalCode());
	}

	//Created from refact
	private User userSyncedTo(UserCredential userCredential, UserAddUpdateRequest request, String collection) {

		User user = existingUserOrNull(request.getUsername(), collection);
		MetadataSchema schema = schemas(collection).user.schema();
		if (user == null) {
			user = schemas(collection).newUser();
			user.setUsername(request.getUsername());
			user.setStatus(ACTIVE);
			User userInOtherCollection = existingUserInAnyCollectionOrNull(request.getUsername());

			if (userInOtherCollection != null) {

				MetadataSchema sourceSchema = schemas(userInOtherCollection.getCollection()).user.schema();

				for (Metadata metadata : sourceSchema.getMetadatas().only(this::isSyncedUserMetadata)) {
					if (schema.hasMetadataWithCode(metadata.getLocalCode())) {
						user.set(metadata, userInOtherCollection.get(metadata));
					}
				}
			} else {
				user.setFirstName(userCredential.getFirstName());
				user.setLastName(userCredential.getLastName());
				user.setEmail(userCredential.getEmail());
				user.setSystemAdmin(userCredential.isSystemAdmin());
			}


		}

		List<String> userGroups = new ArrayList<>(user.getUserGroups());
		if (request.getAddToGroup() != null) {
			for (String groupCode : request.getAddToGroup()) {
				if (getGroupInCollection(groupCode, collection) != null) {
					Group group = getGroupInCollection(groupCode, collection);
					verifyUserAndGroupSync(userCredential, group, request.isLdapSyncRequest());
					if (!userGroups.contains(group.getId())) {
						userGroups.add(group.getId());
					}
				}
			}
		}
		if (request.getAddToGroupInCollection() != null && request.getAddToGroupInCollection().containsKey(collection)) {
			for (String groupCode : request.getAddToGroupInCollection().get(collection)) {
				if (getGroupInCollection(groupCode, collection) != null) {
					Group group = getGroupInCollection(groupCode, collection);
					verifyUserAndGroupSync(userCredential, group, request.isLdapSyncRequest());
					if (!userGroups.contains(group.getId())) {
						userGroups.add(group.getId());
					}
				}
			}
		}
		List<String> removeFromGroup = request.getRemoveFromGroup();
		if (removeFromGroup != null) {
			for (String groupCode : removeFromGroup) {
				Group group = getGroupInCollection(groupCode, collection);
				if (getGroupInCollection(groupCode, collection) != null) {
					verifyUserAndGroupSync(userCredential, group, request.isLdapSyncRequest());
					userGroups.remove(getGroupInCollection(groupCode, collection).getId());
				}
			}
		}
		Map<String, List<String>> removeFromGroupInCollection = request.getRemoveFromGroupInCollection();
		if (removeFromGroupInCollection != null && removeFromGroupInCollection.keySet().contains(collection)) {
			for (String groupCode : removeFromGroupInCollection.get(collection)) {
				Group group = getGroupInCollection(groupCode, collection);

				if (getGroupInCollection(groupCode, collection) != null) {
					verifyUserAndGroupSync(userCredential, group, request.isLdapSyncRequest());
					String groupId = getGroupInCollection(groupCode, collection).getId();
					if (userGroups.contains(groupId)) {
						userGroups.remove(groupId);
					}
				}
			}
		}

		user.setUserGroups(userGroups);

		Map<String, Object> modifiedMetadatasSpecificToCollection =
				request.getModifiedCollectionsProperties().get(collection);
		for (Metadata metadata : schema.getMetadatas().only(this::isSyncedUserMetadata)) {
			if (request.getExtraMetadatas().containsKey(metadata.getLocalCode())) {
				user.set(metadata, request.getExtraMetadatas().get(metadata.getLocalCode()));
			}

			if (modifiedMetadatasSpecificToCollection != null
				&& modifiedMetadatasSpecificToCollection.containsKey(metadata.getLocalCode())) {
				user.set(metadata, modifiedMetadatasSpecificToCollection.get(metadata.getLocalCode()));
			}
		}

		user.setSystemAdmin(userCredential.isSystemAdmin());

		boolean logicallyDeletedStatus = user.getStatus() != ACTIVE;
		user.set(LOGICALLY_DELETED_STATUS, logicallyDeletedStatus ? Boolean.TRUE : null);

		return user;
	}

	private void verifyUserUpdateRequestedSyncedStatus(UserCredential userCredential, UserAddUpdateRequest request) {
		if (!request.isStopSyncingLDAP() && !request.isLdapSyncRequest()
			&& userCredential.getSyncMode() == UserSyncMode.SYNCED) {

			if (request.isModified(User.FIRSTNAME) || request.isModified(User.LASTNAME)) {
				throw new UserServicesRuntimeException
						.UserServicesRuntimeException_CannotChangeNameOfSyncedUser(userCredential.getUsername());
			}

			if (request.isModified(User.EMAIL)) {
				throw new UserServicesRuntimeException
						.UserServicesRuntimeException_CannotChangeEmailOfSyncedUser(userCredential.getUsername());
			}

			if (request.isModified(User.STATUS)) {
				throw new UserServicesRuntimeException
						.UserServicesRuntimeException_CannotChangeStatusOfSyncedUser(userCredential.getUsername());
			}
		}
	}

	private void verifyUserAndGroupSync(UserCredential userCredential, Group group, boolean isLdapSyncRequest) {
		if (!isLdapSyncRequest && !group.isLocallyCreated() && userCredential.getSyncMode() == UserSyncMode.SYNCED) {
			throw new UserServicesRuntimeException
					.UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup(userCredential.getUsername(),
					group.getCode());
		}
	}

	//Created from refact
	private UserCredential userCredentialSyncedTo(UserAddUpdateRequest request) {
		UserCredential userCredential = existingUserCredentialOrNull(request.getUsername());
		if (userCredential == null) {

			if (!EmailValidator.isValid((String) request.getExtraMetadatas().get(User.EMAIL))) {
				throw new UserServicesRuntimeException_EmailRequired(request.getUsername());
			}

			if (!StringUtils.isNotBlank((String) request.getExtraMetadatas().get(User.FIRSTNAME))) {
				throw new UserServicesRuntimeException_FirstNameRequired(request.getUsername());
			}

			if (!StringUtils.isNotBlank((String) request.getExtraMetadatas().get(User.LASTNAME))) {
				throw new UserServicesRuntimeException_LastNameRequired(request.getUsername());
			}

			userCredential = schemas.newCredential();
			userCredential._setUsername(request.getUsername());
			userCredential.setFirstName((String) request.getExtraMetadatas().get(User.FIRSTNAME));
			userCredential.setLastName((String) request.getExtraMetadatas().get(User.LASTNAME));
			userCredential.setEmail((String) request.getExtraMetadatas().get(User.EMAIL));

		}
		else{
			verifyUserUpdateRequestedSyncedStatus( userCredential,  request);
		}

		MetadataSchema credentialSchema = schemas(SYSTEM_COLLECTION).credentialSchema();
		for (Map.Entry<String, Object> entry : request.getExtraMetadatas().entrySet()) {
			if (credentialSchema.hasMetadataWithCode(entry.getKey())) {
				userCredential.set(entry.getKey(), entry.getValue());
			}
		}

		if (request.getExtraMetadatas().containsKey(User.FIRSTNAME)) {
			userCredential.setFirstName((String) request.getExtraMetadatas().get(User.FIRSTNAME));
		}

		if (request.getExtraMetadatas().containsKey(User.LASTNAME)) {
			userCredential.setLastName((String) request.getExtraMetadatas().get(User.LASTNAME));
		}

		if (request.getExtraMetadatas().containsKey(User.EMAIL)) {
			userCredential.setEmail((String) request.getExtraMetadatas().get(User.EMAIL));
		}

		if (request.isLdapSyncRequest()) {
			userCredential.setSyncMode(UserSyncMode.SYNCED);
		} else if (request.isStopSyncingLDAP() && userCredential.getSyncMode() == UserSyncMode.SYNCED) {
			userCredential.setSyncMode(UserSyncMode.NOT_SYNCED);
		} else if (request.isResumeSyncingLDAP() && userCredential.getSyncMode() == UserSyncMode.NOT_SYNCED) {
			userCredential.setSyncMode(UserSyncMode.SYNCED);
		}

		return userCredential;
	}

	//Created from refact

	private boolean removeUserFrom(String username, String collection, UserAddUpdateResponse response) {
		User user = getUserRecordInCollection(username, collection);
		if (user == null) {
			return true;
		} else {
			if (hasUsedSystem(user) || username.equals("admin")) {
				try {
					recordServices.update(user.setStatus(DISABLED).set(LOGICALLY_DELETED_STATUS, true));
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("username", username);
					parameters.put("collection", collection);
					response.addWarning(new ValidationError(UserServices.class, USER_CANNOT_BE_DELETED, parameters));
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
				return false;
			} else {
				recordServices.physicallyDeleteNoMatterTheStatus(user, User.GOD, new RecordPhysicalDeleteOptions());
				return true;
			}
		}

	}

	private void removeGroupFrom(String groupCode, String collection,
								 GroupAddUpdateResponse response) {
		Group group = getGroupInCollection(groupCode, collection);
		if (group != null) {
			if (hasUsedSystem(group)) {
				try {
					recordServices.update(group.setStatus(GlobalGroupStatus.INACTIVE).set(LOGICALLY_DELETED_STATUS, true));
					Map<String, Object> parameters = new HashMap<>();
					parameters.put("groupCode", groupCode);
					parameters.put("collection", collection);
					response.addWarning(new ValidationError(UserServices.class, GROUP_CANNOT_BE_DELETED, parameters));
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}
			} else {
				recordServices.physicallyDeleteNoMatterTheStatus(group, User.GOD, new RecordPhysicalDeleteOptions());
			}
		}
	}

	private void deactivateGroupFrom(String groupCode, String collection) {
		Group group = getGroupInCollection(groupCode, collection);
		try {
			recordServices.update(group.setStatus(GlobalGroupStatus.INACTIVE).set(LOGICALLY_DELETED_STATUS, true));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	private void deactivateGroupHierarchy(String groupCode, String collection) {
		deactivateGroupChildren(groupCode, collection);
		deactivateGroupFrom(groupCode, collection);
	}

	private void deactivateGroupChildren(String groupCode, String collection) {
		for (Group child : getChildrenOfGroupInCollection(groupCode, collection)) {
			deactivateGroupChildren(child.getCode(), collection);
			deactivateGroupFrom(child.getCode(), collection);
		}
	}

	public static class GroupAddUpdateResponse {

		@Getter
		private List<ValidationError> warnings = new ArrayList<>();


		public GroupAddUpdateResponse addWarning(ValidationError validationError) {
			this.warnings.add(validationError);
			return this;
		}
	}

	public static class UserAddUpdateResponse {

		@Getter
		private List<ValidationError> warnings = new ArrayList<>();


		public UserAddUpdateResponse addWarning(ValidationError validationError) {
			this.warnings.add(validationError);
			return this;
		}
	}

	public UserAddUpdateResponse execute(com.constellio.model.services.users.UserAddUpdateRequest request) {
		UserAddUpdateResponse response = new UserAddUpdateResponse();
		SystemWideUserInfos userInfos = systemWideUserInfosOrNull(request.getUsername());

		if (userInfos == null && (request.getAddToCollections() == null || request.getAddToCollections().isEmpty())
			&& !"admin".equals(request.getUsername()) && Toggle.VALIDATE_USER_COLLECTIONS.isEnabled()) {
			throw new UserServicesRuntimeException_AtLeastOneCollectionRequired(request.getUsername());
		}

		boolean removingAllCollections = userInfos != null
										 && request.getRemoveFromCollections() != null
										 && !request.getRemoveFromCollections().isEmpty()
										 && userInfos.getCollections().size() == request.getRemoveFromCollections().size()
										 && userInfos.getCollections().containsAll(request.getRemoveFromCollections())
										 && (request.getAddToCollections() == null || request.getAddToCollections().isEmpty());

		if (request.isMarkedForDeletionInAllCollections() || removingAllCollections) {
			deleteUser(request, response);

		} else {

			validateNewCollections(request);
			validateNewGroups(request, userInfos);

			UserCredential userCredential = userCredentialSyncedTo(request);

			if (request.getNewTokens() != null || request.getRemovedtokens() != null) {
				Map<String, LocalDateTime> tokens = new HashMap<>(userCredential.getAccessTokens());
				if (request.getNewTokens() != null) {
					for (Entry<String, LocalDateTime> token : request.getNewTokens().entrySet()) {
						if (!tokens.containsKey(token.getKey())) {
							tokens.put(token.getKey(), token.getValue());
						}
					}
				}

				if (request.getRemovedtokens() != null) {
					for (String token : request.getRemovedtokens()) {
						tokens.remove(token);
					}
				}

				userCredential.setAccessTokens(tokens);
			}


			try {
				recordServices.add(userCredential);
			} catch (RecordServicesException e) {
				throw new RuntimeException(e);
			}

			List<String> collections = new ArrayList<>();
			if (userInfos != null) {
				collections.addAll(userInfos.getCollections());
			}

			if (request.getAddToCollections() != null) {
				for (String newCollection : request.getAddToCollections()) {
					if (!collections.contains(newCollection)) {
						collections.add(newCollection);
					}
				}
			}

			for (String collection : collections) {
				User user = userSyncedTo(userCredential, request, collection);

				try {
					recordServices.add(user);
				} catch (RecordServicesException e) {
					throw new RuntimeException(e);
				}

			}

			if (request.getRemoveFromCollections() != null) {
				for (String removedCollection : request.getRemoveFromCollections()) {
					verifyUserIsSyncedInCollectionSynced(userCredential, request.isLdapSyncRequest(), removedCollection);
					removeUserFrom(request.getUsername(), removedCollection, response);
				}
			}

		}
		return response;
	}

	private void verifyUserIsSyncedInCollectionSynced(UserCredential userCredential, boolean ldapSyncRequest,
													  String removedCollection) {
		List<String> syncCollections = ldapConfigurationManager
				.getLDAPUserSyncConfiguration(false).getSelectedCollectionsCodes();
		if (!ldapSyncRequest
			&& userCredential.getSyncMode() == UserSyncMode.SYNCED
			&& syncCollections.stream().anyMatch(syncCollection -> syncCollection.equals(removedCollection))) {
			throw new UserServicesRuntimeException.
					UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection(userCredential.getUsername(), removedCollection);
		}
	}

	private void validateNewCollections(UserAddUpdateRequest request) {
		if (request.getAddToCollections() != null) {
			request.getAddToCollections().forEach((collection) -> {
				if (!collectionsListManager.getCollectionsExcludingSystem().contains(collection)) {
					throw new UserServicesRuntimeException_InvalidCollectionForUser(request.getUsername(), collection);
				}
			});
		}
	}

	private void validateNewGroups(UserAddUpdateRequest request, SystemWideUserInfos userInfos) {
		if (request.getAddToGroup() != null) {
			request.getAddToGroup().forEach((group) -> {
				if (getNullableGroup(group) == null) {
					throw new UserServicesRuntimeException_InvalidGroup(group);
				}
			});
		}
		if (request.getAddToGroupInCollection() != null) {
			List<String> userCollections = new ArrayList<>();
			if (userInfos != null) {
				userCollections.addAll(userInfos.getCollections());
			}
			if (request.getAddToCollections() != null) {
				userCollections.addAll(request.getAddToCollections());
			}
			request.getAddToGroupInCollection().forEach((collection, groups) -> {
				for (String group : groups) {
					if (userCollections.contains(collection)) {
						if (getGroupInCollection(group, collection) == null) {
							throw new UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection(request.getUsername(), group, collection);
						}
					} else {
						throw new UserServicesRuntimeException_CannotAssignUserToGroupsInOtherCollection(request.getUsername(), group, collection);
					}
				}
			});
		}
	}

	public GroupAddUpdateRequest createGlobalGroup(
			String code, String name, List<String> collections, String parent, GlobalGroupStatus status,
			boolean locallyCreated) {

		return new GroupAddUpdateRequest(code)
				.setName(name)
				.addCollections(collections)
				.setParent(parent)
				.setStatusInAllCollections(status)
				.setLocallyCreated(locallyCreated);
	}

	public void createGroup(String code, Consumer<GroupAddUpdateRequest> requestConsumer) {
		GroupAddUpdateRequest request = new GroupAddUpdateRequest(code)
				.setName(code)
				.setStatusInAllCollections(GlobalGroupStatus.ACTIVE)
				.setLocallyCreated(true);
		requestConsumer.accept(request);
		execute(request);
	}

	public GroupAddUpdateResponse executeGroupRequest(String groupCode,
															  Consumer<GroupAddUpdateRequest> requestConsumer) {
		GroupAddUpdateRequest request = request(groupCode);
		requestConsumer.accept(request);
		return execute(request);
	}

	public GroupAddUpdateResponse execute(GroupAddUpdateRequest request) {
		GroupAddUpdateResponse response = new GroupAddUpdateResponse();
		if (request.isMarkedForDeletionInAllCollections()) {
			deleteGroup(request.getCode(), response);
		}

		if (request.getMarkedForDeletionInCollections() != null && !request.getMarkedForDeletionInCollections().isEmpty()) {
			request.getMarkedForDeletionInCollections().stream()
					.forEach(collection -> deleteGroupFromCollection(request.getCode(), collection, response));
		}

		validateNewGroupMetadatas(request);
		validateNewCollections(request);

		sync(request, response);
		SystemWideGroup systemWideGroup = getGroup(request);
		if (request.getModifiedAttributes().containsKey(GlobalGroup.STATUS) || request.getNewCollections() != null) {
			if (GlobalGroupStatus.ACTIVE.equals(request.getModifiedAttributes().get(GlobalGroup.STATUS))) {
				activateGlobalGroupHierarchyWithoutUserValidation(systemWideGroup.getCode(), systemWideGroup.getCollections());
			} else if (request.getNewCollections() != null) {
				activateGlobalGroupHierarchyWithoutUserValidation(systemWideGroup.getCode(), request.getNewCollections());
			}
			if (GlobalGroupStatus.INACTIVE.equals(request.getModifiedAttributes().get(GlobalGroup.STATUS))) {
				deactivateGlobalGroupHierarchyWithoutUserValidation(systemWideGroup.getCode(), systemWideGroup.getCollections());
			}
		}
		return response;
	}

	private void deactivateGlobalGroupHierarchyWithoutUserValidation(String groupCode, List<String> collections) {
		if (collections != null) {
			collections.forEach(collection -> deactivateGroupHierarchy(groupCode, collection));
		}
	}

	private void activateGlobalGroupHierarchyWithoutUserValidation(String groupCode, List<String> collections) {
		if (collections != null) {
			restoreGroupHierarchyInBigVault(groupCode, collections);
		}
	}

	private void validateNewCollections(GroupAddUpdateRequest request) {
		if (request.getNewCollections() != null) {
			request.getNewCollections().forEach((collection) -> {
				if (!collectionsListManager.getCollectionsExcludingSystem().contains(collection)) {
					throw new UserServicesRuntimeException_InvalidCollectionForGroup(request.getCode(), collection);
				}
			});
		}
	}

	private void validateNewGroupMetadatas(GroupAddUpdateRequest request) {
		SystemWideGroup systemWideGroup = getNullableGroup(request.getCode());
		if (systemWideGroup == null) {
			if ((request.getNewCollections() == null || request.getNewCollections().isEmpty())) {
				throw new UserServicesRuntimeException_AtLeastOneCollectionRequired(request.getCode());
			}

			if (request.getModifiedAttributes().get(GroupAddUpdateRequest.NAME) == null) {
				throw new UserServicesRuntimeException_NameRequired(request.getCode());
			}
		} else {

			if (!request.isLdapSyncRequest()  && !systemWideGroup.isLocallyCreated()) {

				if (request.isModified(GroupAddUpdateRequest.NAME)) {
					throw new UserServicesRuntimeException
							.UserServicesRuntimeException_CannotChangeNameOfSyncedGroup(systemWideGroup.getCode());
				}

				if (request.isModified(GlobalGroup.STATUS)) {
					throw new UserServicesRuntimeException
							.UserServicesRuntimeException_CannotChangeStatusOfSyncedGroup(systemWideGroup.getCode());
				}

				if (request.isModified(Group.PARENT)) {
					throw new UserServicesRuntimeException
							.UserServicesRuntimeException_CannotChangeParentOfSyncedGroup(systemWideGroup.getCode());
				}
			}

		}

	}


	public SystemWideUserInfos getUserInfos(String username) {
		UserCredential userCredential = getUserCredential(username);
		if (userCredential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(username);
		}
		return toSystemWideUserInfos(userCredential);
	}


	private SystemWideUserInfos toSystemWideUserInfos(UserCredential credential) {
		List<String> collections = new ArrayList<>();
		Map<String, UserCredentialStatus> statuses = new HashMap<>();

		Map<String, List<String>> groupIds = new HashMap<>();
		Map<String, List<String>> groupCodes = new HashMap<>();

		String title = credential.getTitle();
		for (String collection : modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem()) {
			User userInCollection = existingUserOrNull(credential.getUsername(), collection);
			if (userInCollection != null) {
				title = userInCollection.getTitle();
				collections.add(collection);
				statuses.put(collection, userInCollection.getStatus());

				if (userInCollection.getUserGroups() != null && !userInCollection.getUserGroups().isEmpty()) {
					groupIds.put(collection, Collections.unmodifiableList(userInCollection.getUserGroups()));
					groupCodes.put(collection, userInCollection.getUserGroups().stream()
							.map(groupId -> recordServices.getDocumentById(groupId).<String>get(Schemas.CODE))
							.collect(toList()));
				}
			}
		}

		SystemWideUserInfos.SystemWideUserInfosBuilder infos = SystemWideUserInfos.builder()
				.userCredentialId(credential.getId())
				.username(credential.getUsername())
				.firstName(credential.getFirstName())
				.lastName(credential.getLastName())
				.email(credential.getEmail())
				.groupIds(groupIds)
				.groupCodes(groupCodes)
				.title(title)
				.serviceKey(credential.getServiceKey())
				.systemAdmin(credential.isSystemAdmin())
				.statuses(statuses)
				.collections(collections)
				.dn(credential.getDn())

				.accessTokens(credential.getAccessTokens());

		try {
			infos.personalEmails(credential.getPersonalEmails());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.syncMode(credential.getSyncMode());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.phone(credential.getPhone());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.fax(credential.getFax());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.jobTitle(credential.getJobTitle());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.address(credential.getAddress());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.doNotReceiveEmails(credential.isNotReceivingEmails());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.agentStatus(credential.getAgentStatus());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.hasReadLastAlert(credential.hasReadLastAlert());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.enableFacetsApplyButton(credential.isApplyFacetsEnabled());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.hasAgreedToPrivacyPolicy(credential.hasAgreedToPrivacyPolicy());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}


		try {
			infos.electronicSignature(credential.getElectronicSignature());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.electronicInitials(credential.getElectronicInitials());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.azureUsername(credential.getAzureUsername());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		return infos.build();
	}

	public UserCredential getUser(String username) {
		UserCredential credential = getUserCredential(username);
		if (credential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(username);
		}
		return credential;
	}

	public UserCredential getUserConfigs(String username) {
		UserCredential credential = getUserCredential(username);
		if (credential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(username);
		}
		return credential;
	}

	public User getUserInCollection(String username, String collection) {
		User user = getUserRecordInCollection(username, collection);

		if (user == null) {

			UserCredential userCredential = getUser(username);
			// Case insensitive
			username = userCredential.getUsername();
			if (!userCredential.getCollections().contains(collection)) {
				throw new UserServicesRuntimeException_UserIsNotInCollection(username, collection);
			}
			user = getUserRecordInCollection(username, collection);
		}

		return user;
	}

	public User getUserRecordInCollection(String username, String collection) {
		return User.wrapNullable(recordServices.getRecordByMetadata(usernameMetadata(collection), username),
				schemaTypes(collection), rolesManager.getCollectionRoles(collection, modelLayerFactory));
	}


	public SystemWideUserInfos getUserByAzureUsername(String azureUsername) {
		//TODO Improve performance
		try {
			return streamUserInfos().filter(u -> azureUsername.equals(u.getAzureUsername())).findFirst().get();
		} catch (NoSuchElementException e) {
			throw new UserServicesRuntimeException_NoSuchUser(azureUsername);
		}
	}


	public SystemWideGroup getNullableGroup(String groupCode) {
		List<Group> groupsInCollection = new ArrayList<>();
		List<String> wideGroupCollections = new ArrayList<>();
		GlobalGroupStatus groupStatus = GlobalGroupStatus.INACTIVE;
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			Group groupInCollection = getGroupInCollection(groupCode, collection);
			if (groupInCollection != null) {
				groupsInCollection.add(groupInCollection);
				wideGroupCollections.add(collection);
				if (GlobalGroupStatus.ACTIVE.equals(groupInCollection.getStatus())) {
					groupStatus = GlobalGroupStatus.ACTIVE;
				}
			}
		}
		return !groupsInCollection.isEmpty() ? build(groupsInCollection.get(0), wideGroupCollections, groupStatus) : null;
	}

	private SystemWideGroup getGroup(GroupAddUpdateRequest request) {
		List<Group> groupsInCollection = new ArrayList<>();
		List<String> wideGroupCollections = new ArrayList<>();
		Group groupInCollection;
		GlobalGroupStatus groupStatus = GlobalGroupStatus.INACTIVE;
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			groupInCollection = getGroupInCollection(request.getCode(), collection);
			if (groupInCollection != null) {
				groupsInCollection.add(groupInCollection);
				wideGroupCollections.add(collection);
				if (GlobalGroupStatus.ACTIVE.equals(groupInCollection.getStatus())) {
					groupStatus = GlobalGroupStatus.ACTIVE;
				}
			}
		}
		return !groupsInCollection.isEmpty() ? build(groupsInCollection.get(0), wideGroupCollections, groupStatus) : build(request);
	}

	private SystemWideGroup build(Group group, List<String> wideGroupCollections, GlobalGroupStatus groupStatus) {
		String parentCode = null;
		if (group.getParent() != null) {
			parentCode = recordServices.getDocumentById(group.getParent()).get(Schemas.CODE);
		}

		List<String> ancestorsCodes = new ArrayList<>();
		List<String> ancestorsIds = group.getAncestors();
		if (ancestorsIds != null) {
			for (String ancestorId : ancestorsIds) {
				ancestorsCodes.add(recordServices.getDocumentById(ancestorId).get(Schemas.CODE));
			}
		}

		return SystemWideGroup.builder()
				.code(group.getCode())
				.name(group.getTitle())
				.collections(wideGroupCollections)
				.parent(parentCode)
				.groupStatus(groupStatus)
				.hierarchy(group.getHierarchy())
				.logicallyDeletedStatus(group.getLogicallyDeletedStatus())
				.caption(group.getCaption())
				.ancestors(ancestorsCodes)
				.locallyCreated(group.isLocallyCreated())
				.build();
	}


	private SystemWideGroup build(GroupAddUpdateRequest request) {
		String parentCode = (String) request.getModifiedAttributes().get(GroupAddUpdateRequest.PARENT);
		GlobalGroupStatus status = (GlobalGroupStatus) request.getModifiedAttributes().get(GroupAddUpdateRequest.STATUS);
		return SystemWideGroup.builder()
				.code(request.getCode())
				.name((String) request.getModifiedAttributes().get(GroupAddUpdateRequest.NAME))
				.collections(request.getNewCollections())
				.groupStatus(status != null ? status : GlobalGroupStatus.ACTIVE)
				.hierarchy((String) request.getModifiedAttributes().get(GroupAddUpdateRequest.HIERARCHY))
				.parent(parentCode)
				.build();
	}


	public GroupAddUpdateRequest request(String groupCode) {
		return new GroupAddUpdateRequest(groupCode);
	}

	public SystemWideGroup getGroup(String groupCode) {
		SystemWideGroup group = getNullableGroup(groupCode);
		if (group == null) {
			throw new UserServicesRuntimeException_NoSuchGroup(groupCode);
		}
		return group;
	}

	public Group getGroupInCollection(String groupCode, String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		return schemas.getGroupWithCode(groupCode);
	}


	private void restoreGroupHierarchyInBigVault(String globalGroupCode, List<String> collections) {
		for (String collection : collections) {
			List<Group> childrenOfGroupInCollection = getInactiveChildrenOfGroupInCollection(globalGroupCode, collection);
			childrenOfGroupInCollection.stream().forEach(childGroup -> restoreGroupHierarchyInBigVault(childGroup, collection));
		}
	}

	private void restoreGroupHierarchyInBigVault(Group group, String collection) {
		List<Group> childrenOfGroupInCollection = getInactiveChildrenOfGroupInCollection(group.getCode(), collection);
		childrenOfGroupInCollection.stream().forEach(childGroup -> restoreGroupHierarchyInBigVault(childGroup, collection));
		LOGGER.info("restoreDeletedGroup : " + group.getCode());
		recordServices.restore(group.getWrappedRecord(), User.GOD);
		try {
			recordServices.update(group.setStatus(GlobalGroupStatus.ACTIVE));
		} catch (RecordServicesException e) {
			throw new RuntimeException(e);
		}
	}

	//TODO Determine if can be deleted
	@Deprecated
	public void logicallyRemoveGroupHierarchy(String username, SystemWideGroup globalGroup) {
		executeGroupRequest(globalGroup.getCode(), req -> req.setStatusInAllCollections(GlobalGroupStatus.INACTIVE));
	}

	public String giveNewServiceKey(String username) {
		String nextToken = secondaryUniqueIdGenerator.next();
		execute(username, (req) -> req.setServiceKey(nextToken));
		return nextToken;
	}

	public boolean canAddOrModifyUserAndGroup() {
		return !(ldapConfigurationManager.isLDAPAuthentication() && ldapConfigurationManager.idUsersSynchActivated());
	}

	public boolean canModifyPassword(UserCredential userInEdition, UserCredential currentUser) {
		return (userInEdition.getUsername().equals("admin") && currentUser.getUsername().equals("admin"))
			   || !ldapConfigurationManager.isLDAPAuthentication();
	}

	public boolean isLDAPAuthentication() {
		return ldapConfigurationManager.isLDAPAuthentication();
	}

	String getGroupIdInCollection(String groupCode, String collection) {
		Group group = getGroupInCollection(groupCode, collection);
		return group == null ? null : group.getId();
	}

	private void sync(GroupAddUpdateRequest request, GroupAddUpdateResponse response) {
		SystemWideGroup systemWideGroup = getGroup(request);
		Set<String> groupCollections = new HashSet<>();
		if (systemWideGroup != null && systemWideGroup.getCollections() != null) {
			groupCollections.addAll(systemWideGroup.getCollections());
		}
		if (request.getNewCollections() != null) {
			groupCollections.addAll(request.getNewCollections());
		}
		Transaction transaction;
		String parent = (String) request.getModifiedAttributes().get(GroupAddUpdateRequest.PARENT);
		syncParentHierarchy(parent, systemWideGroup, groupCollections);

		validateGroupRemovable(request);

		for (String collection : groupCollections) {
			transaction = new Transaction();
			if (request.getRemovedCollections() != null && request.getRemovedCollections().contains(collection)) {

				removeChildren(systemWideGroup.getCode(), collection, response);
				removeGroupFrom(request.getCode(), collection, response);
			} else if (canSyncGroup(request, collection)) {
				sync(request, collection, transaction, systemWideGroup);
			}
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
			}
		}
	}

	private void validateGroupRemovable(GroupAddUpdateRequest request) {
		if (request.getRemovedCollections() != null && !request.isLdapSyncRequest()) {

			List<String> syncCollections = ldapConfigurationManager
					.getLDAPUserSyncConfiguration(false).getSelectedCollectionsCodes();

			for(String collection : request.getRemovedCollections()) {
				if (syncCollections.contains(collection)) {
					throw new UserServicesRuntimeException_CannotRemoveSyncedGroupFromSyncedCollection(request.getCode(), collection);
				};

			}

		}
	}

	private void syncParentHierarchy(String parent, SystemWideGroup group, Set<String> groupCollections) {
		if (parent == null) {
			parent = group.getParent();
		}

		List<String> parentGroupCollections = getParentGroupCollections(new ArrayList<>(groupCollections), parent);
		if (!parentGroupCollections.isEmpty()) {
			SystemWideGroup parentGroup = getGroup(parent);
			Transaction transaction;
			for (String collection : parentGroupCollections) {
				transaction = new Transaction();
				sync(parentGroup, collection, transaction);
				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
				}
			}
			syncParentHierarchy(null, parentGroup, groupCollections);
		}
	}

	private boolean canSyncGroup(GroupAddUpdateRequest request, String collection) {
		if (request.getMarkedForDeletionInCollections() != null) {
			return !request.getMarkedForDeletionInCollections().contains(collection);
		}
		return !request.isMarkedForDeletionInAllCollections() && request.getMarkedForDeletionInCollections() == null;
	}

	private List<String> getParentGroupCollections(List<String> childGroupCollections, String parent) {
		List<String> newParentGroupCollections = new ArrayList<>();
		if (parent != null) {
			List<String> parentGroupCollections = getGroup(parent).getCollections();
			for (String collection : childGroupCollections) {
				if (!parentGroupCollections.contains(collection)) {
					newParentGroupCollections.add(collection);
				}
			}
		}
		return newParentGroupCollections;
	}

	private void sync(GroupAddUpdateRequest request, String collection,
					  Transaction transaction, SystemWideGroup group) {
		String groupCode = request.getCode();
		Group groupInCollection = getGroupInCollection(groupCode, collection);
		if (groupInCollection == null) {
			groupInCollection = newGroupInCollection(collection);
		}

		groupInCollection.set(Group.CODE, groupCode);

		Map<String, Object> modifiedAttributes = request.getModifiedAttributes();

		if (modifiedAttributes.containsKey(GroupAddUpdateRequest.PARENT)) {
			String parentCode = (String) modifiedAttributes.get(GroupAddUpdateRequest.PARENT);
			if (parentCode != null) {
				String parentId = getGroupIdInCollection(parentCode, collection);
				groupInCollection.setParent(parentId);
			} else {
				groupInCollection.setParent(null);
			}
		} else if (group.getParent() != null) {
			String parentId = getGroupIdInCollection(group.getParent(), collection);
			groupInCollection.setParent(parentId);
		}

		groupInCollection.set(Group.STATUS, GlobalGroupStatus.ACTIVE);
		groupInCollection.set(Schemas.LOGICALLY_DELETED_STATUS, false);
		groupInCollection.set(Group.LOCALLY_CREATED, !request.isLdapSyncRequest());
		if ((modifiedAttributes.get(GroupAddUpdateRequest.NAME) != null)) {
			groupInCollection.setTitle((String) modifiedAttributes.get(GroupAddUpdateRequest.NAME));
		} else {
			groupInCollection.setTitle(group.getName());
		}
		if (groupInCollection.isDirty()) {
			transaction.add(groupInCollection.getWrappedRecord());
		}
	}

	private void sync(SystemWideGroup group, String collection, Transaction transaction) {
		String groupCode = group.getCode();
		Group groupInCollection = getGroupInCollection(groupCode, collection);
		if (groupInCollection == null) {
			groupInCollection = newGroupInCollection(collection);
		}
		groupInCollection.set(Group.CODE, groupCode);

		if (group.getParent() != null) {
			String parentId = null;
			String parentcode = group.getParent();
			if (parentcode != null) {
				parentId = getGroupIdInCollection(parentcode, collection);
			}
			groupInCollection.setParent(parentId);
		}
		groupInCollection.set(Group.STATUS, GlobalGroupStatus.ACTIVE);
		groupInCollection.set(Group.LOCALLY_CREATED, group.getLocallyCreated());
		groupInCollection.set(LOGICALLY_DELETED_STATUS, group.getLogicallyDeletedStatus());
		if (!collection.equals(SYSTEM_COLLECTION)) {
			groupInCollection.set(Group.STATUS, group.getStatus(collection));
		}
		groupInCollection.setTitle(group.getName());
		if (groupInCollection.isDirty()) {
			transaction.add(groupInCollection.getWrappedRecord());
		}
	}

	private String getGroupParentId(SystemWideGroup group, String collection) {
		String parentId = null;
		if (group.getParent() != null) {
			parentId = getGroupIdInCollection(group.getParent(), collection);
			if (parentId == null) {
				throw new ImpossibleRuntimeException("No group with code '" + parentId + "' in collection '" + collection + "'");
			}
		}
		return parentId;
	}

	private OngoingLogicalSearchCondition fromGroupsIn(String collection) {
		return from(groupSchema(collection));
	}

	private MetadataSchemaTypes schemaTypes(String collection) {
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	private MetadataSchema userSchema(String collection) {
		MetadataSchemaTypes schemaTypes = schemaTypes(collection);
		return schemaTypes.getDefaultSchema(User.SCHEMA_TYPE);
	}

	private Metadata usernameMetadata(String collection) {
		return userSchema(collection).getMetadata(User.USERNAME);
	}

	private MetadataSchema groupSchema(String collection) {
		return schemaTypes(collection).getDefaultSchema(Group.SCHEMA_TYPE);
	}

	private Metadata groupCodeMetadata(String collection) {
		return groupSchema(collection).getMetadata(Group.CODE);
	}

	private Metadata groupParentMetadata(String collection) {
		return groupSchema(collection).getMetadata(Group.PARENT);
	}

	private Group newGroupInCollection(String collection) {
		Record record = recordServices.newRecordWithSchema(groupSchema(collection));
		return new Group(record, schemaTypes(collection));
	}

	public List<Group> getAllGroupsInCollections(String collection) {
		MetadataSchemaTypes collectionTypes = metadataSchemasManager.getSchemaTypes(collection);
		LogicalSearchQuery query = new LogicalSearchQuery(allGroups(collectionTypes).returnAll());
		query.filteredByStatus(StatusFilter.ACTIVES);
		return Group.wrap(searchServices.search(query), collectionTypes);
	}

	public List<Group> getCollectionGroups(String collection) {
		MetadataSchemaTypes collectionTypes = metadataSchemasManager.getSchemaTypes(collection);
		LogicalSearchQuery query = new LogicalSearchQuery(allGroupsWhereGlobalGroupFlag(collectionTypes).isFalseOrNull());
		query.filteredByStatus(StatusFilter.ACTIVES);
		return Group.wrap(searchServices.search(query), collectionTypes);
	}

	private OngoingLogicalSearchCondition allGroups(MetadataSchemaTypes types) {
		MetadataSchema groupSchema = types.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
		return from(groupSchema);
	}

	private OngoingLogicalSearchConditionWithDataStoreFields allGroupsWhereGlobalGroupFlag(MetadataSchemaTypes types) {
		MetadataSchema groupSchema = types.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
		Metadata isGlobalGroup = groupSchema.getMetadata(Group.IS_GLOBAL);
		return from(groupSchema).where(isGlobalGroup);
	}

	private void permissionValidateCredentialOnGroup(UserCredential userCredential) {
		if (!has(userCredential).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS_ACTIVATION)) {
			throw new UserServicesRuntimeException_UserPermissionDeniedToDelete(userCredential.getUsername());
		}
	}

	public String getToken(String serviceKey, String username, String password) {
		ReadableDuration tokenDuration = modelLayerConfiguration.getTokenDuration();
		return getToken(serviceKey, username, password, tokenDuration);
	}

	public String getToken(String serviceKey, String username, String password, ReadableDuration duration) {
		if (authenticationService.authenticate(username, password) && serviceKey.equals(getUser(username).getServiceKey())) {
			String token = generateToken(username, duration);
			return token;
		} else {
			throw new UserServicesRuntimeException_InvalidUserNameOrPassword(username);
		}
	}

	public String getToken(String serviceKey, String token) {
		//TODO Refact!
		if (getServiceKeyByToken(token) != null) {
			removeToken(token);
			UserCredential credential = modelLayerFactory.getUserCredentialServiceKeyCacheHookRetriever().getUserByServiceKey(serviceKey);
			String newToken = generateToken(credential.getUsername());
			return newToken;
		} else {
			throw new UserServicesRuntimeException_InvalidToken();
		}
	}

	public String generateToken(String username) {
		//TODO Refact!
		return generateToken(username, modelLayerConfiguration.getTokenDuration());
	}

	public String generateToken(String username, ReadableDuration duration) {
		//TODO Refact!
		String token = secondaryUniqueIdGenerator.next();
		LocalDateTime expiry = TimeProvider.getLocalDateTime().plus(duration);
		execute(username, (req) -> req.addAccessToken(token, expiry));
		return token;
	}

	public String generateToken(String username, String unitTime, int duration) {
		//TODO Refact!
		String token = secondaryUniqueIdGenerator.next();
		LocalDateTime expiry = unitTime.equals("hours") ?
							   TimeProvider.getLocalDateTime().plusHours(duration) :
							   TimeProvider.getLocalDateTime().plusDays(duration);
		execute(username, (req) -> req.addAccessToken(token, expiry));

		return token;
	}

	public String getTokenUser(String serviceKey, String token) {
		String retrivedServiceKey = getServiceKeyByToken(token);
		if (retrivedServiceKey == null || !retrivedServiceKey.equals(serviceKey)) {
			throw new UserServicesRuntimeException_InvalidToken();
		}
		UserCredential credential = modelLayerFactory.getUserCredentialTokenCacheHookRetriever().getUserByToken(token);
		return credential == null ? null :credential.getUsername();
	}

	public String getServiceKeyByToken(String token) {
		UserCredential credential = modelLayerFactory.getUserCredentialTokenCacheHookRetriever().getUserByToken(token);
		String retrivedServiceKey = credential == null ? null : credential.getServiceKey();
		if (retrivedServiceKey != null) {
			return retrivedServiceKey;
		} else {
			throw new UserServicesRuntimeException_InvalidToken();
		}
	}

	public void removeToken(String token) {
		UserCredential credential = modelLayerFactory.getUserCredentialTokenCacheHookRetriever().getUserByToken(token);
		if (credential != null) {
			execute(credential.getUsername(), req -> req.removeAccessToken(token));
		}
	}

	public String getUserCredentialByServiceKey(String serviceKey) {
		UserCredential userCredential = modelLayerFactory.getUserCredentialServiceKeyCacheHookRetriever().getUserByServiceKey(serviceKey);
		return userCredential == null ? null : userCredential.getUsername();
	}

	public UserCredential getUserCredential(String username) {
		if (username == null) {
			return null;
		}

		Record record = modelLayerFactory.newRecordServices()
				.getRecordByMetadata(schemas.credentialUsername(), username);

		if (record == null) {

			String cleanedUsername = cleanUsername(username);
			if (!cleanedUsername.equals(username)) {
				record = modelLayerFactory.newRecordServices()
						.getRecordByMetadata(schemas.credentialUsername(), cleanUsername(username));
			}
		}
		return record != null ? schemas.wrapCredential(record) : null;
	}

	public SystemWideUserInfos getUserCredentialByDN(String dn) {
		Record record = recordServices.getRecordByMetadata(schemas.credentialDN(), dn);
		return record == null ? null : getUserInfos(schemas.wrapUserCredential(record).getUsername());
	}

	private List<Group> getChildrenOfGroupInCollection(String groupParentCode, String collection) {
		List<Group> groups = new ArrayList<>();
		String parentId = getGroupIdInCollection(groupParentCode, collection);
		if (parentId != null) {
			LogicalSearchCondition condition = from(groupSchema(collection))
					.where(groupParentMetadata(collection))
					.is(parentId).andWhere(LOGICALLY_DELETED_STATUS).isFalseOrNull();
			LogicalSearchQuery query = new LogicalSearchQuery().setCondition(condition);
			for (Record record : searchServices.search(query)) {
				groups.add(wrapNullable(record, schemaTypes(collection)));
			}
		}
		return groups;
	}

	private List<Group> getInactiveChildrenOfGroupInCollection(String groupParentCode, String collection) {
		List<Group> groups = new ArrayList<>();
		String parentId = getGroupIdInCollection(groupParentCode, collection);
		if (parentId != null) {
			LogicalSearchCondition condition = from(groupSchema(collection))
					.where(groupParentMetadata(collection))
					.is(parentId).andWhere(LOGICALLY_DELETED_STATUS).isTrue();
			LogicalSearchQuery query = new LogicalSearchQuery().setCondition(condition);
			for (Record record : searchServices.search(query)) {
				groups.add(wrapNullable(record, schemaTypes(collection)));
			}
		}
		return groups;
	}


	public List<User> getAllUsersInCollection(String collection) {
		return streamUser(collection).collect(toList());
	}

	public boolean isAuthenticated(String userServiceKey, String userToken) {
		if (userToken == null || userServiceKey == null) {
			return false;
		} else {
			try {
				String tokenServiceKey = getServiceKeyByToken(userToken);
				return tokenServiceKey != null && tokenServiceKey.equals(userServiceKey);
			} catch (UserServicesRuntimeException_InvalidToken e) {
				return false;
			}
		}
	}


	private void deleteUser(com.constellio.model.services.users.UserAddUpdateRequest request,
							UserAddUpdateResponse response) {
		String username = request.getUsername();
		SystemWideUserInfos userInfos = systemWideUserInfosOrNull(username);
		if (userInfos != null) {
			boolean removedEverywhere = true;
			UserCredential userCredential = existingUserCredentialOrNull(username);
			for (String collection : userInfos.getCollections()) {
				verifyUserIsSyncedInCollectionSynced(userCredential,request.isLdapSyncRequest(),collection);
				removedEverywhere &= removeUserFrom(username, collection, response);
			}

			if (removedEverywhere) {
				if (userCredential != null) {
					recordServices.physicallyDeleteNoMatterTheStatus(userCredential, User.GOD, new RecordPhysicalDeleteOptions());
				}
			}
		}
	}

	private void deleteGroup(String groupCode, GroupAddUpdateResponse response) {
		SystemWideGroup systemWideGroup = getNullableGroup(groupCode);
		if (systemWideGroup != null) {
			for (String collection : systemWideGroup.getCollections()) {
				removeChildren(groupCode, collection, response);
				removeGroupFrom(groupCode, collection, response);
			}
		}
	}

	private void deleteGroupFromCollection(String groupCode, String collection,
										   GroupAddUpdateResponse response) {
		SystemWideGroup systemWideGroup = getNullableGroup(groupCode);
		if (systemWideGroup != null) {
			removeChildren(groupCode, collection, response);
			removeGroupFrom(groupCode, collection, response);
		}
	}

	private void removeChildren(String group, String collection, GroupAddUpdateResponse response) {
		for (Group child : getChildrenOfGroupInCollection(group, collection)) {
			removeChildren(child.getCode(), collection, response);
			removeGroupFrom(child.getCode(), collection, response);
		}
	}

	public List<User> getAllUsersInGroup(Group group, boolean includeGroupInheritance,
										 boolean onlyActiveUsersAndGroups) {

		//TODO Refact : Kept method from old services, make sure it is tested
		List<User> userRecords = new ArrayList<>();
		Set<String> usernames = new HashSet<>();

		getUsersRecordsInGroup(group, userRecords, usernames, includeGroupInheritance, onlyActiveUsersAndGroups);

		return userRecords;
	}

	private void getUsersRecordsInGroup(Group group, List<User> returnedUserRecords,
										Set<String> usernamesOfReturnedUsers,
										boolean includeGroupInheritance, boolean onlyActiveUsersAndGroups) {

		UserServices userServices = modelLayerFactory.newUserServices();

		boolean includedGroup = true;
		if (onlyActiveUsersAndGroups) {
			try {
				SystemWideGroup globalGroup = userServices.getGroup(group.getCode());
				includedGroup = globalGroup.getStatus() == GlobalGroupStatus.ACTIVE;
			} catch (UserServicesRuntimeException_NoSuchGroup e) {
				LOGGER.info("No such global group with code '" + group.getCode() + "', group is considered active");
			}
		}

		if (includedGroup) {

			SchemasRecordsServices schemas = new SchemasRecordsServices(group.getCollection(), modelLayerFactory);
			if (includeGroupInheritance) {
				GroupAuthorizationsInheritance inheritance =
						modelLayerFactory.getSystemConfigurationsManager().getValue(GROUP_AUTHORIZATIONS_INHERITANCE);

				if (inheritance == GroupAuthorizationsInheritance.FROM_CHILD_TO_PARENT) {

					if (group.getParent() != null) {
						Group parentGroup = schemas.getGroup(group.getParent());
						getUsersRecordsInGroup(parentGroup, returnedUserRecords, usernamesOfReturnedUsers, true,
								onlyActiveUsersAndGroups);
					}
				} else {
					LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.group.schemaType())
							.where(schemas.group.parent()).isEqualTo(group.getId()));
					for (Group aGroup : schemas.searchGroups(query)) {
						if (group.getId().equals(aGroup.getParent())) {
							getUsersRecordsInGroup(aGroup, returnedUserRecords, usernamesOfReturnedUsers, true,
									onlyActiveUsersAndGroups);
						}
					}
				}
			}

			LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.user.schemaType())
					.where(schemas.user.groups()).isEqualTo(group.getId()));

			for (User aUser : schemas.searchUsers(query)) {
				if (!usernamesOfReturnedUsers.contains(aUser.getId()) && aUser.getUserGroups().contains(group.getId())) {

					boolean includedUser;
					if (onlyActiveUsersAndGroups) {
						UserCredential userCredential = userServices.getUserCredential(aUser.getUsername());
						includedUser = userCredential.getStatus() == UserCredentialStatus.ACTIVE;

					} else {
						includedUser = true;
					}

					if (includedUser) {
						usernamesOfReturnedUsers.add(aUser.getUsername());
						returnedUserRecords.add(aUser);
					}

				}
			}
		}
	}

	public boolean isGroupAndAllHisAncestorsActive(String aGroup, String collection) {
		//TODO Refact : With new services, a child group is always INACTIVE if it's parent is inactive, so replace with :
		//getGroup(aGroup).getStatus(collection) == ACTIVE;

		try {
			Record record = recordServices.getDocumentById(aGroup);
			Group group = wrapNullable(record,
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection()));
			return group.getStatus() == GlobalGroupStatus.ACTIVE;

		} catch (RecordServicesRuntimeException.NoSuchRecordWithId ignored) {
			return getNullableGroup(aGroup).getStatus(collection) == GlobalGroupStatus.ACTIVE;
		}

	}


	public com.constellio.model.services.users.UserAddUpdateRequest addUpdate(String username) {
		UserCredential userCredential = getUserCredential(username);
		com.constellio.model.services.users.UserAddUpdateRequest request;
		if (userCredential == null) {
			request = new com.constellio.model.services.users.UserAddUpdateRequest(cleanUsername(username), Collections.emptyList(), Collections.emptyList());
		} else {
			request = new com.constellio.model.services.users.UserAddUpdateRequest(username, userCredential.getCollections(), userCredential.getGlobalGroups());

		}
		return request;
	}

	public void reShowPrivacyPolicyToUser() {
		//TODO Refact : Kept method from old services, make sure it is tested
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(systemSchemas.credentialSchemaType())
				.where(systemSchemas.credentialSchemaType().getAllMetadatas().getMetadataWithLocalCode(UserCredential.HAS_AGREED_TO_PRIVACY_POLICY)).isTrue());

		new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, query.getCondition())
				.setOptions(RecordUpdateOptions
						.validationExceptionSafeOptions())
				.modifyingRecordsWithImpactHandling(new RecordScript() {

					@Override
					public void modifyRecord(Record record) {
						UserCredential userCredential = systemSchemas.wrapUserCredential(record);
						userCredential.setAgreedPrivacyPolicy(false);
					}
				});
	}

	public void resetHasReadLastAlertMetadataOnUsers() {
		//TODO Refact : Kept method from old services, make sure it is tested
		SchemasRecordsServices systemSchemas = new SchemasRecordsServices(com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(systemSchemas.credentialSchemaType())
				.where(systemSchemas.credentialSchemaType()
						.getAllMetadatas().getMetadataWithLocalCode(UserCredential.HAS_READ_LAST_ALERT)).isTrue());
		new ConditionnedActionExecutorInBatchBuilder(modelLayerFactory, query.getCondition())
				.setOptions(RecordUpdateOptions
						.validationExceptionSafeOptions())
				.modifyingRecordsWithImpactHandling(new RecordScript() {

					@Override
					public void modifyRecord(Record record) {
						UserCredential userCredential = systemSchemas.wrapUserCredential(record);
						userCredential.setReadLastAlert(false);
					}
				});
	}

	public void removeTimedOutTokens() {
		//TODO Refact : Kept method from old services, make sure it is tested
		LocalDateTime now = TimeProvider.getLocalDateTime();
		Transaction transaction = new Transaction();
		for (Record record : searchServices.search(getUserCredentialsWithExpiredTokensQuery(now))) {
			UserCredential credential = schemas.wrapCredential(record);
			Map<String, LocalDateTime> validTokens = new HashMap<>();
			for (Entry<String, LocalDateTime> token : credential.getAccessTokens().entrySet()) {
				LocalDateTime expiration = token.getValue();
				if (expiration.isAfter(now)) {
					validTokens.put(token.getKey(), token.getValue());
				}
			}
			transaction.add(credential.setAccessTokens(validTokens));
		}
		try {
			modelLayerFactory.newRecordServices().execute(transaction);
		} catch (RecordServicesException e) {
			throw new UserCredentialsManagerRuntimeException_CannotExecuteTransaction(e);
		}
	}

	private LogicalSearchQuery getUserCredentialsWithExpiredTokensQuery(LocalDateTime now) {
		return new LogicalSearchQuery(
				from(schemas.credentialSchemaType()).where(schemas.credentialTokenExpirations()).isLessOrEqualThan(now));
	}

	public List<SystemWideGroup> getActiveGroups() {
		return streamGroupInfos().filter(g -> g.getGroupStatus() == GlobalGroupStatus.ACTIVE).collect(toList());
	}

	public List<SystemWideGroup> getAllGroups() {
		return streamGroupInfos().collect(toList());
	}


	public Stream<SystemWideGroup> streamGroupInfos() {
		//TODO Refact Francis : Improve performance!
		Set<String> allGroupCodes = new HashSet<>();

		for (String collection : modelLayerFactory.getCollectionsListManager().getCollections()) {
			streamGroup(collection).forEach(g -> allGroupCodes.add(g.getCode()));
		}

		List<String> allGroupCodesSorted = new ArrayList<>(allGroupCodes);
		Collections.sort(allGroupCodesSorted);

		return allGroupCodesSorted.stream().map(g -> getGroup(g));
	}


	public Stream<SystemWideGroup> streamGroupInfos(String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.group.schemaType()).returnAll());
		return searchServices.stream(query).map(u -> getGroup(schemas.wrapGroup(u).getCode()));
	}

	public Stream<Group> streamGroup(String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.group.schemaType()).returnAll());
		return searchServices.stream(query).map(schemas::wrapGroup);
	}

	public Stream<SystemWideUserInfos> streamUserInfos() {
		SchemasRecordsServices schemas = new SchemasRecordsServices(SYSTEM_COLLECTION, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.credentialSchemaType()).returnAll());
		return searchServices.stream(query).map(u -> getUserInfos(schemas.wrapUserCredential(u).getUsername()));
	}

	public Stream<SystemWideUserInfos> streamUserInfos(String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.user.schemaType()).returnAll());
		return searchServices.stream(query).map(u -> getUserInfos(schemas.wrapUser(u).getUsername()));

	}

	public Stream<User> streamUser(String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		LogicalSearchQuery query = new LogicalSearchQuery(from(schemas.user.schemaType()).returnAll());
		return searchServices.stream(query).map(schemas::wrapUser);
	}


	//----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----
	// Entering GARBAGE AREA : Following methods should be deleted
	//----- ----- ----- ----- ----- ----- ----- ----- ----- ----- -----

	//TODO Users should be able to have different groups from a collection to an other, this service break this
	@Deprecated
	public List<SystemWideUserInfos> getGlobalGroupActifUsers(String collection, String groupCode) {
		return streamUserInfos().filter(u -> u.getGroupCodes(collection).contains(groupCode)).collect(Collectors.toList());
	}

	@Deprecated
	public List<SystemWideUserInfos> getGlobalGroupActifUsers(String groupCode) {
		return streamUserInfos().filter(u -> u.isInGroupInAnyCollection(groupCode)).collect(Collectors.toList());
	}

	//Use execute instead
	@Deprecated
	public void updateAzureUsername(String username, String azureUser) {
		execute(username, (req) -> req.setAzureUsername(azureUser));
	}


	@Deprecated
	public void removeUserFromCollection(String username, String collection) {
		execute(username, (req) -> req.removeFromCollection(collection));

	}

	@Deprecated
	void activateGlobalGroupHierarchy(UserCredential userCredential, SystemWideGroup globalGroup) {
		permissionValidateCredentialOnGroup(userCredential);
		//activateGlobalGroupHierarchyWithoutUserValidation(globalGroup);
	}

	@Deprecated
	void removeUserCredentialAndUser(UserCredential userCredential) {
		execute(userCredential.getUsername(), (req) -> req.setStatusForAllCollections(DISABLED));
	}

	@Deprecated
	public void removeUserCredentialAndUser(SystemWideUserInfos userCredential) {
		execute(addUpdate(userCredential.getUsername()).setStatusForAllCollections(DISABLED));
	}

	@Deprecated
	public void setUserCredentialAndUserStatusPendingApproval(String username) {
		execute(addUpdate(username).setStatusForAllCollections(PENDING));
	}

	@Deprecated
	public void suspendUserCredentialAndUser(String username) {
		execute(addUpdate(username).setStatusForAllCollections(SUSPENDED));
	}

	@Deprecated
	public void activeUserCredentialAndUser(String username) {
		execute(addUpdate(username).setStatusForAllCollections(ACTIVE));
	}

	@Deprecated
	public List<SystemWideUserInfos> getActiveUserCredentials() {
		return streamUserInfos().filter(SystemWideUserInfos::isActiveInAnyCollection).collect(Collectors.toList());
	}

	@Deprecated
	public List<SystemWideUserInfos> getAllUserCredentials() {
		return streamUserInfos().collect(Collectors.toList());
	}

	@Deprecated
	public List<SystemWideGroup> getAllGlobalGroups() {
		return streamGroupInfos().collect(toList());
	}


	public CredentialUserPermissionChecker has(User user) {
		return has(user.getUsername());
	}

	@Deprecated
	public CredentialUserPermissionChecker has(UserCredential userCredential) {
		return has(userCredential.getUsername());
	}

	@Deprecated
	public CredentialUserPermissionChecker has(SystemWideUserInfos userCredential) {
		return has(userCredential.getUsername());
	}


	public CredentialUserPermissionChecker has(String username) {
		List<User> users = new ArrayList<>();
		SystemWideUserInfos userInfos = getUserInfos(username);
		for (String collection : userInfos.getCollections()) {
			users.add(getUserInCollection(username, collection));
		}
		return new CredentialUserPermissionChecker(users);
	}


	//User execute instead
	@Deprecated
	public void givenSystemAdminPermissionsToUser(SystemWideUserInfos user) {
		execute(user.getUsername(), com.constellio.model.services.users.UserAddUpdateRequest::setSystemAdminEnabled);
	}


	//Use execute instead
	@Deprecated
	public void removeUserFromGlobalGroup(String username, String globalGroupCode) {
		execute(username, (req) -> req.removeFromGroupOfEachCollection(globalGroupCode));
	}

	LogicalSearchQuery getUserCredentialNotSynced() {
		return new LogicalSearchQuery(from(schemas.credentialSchemaType()).where(schemas.syncStatus()).is(UserSyncMode.NOT_SYNCED))
				.sortAsc(Schemas.TITLE);
	}

	public List<UserCredential> getUsersNotSynced() {
		List<Record> records = searchServices.search(getUserCredentialNotSynced());
		return records.stream().map(x -> schemas.wrapCredential(x)).collect(toList());
	}
}
