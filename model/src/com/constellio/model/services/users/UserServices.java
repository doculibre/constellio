package com.constellio.model.services.users;

import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.data.utils.TimeProvider;
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
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotExcuteTransaction;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidToken;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUserNameOrPassword;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserPermissionDeniedToDelete;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.joda.time.ReadableDuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.records.wrappers.Group.wrapNullable;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_ON;
import static com.constellio.model.entities.schemas.Schemas.LOGICALLY_DELETED_STATUS;
import static com.constellio.model.entities.security.global.UserCredentialStatus.DELETED;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.GROUP_AUTHORIZATIONS_INHERITANCE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.users.UserUtils.cleanUsername;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class UserServices {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServices.class);
	public static final String ADMIN = "admin";
	private final SolrUserCredentialsManager userCredentialsManager;
	private final SolrGlobalGroupsManager globalGroupsManager;
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
		this.userCredentialsManager = modelLayerFactory.getUserCredentialsManager();
		this.globalGroupsManager = modelLayerFactory.getGlobalGroupsManager();
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

	public GlobalGroup newGlobalGroup() {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(SYSTEM_COLLECTION);
		return new GlobalGroup(recordServices.newRecordWithSchema(types.getDefaultSchema(GlobalGroup.SCHEMA_TYPE)), types);
	}

	public void addUpdateUserCredential(UserAddUpdateRequest request) {
		UserCredential userCredential = addEdit(request.getUsername())
				.setFirstName(request.getFirstName())
				.setLastName(request.getLastName())
				.setEmail(request.getEmail())
				.setPersonalEmails(request.getPersonalEmails())
				.setServiceKey(request.getServiceKey())
				.setSystemAdmin(request.getSystemAdmin())
				.setStatus(request.getStatus())
				.setCollections(request.getCollections())
				.setGlobalGroups(request.getGlobalGroups())
				.setDomain(request.getDomain())
				.setMsExchDelegateListBL(request.getMsExchangeDelegateList())
				.setDn(request.getDn());

		try {
			userCredential.setPhone(request.getPhone());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {
		}
		try {
			userCredential.setFax(request.getFax());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {
		}
		try {
			userCredential.setJobTitle(request.getJobTitle());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {
		}
		try {
			userCredential.setAddress(request.getAddress());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {
		}
		try {
			userCredential.setAgentStatus(request.getAgentStatus());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {
		}

		try {
			userCredential.setAgreedPrivacyPolicy(request.getHasAgreedToPrivacyPolicy());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setNotReceivingEmails(request.getDoNotReceiveEmails());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setApplyFacetsEnabled(request.getEnableFacetsApplyButton());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setReadLastAlert(request.getHasReadLastAlert());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setElectronicSignature(request.getElectronicSignature());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setElectronicInitials(request.getElectronicInitials());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setAzureUsername(request.getAzureUsername());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}
		try {
			userCredential.setAccessTokens(request.getAccessTokens());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {

		}

		addUpdateUserCredential(userCredential);
	}

	public void addUpdateUserConfigs(UserCredential userCredential) {
		addUpdateUserCredential(userCredential);
	}

	private void addUpdateUserCredential(UserCredential userCredential) {
		List<String> collections = collectionsListManager.getCollectionsExcludingSystem();
		validateAdminIsActive(userCredential);
		UserCredential savedUserCredential = userCredential;
		for (String groupCode : userCredential.getGlobalGroups()) {
			GlobalGroup group = globalGroupsManager.getGlobalGroupWithCode(groupCode);
			if (group == null) {
				throw new UserServicesRuntimeException_InvalidGroup(groupCode);
			}
			for (String collection : group.getUsersAutomaticallyAddedToCollections()) {
				if (collections.contains(collection)) {
					savedUserCredential = savedUserCredential.addCollection(collection);
				}
			}
		}
		userCredentialsManager.addUpdate(savedUserCredential);
		sync(toSystemWideUserInfos(savedUserCredential));
	}

	public GlobalGroup createGlobalGroup(
			String code, String name, List<String> collections, String parent, GlobalGroupStatus status,
			boolean locallyCreated) {
		return globalGroupsManager.create(code, name, collections, parent, status, locallyCreated);
	}

	public void addUpdateGlobalGroup(GlobalGroup globalGroup) {
		GlobalGroup currentGroup = globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode());
		List<String> wasAutomaticCollections = currentGroup == null ? new ArrayList<String>() : currentGroup
				.getUsersAutomaticallyAddedToCollections();
		ListComparisonResults<String> comparisonResults = new LangUtils().compare(wasAutomaticCollections,
				globalGroup.getUsersAutomaticallyAddedToCollections());
		for (String newAutomaticCollection : comparisonResults.getNewItems()) {
			for (SystemWideUserInfos userInGroup : getGlobalGroupActifUsers(globalGroup.getCode())) {
				addUserToCollection(userInGroup.getUsername(), newAutomaticCollection);
			}
		}
		if (globalGroup.getStatus() == GlobalGroupStatus.ACTIVE) {
			activateGlobalGroupHierarchyWithoutUserValidation(globalGroup);
		} else {
			logicallyRemoveGroupHierarchyWithoutUserValidation(globalGroup);
		}
		globalGroupsManager.addUpdate(globalGroup);
		sync(globalGroup);
	}

	public void addUserToCollection(SystemWideUserInfos user, String collection) {
		addUserToCollection(user.getUsername(), collection);
	}

	public void addUserToCollection(String username, String collection) {
		UserCredential userCredential = getUserCredential(username);
		if (!userCredential.getCollections().contains(collection)) {
			try {
				addUpdateUserCredential(userCredential.addCollection(collection));
			} catch (UserServicesRuntimeException_CannotExcuteTransaction e) {
				// Revert change in XML config
				userCredentialsManager.addUpdate(userCredential.removeCollection(collection));
				throw e;
			}
		} else {
			// This apparently redundant sync allows to add a user to a collection
			// in case the user credential configuration file and the solr state are out of sync
			sync(getUserInfos(userCredential.getUsername()));
		}
	}

	//TODO Refact User method with
	@Deprecated
	public void addUserToCollection(UserCredential userCredential, String collection) {
		addUserToCollection(userCredential.getUsername(), collection);
	}

	public void setGlobalGroupUsers(String groupCode, List<SystemWideUserInfos> newUserList) {
		List<String> newUsernameList = toUserNames(newUserList);

		for (SystemWideUserInfos userCredential : newUserList) {
			UserCredential latestCrendential = getUser(userCredential.getUsername());
			if (!latestCrendential.getGlobalGroups().contains(groupCode)) {
				List<String> groupCodes = new ArrayList<>();
				groupCodes.addAll(latestCrendential.getGlobalGroups());
				groupCodes.add(groupCode);
				addUpdateUserCredential(latestCrendential.setGlobalGroups(groupCodes));
			}
		}

		List<SystemWideUserInfos> currentList = getGlobalGroupActifUsers(groupCode);
		for (SystemWideUserInfos currentListUser : currentList) {
			UserAddUpdateRequest request = addEditRequest(currentListUser.getUsername());
			if (!newUsernameList.contains(currentListUser.getUsername())) {
				List<String> groupCodes = new ArrayList<>();
				groupCodes.addAll(currentListUser.getGlobalGroups());
				groupCodes.remove(groupCode);
				addUpdateUserCredential(request.setGlobalGroups(groupCodes));
			}
		}
	}

	public List<SystemWideUserInfos> getGlobalGroupActifUsers(String groupCode) {
		return userCredentialsManager.getUserCredentialsInGlobalGroup(groupCode)
				.stream().map((u) -> getUserInfos(u.getUsername())).collect(toList());
	}

	public SystemWideUserInfos getUserInfos(String username) {
		UserCredential credential = userCredentialsManager.getUserCredential(username);
		if (credential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(username);
		}


		return toSystemWideUserInfos(credential);
	}

	private SystemWideUserInfos toSystemWideUserInfos(UserCredential credential) {
		SystemWideUserInfos infos = new SystemWideUserInfos()
				.setUserCredentialId(credential.getId())
				.setUsername(credential.getUsername())
				.setFirstName(credential.getFirstName())
				.setLastName(credential.getLastName())
				.setTitle(credential.getTitle())
				.setEmail(credential.getEmail())
				.setServiceKey(credential.getServiceKey())
				.setSystemAdmin(credential.isSystemAdmin())
				.setStatus(credential.getStatus())
				.setCollections(credential.getCollections())
				.setGlobalGroups(credential.getGlobalGroups())
				.setDomain(credential.getDomain())
				.setMsExchangeDelegateList(credential.getMsExchDelegateListBL())
				.setDn(credential.getDn())

				.setAccessTokens(credential.getAccessTokens());

		try {
			infos.setPersonalEmails(credential.getPersonalEmails());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setPhone(credential.getPhone());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setFax(credential.getFax());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setJobTitle(credential.getJobTitle());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setAddress(credential.getAddress());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setDoNotReceiveEmails(credential.isNotReceivingEmails());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setAgentStatus(credential.getAgentStatus());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setHasReadLastAlert(credential.hasReadLastAlert());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setEnableFacetsApplyButton(credential.isApplyFacetsEnabled());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setHasAgreedToPrivacyPolicy(credential.hasAgreedToPrivacyPolicy());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}


		try {
			infos.setElectronicSignature(credential.getElectronicSignature());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setElectronicInitials(credential.getElectronicInitials());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		try {
			infos.setAzureUsername(credential.getAzureUsername());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {

		}

		return infos;
	}

	public UserCredential getUser(String username) {
		UserCredential credential = userCredentialsManager.getUserCredential(username);
		if (credential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(username);
		}
		return credential;
	}

	public UserCredential getUserConfigs(String username) {
		UserCredential credential = userCredentialsManager.getUserCredential(username);
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

		UserCredential credential = userCredentialsManager.getAzureUserCredential(azureUsername);
		if (credential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(azureUsername);
		}
		return getUserInfos(credential.getUsername());
	}

	public void updateAzureUsername(String username, String azureUser) {
		UserAddUpdateRequest request = addEditRequest(username);
		request.setAzureUsername(azureUser);
		addUpdateUserCredential(request);
	}


	public GlobalGroup getNullableGroup(String groupCode) {
		return globalGroupsManager.getGlobalGroupWithCode(groupCode);
	}

	public GlobalGroup getGroup(String groupCode) {
		GlobalGroup group = getNullableGroup(groupCode);
		if (group == null) {
			throw new UserServicesRuntimeException_NoSuchGroup(groupCode);
		}
		return group;
	}

	public GlobalGroup getActiveGroup(String groupCode) {
		GlobalGroup group = globalGroupsManager.getActiveGlobalGroupWithCode(groupCode);
		if (group == null) {
			throw new UserServicesRuntimeException_NoSuchGroup(groupCode);
		}
		return group;
	}

	public Group getGroupInCollection(String groupCode, String collection) {
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		return schemas.getGroupWithCode(groupCode);
	}

	public void addGlobalGroupsInCollection(String collection) {
		List<GlobalGroup> groups = globalGroupsManager.getActiveGroups();

		Set<String> addedGroups = new HashSet<>();

		for (int i = 0; i < 1000 && addedGroups.size() != groups.size(); i++) {
			for (GlobalGroup globalGroup : groups) {
				if (!addedGroups.contains(globalGroup.getCode())) {
					if (globalGroup.getParent() == null || addedGroups.contains(globalGroup.getParent())) {
						sync(globalGroup);
						addedGroups.add(globalGroup.getCode());
					}
				}
			}
		}
	}

	public void removeUserFromCollection(String username, String collection) {
		UserCredential userCredential = getUser(username);
		userCredentialsManager.removeUserCredentialFromCollection(userCredential, collection);

		LogicalSearchCondition condition = fromUsersIn(collection)
				.where(usernameMetadata(collection)).is(userCredential.getUsername());
		Record userCredentialRecord = searchServices.searchSingleResult(condition);
		recordServices.logicallyDelete(userCredentialRecord, User.GOD);
	}

	void activateGlobalGroupHierarchy(UserCredential userCredential, GlobalGroup globalGroup) {
		permissionValidateCredentialOnGroup(userCredential);
		activateGlobalGroupHierarchyWithoutUserValidation(globalGroup);
	}

	private void activateGlobalGroupHierarchyWithoutUserValidation(GlobalGroup globalGroup) {
		List<String> collections = collectionsListManager.getCollections();
		restoreGroupHierarchyInBigVault(globalGroup.getCode(), collections);
		globalGroupsManager.activateGlobalGroupHierarchy(globalGroup);
	}

	void removeUserCredentialAndUser(UserCredential userCredential) {
		userCredential = userCredential.setStatus(DELETED);
		addUpdateUserCredential(userCredential);
	}

	public void removeUserCredentialAndUser(SystemWideUserInfos userCredential) {
		addUpdateUserCredential(addEditRequest(userCredential.getUsername()).setStatus(DELETED));
	}

	public void setUserCredentialAndUserStatusPendingApproval(String username) {
		UserCredential userCredential = getUser(username);
		userCredential = userCredential.setStatus(UserCredentialStatus.PENDING);
		addUpdateUserCredential(userCredential);
	}

	public void suspendUserCredentialAndUser(String username) {

		UserCredential userCredential = getUser(username);
		userCredential = userCredential.setStatus(UserCredentialStatus.SUSPENDED);
		addUpdateUserCredential(userCredential);
	}

	public void activeUserCredentialAndUser(String username) {

		UserCredential userCredential = getUser(username);
		userCredential = userCredential.setStatus(UserCredentialStatus.ACTIVE);
		addUpdateUserCredential(userCredential);
		List<String> collections = userCredential.getCollections();
		restoreUserInBigVault(userCredential.getUsername(), collections);
	}

	private void restoreUserInBigVault(String username, List<String> collections) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition;
		for (String collection : collections) {
			condition = fromUsersIn(collection).where(usernameMetadata(collection))
					.isEqualTo(username).andWhere(LOGICALLY_DELETED_STATUS).isTrue();
			query.setCondition(condition);
			Record recordGroup = searchServices.searchSingleResult(condition);
			if (recordGroup != null) {
				recordServices.restore(recordGroup, User.GOD);
			}
		}
	}

	private void restoreGroupHierarchyInBigVault(String globalGroupCode, List<String> collections) {
		for (GlobalGroup group : globalGroupsManager.getHierarchy(globalGroupCode)) {
			LogicalSearchQuery query = new LogicalSearchQuery();
			LogicalSearchCondition condition;
			for (String collection : collections) {
				condition = fromGroupsIn(collection)
						.where(groupCodeMetadata(collection)).isEqualTo(group.getCode())
						.andWhere(LOGICALLY_DELETED_STATUS).isTrue();
				query.setCondition(condition);
				Record recordGroup = searchServices.searchSingleResult(condition);
				if (recordGroup != null) {
					recordServices.restore(recordGroup, User.GOD);
				}
			}
		}
	}

	public void logicallyRemoveGroupHierarchy(String username, GlobalGroup globalGroup) {
		UserCredential userCredential = getUser(username);
		permissionValidateCredentialOnGroup(userCredential);
		logicallyRemoveGroupHierarchyWithoutUserValidation(globalGroup);

	}

	public void logicallyRemoveGroupHierarchy(SystemWideUserInfos systemWideUserInfos, GlobalGroup globalGroup) {
		permissionValidateCredentialOnGroup(systemWideUserInfos);
		logicallyRemoveGroupHierarchyWithoutUserValidation(globalGroup);

	}

	private void logicallyRemoveGroupHierarchyWithoutUserValidation(GlobalGroup globalGroup) {
		List<String> collections = collectionsListManager.getCollections();
		List<SystemWideUserInfos> users = getGlobalGroupActifUsers(globalGroup.getCode());

		userCredentialsManager.removeGroup(globalGroup.getCode());
		globalGroupsManager.logicallyRemoveGroup(globalGroup);
		removeGroupFromCollectionsWithoutUserValidation(globalGroup.getCode(), collections);

		syncUsersCredentials(users);
	}


	void removeGroupFromCollectionsWithoutUserValidation(String group, List<String> collections) {
		removeChildren(group, collections);
		removeFromBigVault(group, collections);
	}

	private void removeChildren(String group, List<String> collections) {
		for (String collection : collections) {
			for (Group child : getChildrenOfGroupInCollection(group, collection)) {
				removeFromBigVault(child.getCode(), Arrays.asList(collection));
				removeChildren(child.getCode(), Arrays.asList(collection));
			}
		}
	}

	public Group createCustomGroupInCollectionWithCodeAndName(String collection, String code, String name) {
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema schema = types.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
		Record groupRecord = recordServices.newRecordWithSchema(schema);
		groupRecord.set(schema.getMetadata(Group.CODE), code);
		groupRecord.set(schema.getMetadata(Group.TITLE), name);
		try {
			recordServices.add(groupRecord);
		} catch (RecordServicesException e) {
			throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
		}
		return new Group(groupRecord, types);
	}

	public void givenSystemAdminPermissionsToUser(SystemWideUserInfos user) {
		addUpdateUserCredential(addEditRequest(user.getUsername()).setSystemAdminEnabled());
	}

	public String giveNewServiceToken(String username) {
		UserCredential modifiedUser = addEdit(username).setServiceKey(secondaryUniqueIdGenerator.next());
		addUpdateUserCredential(modifiedUser);
		return modifiedUser.getServiceKey();
	}

	public String giveNewServiceToken(SystemWideUserInfos user) {
		return giveNewServiceToken(user.getUsername());
	}

	public void sync(SystemWideUserInfos user) {
		List<String> availableCollections = collectionsListManager.getCollectionsExcludingSystem();
		List<String> removedCollections = new ArrayList<>();
		for (String collection : user.getCollections()) {
			if (availableCollections.contains(collection)) {
				Transaction transaction = new Transaction().setSkippingReferenceToLogicallyDeletedValidation(true);
				sync(user, collection, transaction);
				try {
					recordServices.execute(transaction);
				} catch (RecordServicesException e) {
					throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
				}
			} else {
				removedCollections.add(collection);
				LOGGER.warn("User '" + user.getUsername() + "' is in invalid collection '" + collection + "'");
			}
		}

		if (!removedCollections.isEmpty()) {
			List<String> collections = new ArrayList<>(user.getCollections());
			collections.removeAll(removedCollections);
			addUpdateUserCredential(addEditRequest(user.getUsername()).setCollections(collections));
		}
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

	private List<String> toUserNames(List<SystemWideUserInfos> users) {
		List<String> usernames = new ArrayList<>();
		for (SystemWideUserInfos user : users) {
			usernames.add(user.getUsername());
		}
		return usernames;
	}


	private void sync(SystemWideUserInfos user, String collection, Transaction transaction) {
		User userInCollection = getUserInCollection(user.getUsername(), collection);

		if (userInCollection == null) {
			userInCollection = newUserInCollection(collection);
		} else {
			userInCollection.set(CommonMetadataBuilder.LOGICALLY_DELETED, false);
		}
		userInCollection.setEmail(StringUtils.isBlank(user.getEmail()) ? null : user.getEmail());
		if (userInCollection.getSchema().hasMetadataWithCode(UserCredential.PERSONAL_EMAILS)) {
			userInCollection.setPersonalEmails(isEmpty(user.getPersonalEmails()) ? null : user.getPersonalEmails());
		}
		userInCollection.setFirstName(user.getFirstName());
		userInCollection.setLastName(user.getLastName());
		userInCollection.setUsername(user.getUsername());
		userInCollection.setSystemAdmin(user.isSystemAdmin());
		try {
			userInCollection.setPhone(user.getPhone());
			userInCollection.setJobTitle(user.getJobTitle());
			userInCollection.setAddress(user.getAddress());
			userInCollection.setFax(user.getFax());
		} catch (MetadataSchemasRuntimeException.NoSuchMetadata e) {
			//Normal with versions before 6.2
		}
		setRoles(userInCollection);
		changeUserStatus(userInCollection, user.getUsername());
		List<String> groupIds = getGroupIds(user.getGlobalGroups(), collection);
		List<String> UserInCollectionGroupIds = userInCollection.getUserGroups();
		if (!hasSameGroups(groupIds, UserInCollectionGroupIds)) {
			userInCollection.setUserGroups(groupIds);
		}
		if (userInCollection.isDirty()) {
			transaction.add(userInCollection.getWrappedRecord());
		}
	}

	private void setRoles(User userInCollection) {
		if (userInCollection.getUserRoles() != null && userInCollection.getUserRoles().isEmpty()) {
			try {
				Role role = rolesManager.getRole(userInCollection.getCollection(), "U");
				if (role != null) {
					userInCollection.setUserRoles("U");
				}
			} catch (RolesManagerRuntimeException e) {
				//
			}
		}
	}

	private void changeUserStatus(User userInCollection, String username) {
		UserCredential userCredential = getUserConfigs(username);
		if (userCredential.getStatus() != null) {
			userInCollection.setStatus(userCredential.getStatus());
			if (userCredential.getStatus() == UserCredentialStatus.ACTIVE) {
				userInCollection.set("deleted", false);
			} else {
				validateAdminIsActive(userCredential);
				userInCollection.set("deleted", true);
			}
		}
	}

	private List<String> getGroupIds(List<String> groupCodes, String collection) {
		List<String> groupIds = new ArrayList<>();
		for (String groupCode : groupCodes) {
			String groupId = getGroupIdInCollection(groupCode, collection);
			if (groupId != null) {
				groupIds.add(groupId);
				//throw new ImpossibleRuntimeException("No group with code '" + groupCode + "' in collection '" + collection + "'");
			}

		}
		return groupIds;
	}

	String getGroupIdInCollection(String groupCode, String collection) {
		Group group = getGroupInCollection(groupCode, collection);
		return group == null ? null : group.getId();
	}

	private boolean hasSameGroups(List<String> groupIds1, List<String> groupIds2) {
		ListComparisonResults<String> comparisonResults = new LangUtils().compare(groupIds1, groupIds2);

		return comparisonResults.getNewItems().isEmpty() && comparisonResults.getRemovedItems().isEmpty();
	}

	public void sync(GlobalGroup group) {
		Transaction transaction;
		for (String collection : collectionsListManager.getCollections()) {
			transaction = new Transaction();
			sync(group, collection, transaction);
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
			}
		}
	}

	private void sync(GlobalGroup group, String collection, Transaction transaction) {
		String groupCode = group.getCode();
		Group groupInCollection = getGroupInCollection(groupCode, collection);
		if (groupInCollection == null) {
			groupInCollection = newGroupInCollection(collection);
		}
		groupInCollection.set(Group.CODE, group.getCode());
		String parentId = getGroupParentId(group, collection);
		groupInCollection.set(Group.PARENT, parentId);
		groupInCollection.set(Group.IS_GLOBAL, true);
		groupInCollection.set(LOGICALLY_DELETED_STATUS, group.getLogicallyDeletedStatus());
		groupInCollection.set(LOGICALLY_DELETED_ON, group.get(LOGICALLY_DELETED_ON));
		groupInCollection.setTitle(group.getName());
		if (groupInCollection.isDirty()) {
			transaction.add(groupInCollection.getWrappedRecord());
		}
	}

	private String getGroupParentId(GlobalGroup group, String collection) {
		String parentId = null;
		if (group.getParent() != null) {
			parentId = getGroupIdInCollection(group.getParent(), collection);
			if (parentId == null) {
				throw new ImpossibleRuntimeException("No group with code '" + parentId + "' in collection '" + collection + "'");
			}
		}
		return parentId;
	}

	private OngoingLogicalSearchCondition fromUsersIn(String collection) {
		return from(userSchema(collection));
	}

	private OngoingLogicalSearchCondition fromGroupsIn(String collection) {
		return from(groupSchema(collection));
	}

	private MetadataSchemaTypes schemaTypes(String collection) {
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	MetadataSchema userSchema(String collection) {
		MetadataSchemaTypes schemaTypes = schemaTypes(collection);
		return schemaTypes.getDefaultSchema(User.SCHEMA_TYPE);
	}

	Metadata usernameMetadata(String collection) {
		return userSchema(collection).getMetadata(User.USERNAME);
	}

	Metadata userGroupsMetadata(String collection) {
		return userSchema(collection).getMetadata(User.GROUPS);
	}

	MetadataSchema groupSchema(String collection) {
		return schemaTypes(collection).getDefaultSchema(Group.SCHEMA_TYPE);
	}

	Metadata groupCodeMetadata(String collection) {
		return groupSchema(collection).getMetadata(Group.CODE);
	}

	private Metadata groupParentMetadata(String collection) {
		return groupSchema(collection).getMetadata(Group.PARENT);
	}

	User newUserInCollection(String collection) {
		Record record = recordServices.newRecordWithSchema(userSchema(collection));
		return new User(record, schemaTypes(collection), rolesManager.getCollectionRoles(collection, modelLayerFactory));
	}

	Group newGroupInCollection(String collection) {
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

	List<Group> getGlobalGroupsInCollections(String collection) {
		MetadataSchemaTypes collectionTypes = metadataSchemasManager.getSchemaTypes(collection);
		LogicalSearchQuery query = new LogicalSearchQuery(allGroupsWhereGlobalGroupFlag(collectionTypes).isTrue());
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

	private void syncUsersCredentials(List<SystemWideUserInfos> users) {
		for (SystemWideUserInfos userCredential : users) {
			SystemWideUserInfos userCredentialUpdated = getUserInfos(userCredential.getUsername());
			sync(userCredentialUpdated);
		}
	}

	private void removeFromBigVault(String group, List<String> collections) {
		for (String collection : collections) {
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			LogicalSearchCondition condition = from(types.getSchemaType(Group.SCHEMA_TYPE))
					.where(groupCodeMetadata(collection)).isEqualTo(group);
			Record recordGroup = searchServices.searchSingleResult(condition);
			if (recordGroup != null) {
				recordServices.logicallyDelete(recordGroup, User.GOD);
			}
		}
	}

	public boolean isSystemAdmin(User user) {
		UserCredential userCredential = getUserCredential(user.getUsername());
		return userCredential != null && userCredential.isSystemAdmin();
	}

	private void permissionValidateCredentialOnGroup(UserCredential userCredential) {
		if (!has(userCredential).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS_ACTIVATION)) {
			throw new UserServicesRuntimeException_UserPermissionDeniedToDelete(userCredential.getUsername());
		}
	}


	private void permissionValidateCredentialOnGroup(SystemWideUserInfos userCredential) {
		if (!has(userCredential).globalPermissionInAnyCollection(CorePermissions.MANAGE_SYSTEM_GROUPS_ACTIVATION)) {
			throw new UserServicesRuntimeException_UserPermissionDeniedToDelete(userCredential.getUsername());
		}
	}


	public void removeUserFromGlobalGroup(String username, String globalGroupCode) {
		UserCredential user = getUser(username);
		List<String> newGlobalGroups = new ArrayList<>();
		newGlobalGroups.addAll(user.getGlobalGroups());
		if (!newGlobalGroups.remove(globalGroupCode)) {
			throw new UserServicesRuntimeException_NoSuchGroup(globalGroupCode);
		}
		addUpdateUserCredential(user.setGlobalGroups(newGlobalGroups));
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
		if (userCredentialsManager.getServiceKeyByToken(token) != null) {
			userCredentialsManager.removeToken(token);
			String username = userCredentialsManager.getUsernameByServiceKey(serviceKey);
			String newToken = generateToken(username);
			return newToken;
		} else {
			throw new UserServicesRuntimeException_InvalidToken();
		}
	}

	public String generateToken(String username) {
		ReadableDuration duration = modelLayerConfiguration.getTokenDuration();
		return generateToken(username, duration);
	}

	public String generateToken(String username, ReadableDuration duration) {
		String token = secondaryUniqueIdGenerator.next();
		LocalDateTime expiry = TimeProvider.getLocalDateTime().plus(duration);
		UserCredential userCredential = getUser(username).addAccessToken(token, expiry);
		userCredentialsManager.addUpdate(userCredential);
		return token;
	}

	public String generateToken(String username, String unitTime, int duration) {
		String token = secondaryUniqueIdGenerator.next();
		LocalDateTime expiry = unitTime.equals("hours") ?
							   TimeProvider.getLocalDateTime().plusHours(duration) :
							   TimeProvider.getLocalDateTime().plusDays(duration);
		UserCredential userCredential = getUser(username).addAccessToken(token, expiry);
		userCredentialsManager.addUpdate(userCredential);
		return token;
	}

	private void validateAdminIsActive(UserCredential userCredential) {
		if (userCredential.getUsername().equals(ADMIN) && userCredential.getStatus() != UserCredentialStatus.ACTIVE) {
			throw new UserServicesRuntimeException_CannotRemoveAdmin();
		}
	}

	public String getTokenUser(String serviceKey, String token) {
		String retrivedServiceKey = getServiceKeyByToken(token);
		if (retrivedServiceKey == null || !retrivedServiceKey.equals(serviceKey)) {
			throw new UserServicesRuntimeException_InvalidToken();
		}
		return userCredentialsManager.getUsernameByServiceKey(serviceKey);
	}

	public String getServiceKeyByToken(String token) {
		String retrivedServiceKey = userCredentialsManager.getServiceKeyByToken(token);
		if (retrivedServiceKey != null) {
			return retrivedServiceKey;
		} else {
			throw new UserServicesRuntimeException_InvalidToken();
		}
	}

	public void removeToken(String token) {
		userCredentialsManager.removeToken(token);
	}

	public String getUserCredentialByServiceKey(String serviceKey) {
		return userCredentialsManager.getUsernameByServiceKey(serviceKey);
	}

	public UserCredential getUserCredential(String username) {
		return userCredentialsManager.getUserCredential(username);
	}

	public SystemWideUserInfos getUserCredentialByDN(String dn) {
		UserCredential userCredential = userCredentialsManager.getUserCredentialByDN(dn);
		return userCredential == null ? null : getUserInfos(userCredential.getUsername());
	}

	public List<SystemWideUserInfos> getActiveUserCredentials() {
		return userCredentialsManager.getActiveUserCredentials()
				.stream().map((u) -> getUserInfos(u.getUsername())).collect(toList());
	}

	public List<SystemWideUserInfos> getAllUserCredentials() {
		return userCredentialsManager.getUserCredentials()
				.stream().map((u) -> getUserInfos(u.getUsername())).collect(toList());
	}

	public List<GlobalGroup> getAllGlobalGroups() {
		return globalGroupsManager.getAllGroups();
	}

	List<Group> getChildrenOfGroupInCollection(String groupParentCode, String collection) {
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

	public CredentialUserPermissionChecker has(User user) {
		return has(user.getUsername());
	}

	public CredentialUserPermissionChecker has(UserCredential userCredential) {
		return has(userCredential.getUsername());
	}

	public CredentialUserPermissionChecker has(SystemWideUserInfos userCredential) {
		return has(userCredential.getUsername());
	}


	public CredentialUserPermissionChecker has(String username) {
		List<User> users = new ArrayList<>();
		UserCredential user = getUser(username);
		for (String collection : user.getCollections()) {
			users.add(getUserInCollection(username, collection));
		}
		return new CredentialUserPermissionChecker(users);
	}

	public List<User> getAllUsersInCollection(String collection) {
		List<User> usersInCollection = new ArrayList<>();
		for (SystemWideUserInfos userCredential : getAllUserCredentials()) {
			if (userCredential.getCollections().contains(collection)) {
				User user = getUserInCollection(userCredential.getUsername(), collection);
				if (user != null) {
					usersInCollection.add(getUserInCollection(userCredential.getUsername(), collection));
				}
			}
		}
		return usersInCollection;
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

	public List<GlobalGroup> safePhysicalDeleteAllUnusedGlobalGroups() {
		return physicallyRemoveGlobalGroup(globalGroupsManager.getAllGroups().toArray(new GlobalGroup[0]));
	}

	List<GlobalGroup> physicallyRemoveGlobalGroup(GlobalGroup... globalGroups) {
		List<GlobalGroup> groupWithUserList = new ArrayList<>();
		for (GlobalGroup group : globalGroups) {
			List<SystemWideUserInfos> userInGroup = this.getGlobalGroupActifUsers(group.getCode());
			if ((group.getStatus().equals(GlobalGroupStatus.INACTIVE) && userInGroup.size() == 0)) {
				globalGroupsManager.logicallyRemoveGroup(group);
				recordServices.physicallyDelete(((GlobalGroup) group).getWrappedRecord(), User.GOD);
			} else if (userInGroup.size() != 0) {
				groupWithUserList.add(group);
			}
		}
		return groupWithUserList;
	}

	public List<Group> safePhysicalDeleteAllUnusedGroups(String collection) {
		List<Group> nonDeletedGroups = new ArrayList<>();
		MetadataSchemaTypes collectionTypes = metadataSchemasManager.getSchemaTypes(collection);
		LogicalSearchQuery query = new LogicalSearchQuery(allGroups(collectionTypes).returnAll());
		query.filteredByStatus(StatusFilter.DELETED);

		List<Group> deletedGroups = Group.wrap(searchServices.search(query), collectionTypes);
		for (Group group : deletedGroups) {
			LOGGER.info("safePhysicalDeleteAllUnusedGroups : " + group.getCode());
			try {
				physicallyRemoveGroup(group, collection);
			} catch (UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically e) {
				LOGGER.warn("Exception on safePhysicalDeleteAllUnusedGroups : " + group.getCode());
				nonDeletedGroups.add(group);
			}
		}

		return nonDeletedGroups;
	}

	void physicallyRemoveGroup(Group group, String collection) {
		LOGGER.info("physicallyRemoveGroup : " + group.getCode());

		List<Record> userInGroup = authorizationsServices.getUserRecordsInGroup(group.getWrappedRecord());
		if (userInGroup.size() != 0 ||
			searchServices.hasResults(fromAllSchemasIn(collection).where(Schemas.ALL_REFERENCES).isEqualTo(group.getId()))) {
			LOGGER.warn("Exception on physicallyRemoveGroup : " + group.getCode());
			throw new UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically(group.getCode());
		}

		recordServices.logicallyDelete(group.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(group.getWrappedRecord(), User.GOD);
	}

	public List<SystemWideUserInfos> safePhysicalDeleteAllUnusedUserCredentials() {
		List<SystemWideUserInfos> nonDeletedUsers = new ArrayList<>();
		Predicate<SystemWideUserInfos> filter = new Predicate<SystemWideUserInfos>() {
			@Override
			public boolean apply(SystemWideUserInfos input) {
				return input.getStatus().equals(DELETED);
			}
		};
		List<SystemWideUserInfos> userCredentials = this.getAllUserCredentials();
		LOGGER.info("safePhysicalDeleteAllUnusedUsers getAllUserCredentials  : " + userCredentials.size());
		Collection<SystemWideUserInfos> usersToDelete = Collections2.filter(userCredentials, filter);
		LOGGER.info("safePhysicalDeleteAllUnusedUsers usersToDelete  : " + usersToDelete.size());
		for (SystemWideUserInfos credential : usersToDelete) {
			try {
				safePhysicalDeleteUserCredential(credential.getUsername());
			} catch (UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically e) {
				nonDeletedUsers.add(credential);
			}
		}
		return nonDeletedUsers;
	}

	void safePhysicalDeleteUserCredential(String username)
			throws UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically {
		LOGGER.info("safePhysicalDeleteUser : " + username);
		UserCredential userCredential = getUser(username);
		for (String collection : userCredential.getCollections()) {
			User user = this.getUserInCollection(userCredential.getUsername(), collection);
			if (user != null) {
				if (searchServices.hasResults(
						fromAllSchemasIn(collection).where(Schemas.ALL_REFERENCES)
								.isEqualTo(user.getId()))) {
					LOGGER.warn("Exception on safePhysicalDeleteUser : " + username);
					throw new UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically(username);
				}
			}
		}
		recordServices.logicallyDelete((userCredential).getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete((userCredential).getWrappedRecord(), User.GOD);
	}

	public List<User> safePhysicalDeleteAllUnusedUsers(String collection) {
		List<User> nonDeletedUsers = new ArrayList<>();
		MetadataSchemaTypes collectionTypes = metadataSchemasManager.getSchemaTypes(collection);
		LogicalSearchQuery query = new LogicalSearchQuery(
				from(collectionTypes.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema()).returnAll());
		List<User> deletedUsers = new ArrayList<>();
		SchemasRecordsServices schemas = new SchemasRecordsServices(collection, modelLayerFactory);
		for (Record record : searchServices.search(query)) {
			deletedUsers.add(schemas.wrapUser(record));
		}
		for (User user : deletedUsers) {
			LOGGER.info("safePhysicalDeleteAllUnusedUsers : " + user.getUsername());
			try {
				physicallyRemoveUser(user, collection);
			} catch (UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically e) {
				LOGGER.warn("Exception on safePhysicalDeleteAllUnusedUsers : " + user.getUsername());
				nonDeletedUsers.add(user);
			}
		}

		return nonDeletedUsers;
	}

	public void physicallyRemoveUserCredentialAndUsers(String username) {
		List<User> users = getUserForEachCollection(username);
		for (User user : users) {
			String collection = user.getCollection();
			physicallyRemoveUser(user, collection);
		}

		LOGGER.info("physicallyRemoveUserCredential : " + username);
		UserCredential userCredential = getUserCredential(username);
		recordServices.logicallyDelete(userCredential.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(userCredential.getWrappedRecord(), User.GOD);
	}

	void physicallyRemoveUser(User user, String collection) {
		LOGGER.info("physicallyRemoveUser : " + user.getUsername());

		if (searchServices.hasResults(fromAllSchemasIn(collection).where(Schemas.ALL_REFERENCES).isEqualTo(user.getId()))) {
			LOGGER.warn("Exception on physicallyRemoveUser : " + user.getUsername());
			throw new UserServicesRuntimeException.UserServicesRuntimeException_CannotSafeDeletePhysically(user.getUsername());
		}

		recordServices.logicallyDelete(user.getWrappedRecord(), User.GOD);
		recordServices.physicallyDelete(user.getWrappedRecord(), User.GOD);
	}

	void restoreDeletedGroup(String groupCode, String collection) {
		GlobalGroup globalGroup = globalGroupsManager.getGlobalGroupWithCode(groupCode);
		if (globalGroup.getStatus().equals(GlobalGroupStatus.INACTIVE)) {
			globalGroupsManager.addUpdate(globalGroup.setStatus(GlobalGroupStatus.ACTIVE));
		}

		MetadataSchemaTypes collectionTypes = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema groupSchema = collectionTypes.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
		LogicalSearchCondition condition = fromGroupsIn(collection).where(groupCodeMetadata(collection)).is(groupCode);
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.filteredByStatus(StatusFilter.DELETED);

		List<Group> groups = Group.wrap(searchServices.search(query), collectionTypes);
		for (Group group : groups) {
			LOGGER.info("restoreDeletedGroup : " + group.getCode());
			recordServices.restore(group.getWrappedRecord(), User.GOD);
		}
	}

	public boolean isAdminInAnyCollection(String username) {
		return has(username).globalPermissionInAnyCollection(CorePermissions.MANAGE_SECURITY);
	}

	public List<User> getAllUsersInGroup(Group group, boolean includeGroupInheritance,
										 boolean onlyActiveUsersAndGroups) {
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
				GlobalGroup globalGroup = userServices.getGroup(group.getCode());
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

	public boolean isGroupActive(Group group) {
		GlobalGroup globalGroup = globalGroupsManager.getGlobalGroupWithCode(group.getCode());
		return globalGroup == null || isGroupActive(globalGroup);
	}

	public boolean isGroupActive(String aGroup) {

		GlobalGroup globalGroup;
		Record record = recordServices.getDocumentById(aGroup);

		if (Group.SCHEMA_TYPE.equals(record.getTypeCode())) {
			Group group = wrapNullable(record,
					modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(record.getCollection()));
			globalGroup = globalGroupsManager.getGlobalGroupWithCode(group.getCode());
		} else {
			SchemasRecordsServices schemas = new SchemasRecordsServices(
					SYSTEM_COLLECTION, modelLayerFactory);
			globalGroup = schemas.wrapGlobalGroup(record);
		}

		return isGroupActive(globalGroup);
	}

	private boolean isGroupActive(GlobalGroup globalGroup) {

		if (globalGroup.getStatus() == GlobalGroupStatus.INACTIVE) {
			return false;

		} else if (globalGroup.getParent() != null) {
			return isGroupActive(globalGroupsManager.getGlobalGroupWithCode(globalGroup.getParent()));

		} else {
			return true;
		}
	}

	public List<User> getUserForEachCollection(String username) {

		List<User> users = new ArrayList<>();
		UserCredential userCredential = getUserCredential(username);
		for (String collection : userCredential.getCollections()) {
			users.add(getUserInCollection(userCredential.getUsername(), collection));
		}

		return users;
	}

	public List<Group> getGroupForEachCollection(GlobalGroup globalGroup) {

		List<Group> groups = new ArrayList<>();
		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			groups.add(getGroupInCollection(globalGroup.getCode(), collection));
		}

		return groups;
	}

	public UserCredential addEdit(String username) {
		return userCredentialsManager.addEdit(username);
	}

	public UserAddUpdateRequest addEditRequest(String username) {
		UserCredential userCredential = getUserCredential(username);
		UserAddUpdateRequest request = new UserAddUpdateRequest();
		if (userCredential == null) {
			request.setUsername(cleanUsername(username));
		} else {
			request.setUsername(userCredential.getUsername())
					.setFirstName(userCredential.getFirstName())
					.setLastName(userCredential.getLastName())
					.setEmail(userCredential.getEmail())
					.setAzureUsername(userCredential.getAzureUsername())
					.setServiceKey(userCredential.getServiceKey())
					.setDn(userCredential.getDn())
					.setDomain(userCredential.getDomain())
					.setAddress(userCredential.getAddress())
					.setFax(userCredential.getFax())
					.setJobTitle(userCredential.getJobTitle())
					.setPhone(userCredential.getPhone())
					.setCollections(userCredential.getCollections())
					.setGlobalGroups(userCredential.getGlobalGroups())
					.setAccessTokens(userCredential.getAccessTokens())
					.setMsExchDelegateListBL(userCredential.getMsExchDelegateListBL())
					.setPersonalEmails(userCredential.getPersonalEmails())
					.setStatus(userCredential.getStatus())
					.setAgentStatus(userCredential.getAgentStatus());

			request.setElectronicInitials(userCredential.getElectronicInitials());
			request.setElectronicSignature(userCredential.getElectronicSignature());
		}
		return request;
	}

	public UserAddUpdateRequest addRequest(String username, String firstName, String lastName, String email) {
		return new UserAddUpdateRequest().setUsername(cleanUsername(username)).setFirstName(firstName).setLastName(lastName).setEmail(email);
	}

	void removeUserCredentialFromCollection(String username, String collection) {
		userCredentialsManager.removeUserCredentialFromCollection(getUserCredential(username), collection);
	}

	public void reShowPrivacyPolicyToUser() {
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

	public void prepareForCollectionDelete(String collection) {
		userCredentialsManager.removeCollection(collection);
		globalGroupsManager.removeCollection(collection);
	}

	public void removeTimedOutTokens() {
		userCredentialsManager.removeTimedOutTokens();
	}

	public List<GlobalGroup> getActiveGroups() {
		return globalGroupsManager.getActiveGroups();

	}

	public List<GlobalGroup> getAllGroups() {
		return globalGroupsManager.getAllGroups();
	}

	public void logicallyRemoveGroup(GlobalGroup globalGroup) {
		globalGroupsManager.logicallyRemoveGroup(globalGroup);
	}
}
