/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.data.utils.LangUtils.ListComparisonResults;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.conf.ldap.LDAPConfigurationManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.GlobalGroup;
import com.constellio.model.entities.security.global.GlobalGroupStatus;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import com.constellio.model.services.security.authentification.AuthenticationService;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotExcuteTransaction;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_CannotRemoveAdmin;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidToken;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_InvalidUserNameOrPassword;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchGroup;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_NoSuchUser;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserIsNotInCollection;
import com.constellio.model.services.users.UserServicesRuntimeException.UserServicesRuntimeException_UserPermissionDeniedToDelete;

public class UserServices {

	public static final String ADMIN = "admin";
	private final UserCredentialsManager userCredentialsManager;
	private final GlobalGroupsManager globalGroupsManager;
	private final CollectionsListManager collectionsListManager;
	private final RecordServices recordServices;
	private final SearchServices searchServices;
	private final MetadataSchemasManager metadataSchemasManager;
	private final AuthenticationService authenticationService;
	private final LDAPConfigurationManager ldapConfigurationManager;
	private final RolesManager rolesManager;
	private final ModelLayerConfiguration modelLayerConfiguration;

	public UserServices(UserCredentialsManager userCredentialsManager, GlobalGroupsManager globalGroupsManager,
			CollectionsListManager collectionsListManager, RecordServices recordServices, SearchServices searchServices,
			MetadataSchemasManager metadataSchemasManager, AuthenticationService authenticationService, RolesManager rolesManager,
			ModelLayerConfiguration modelLayerConfiguration,
			LDAPConfigurationManager ldapConfigurationManager) {
		this.userCredentialsManager = userCredentialsManager;
		this.globalGroupsManager = globalGroupsManager;
		this.collectionsListManager = collectionsListManager;
		this.recordServices = recordServices;
		this.searchServices = searchServices;
		this.metadataSchemasManager = metadataSchemasManager;
		this.authenticationService = authenticationService;
		this.modelLayerConfiguration = modelLayerConfiguration;
		this.ldapConfigurationManager = ldapConfigurationManager;
		this.rolesManager = rolesManager;
	}

	public void addUpdateUserCredential(UserCredential userCredential) {
		validateAdminIsActive(userCredential);
		UserCredential savedUserCredential = userCredential;
		for (String groupCode : userCredential.getGlobalGroups()) {
			GlobalGroup group = globalGroupsManager.getGlobalGroupWithCode(groupCode);
			for (String collection : group.getUsersAutomaticallyAddedToCollections()) {
				savedUserCredential = savedUserCredential.withNewCollection(collection);
			}
		}
		userCredentialsManager.addUpdate(savedUserCredential);
		sync(savedUserCredential);
	}

	public void addUpdateGlobalGroup(GlobalGroup globalGroup) {
		GlobalGroup currentGroup = globalGroupsManager.getGlobalGroupWithCode(globalGroup.getCode());
		List<String> wasAutomaticCollections = currentGroup == null ? new ArrayList<String>() : currentGroup
				.getUsersAutomaticallyAddedToCollections();
		ListComparisonResults<String> comparisonResults = new LangUtils().compare(wasAutomaticCollections,
				globalGroup.getUsersAutomaticallyAddedToCollections());
		for (String newAutomaticCollection : comparisonResults.getNewItems()) {
			for (UserCredential userInGroup : getGlobalGroupActifUsers(globalGroup.getCode())) {
				addUserToCollection(userInGroup, newAutomaticCollection);
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

	public void addUserToCollection(UserCredential userCredential, String collection) {
		UserCredential latestCrendential = getUser(userCredential.getUsername());
		UserCredential userWithCollection = latestCrendential.withNewCollection(collection);
		if (userWithCollection != latestCrendential) {
			addUpdateUserCredential(userWithCollection);
		}
	}

	public void setGlobalGroupUsers(String groupCode, List<UserCredential> newUserList) {
		List<String> newUsernameList = toUserNames(newUserList);

		for (UserCredential userCredential : newUserList) {
			UserCredential latestCrendential = getUser(userCredential.getUsername());
			if (!latestCrendential.getGlobalGroups().contains(groupCode)) {
				List<String> groupCodes = new ArrayList<>();
				groupCodes.addAll(latestCrendential.getGlobalGroups());
				groupCodes.add(groupCode);
				addUpdateUserCredential(latestCrendential.withGlobalGroups(groupCodes));
			}
		}

		List<UserCredential> currentList = getGlobalGroupActifUsers(groupCode);
		for (UserCredential currentListUser : currentList) {
			if (!newUsernameList.contains(currentListUser.getUsername())) {
				List<String> groupCodes = new ArrayList<>();
				groupCodes.addAll(currentListUser.getGlobalGroups());
				groupCodes.remove(groupCode);
				addUpdateUserCredential(currentListUser.withGlobalGroups(groupCodes));
			}
		}
	}

	public List<UserCredential> getGlobalGroupActifUsers(String groupCode) {
		return userCredentialsManager.getUserCredentialsInGlobalGroup(groupCode);
	}

	public UserCredential getUser(String username) {
		UserCredential credential = userCredentialsManager.getUserCredential(username);
		if (credential == null) {
			throw new UserServicesRuntimeException_NoSuchUser(username);
		}
		return credential;
	}

	public User getUserInCollection(String username, String collection) {
		UserCredential userCredential = getUser(username);
		if (!userCredential.getCollections().contains(collection)) {
			throw new UserServicesRuntimeException_UserIsNotInCollection(username, collection);
		}
		return getUserRecordInCollection(username, collection);
	}

	public User getUserRecordInCollection(String username, String collection) {
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(userSchema(collection))
				.where(usernameMetadata(collection))
				.is(username);

		return User.wrapNullable(searchServices.searchSingleResult(condition), schemaTypes(collection),
				rolesManager.getCollectionRoles(collection));
	}

	public GlobalGroup getGroup(String groupCode) {
		GlobalGroup group = globalGroupsManager.getGlobalGroupWithCode(groupCode);
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
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(groupSchema(collection))
				.where(groupCodeMetadata(collection))
				.is(groupCode);
		return Group.wrapNullable(searchServices.searchSingleResult(condition), schemaTypes(collection));
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

	public void removeUserFromCollection(UserCredential userCredential, String collection) {
		//permissionValidateCredential(userCredential);

		userCredentialsManager.removeUserCredentialFromCollection(userCredential, collection);

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection)
				.where(usernameMetadata(collection))
				.is(userCredential.getUsername());
		Record userCredentialRecord = searchServices.searchSingleResult(condition);
		recordServices.logicallyDelete(userCredentialRecord, User.GOD);
	}

	public void activateGlobalGroupHierarchy(UserCredential userCredential, GlobalGroup globalGroup) {
		permissionValidateCredential(userCredential);
		activateGlobalGroupHierarchyWithoutUserValidation(globalGroup);
	}

	private void activateGlobalGroupHierarchyWithoutUserValidation(GlobalGroup globalGroup) {
		List<String> collections = collectionsListManager.getCollections();
		restoreGroupHierarchyInBigVault(globalGroup.getCode(), collections);
		globalGroupsManager.activeGlobalGroupHierarchy(globalGroup);
	}

	public void removeUserCredentialAndUser(UserCredential userCredential) {
		userCredential = userCredential.withStatus(UserCredentialStatus.DELETED);
		addUpdateUserCredential(userCredential);
	}

	public void setUserCredentialAndUserStatusPendingApproval(UserCredential userCredential) {

		userCredential = userCredential.withStatus(UserCredentialStatus.PENDING);
		addUpdateUserCredential(userCredential);
	}

	public void suspendUserCredentialAndUser(UserCredential userCredential) {

		userCredential = userCredential.withStatus(UserCredentialStatus.SUPENDED);
		addUpdateUserCredential(userCredential);
	}

	public void activeUserCredentialAndUser(UserCredential userCredential) {

		userCredential = userCredential.withStatus(UserCredentialStatus.ACTIVE);
		addUpdateUserCredential(userCredential);
		List<String> collections = userCredential.getCollections();
		restoreUserInBigVault(userCredential.getUsername(), collections);
	}

	private void restoreUserInBigVault(String username, List<String> collections) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition;
		for (String collection : collections) {
			condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection).where(usernameMetadata(collection))
					.isEqualTo(username).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
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
				condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection).where(groupCodeMetadata(collection))
						.isEqualTo(group.getCode()).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isTrue();
				query.setCondition(condition);
				Record recordGroup = searchServices.searchSingleResult(condition);
				if (recordGroup != null) {
					recordServices.restore(recordGroup, User.GOD);
				}
			}
		}
	}

	public void logicallyRemoveGroupHierarchy(UserCredential userCredential, GlobalGroup globalGroup) {
		permissionValidateCredential(userCredential);
		logicallyRemoveGroupHierarchyWithoutUserValidation(globalGroup);

	}

	private void logicallyRemoveGroupHierarchyWithoutUserValidation(GlobalGroup globalGroup) {
		//		List<String> collections = globalGroup.getUsersAutomaticallyAddedToCollections();
		List<String> collections = collectionsListManager.getCollections();
		List<UserCredential> users = getGlobalGroupActifUsers(globalGroup.getCode());

		userCredentialsManager.removeGroup(globalGroup.getCode());
		globalGroupsManager.logicallyRemoveGroup(globalGroup);
		removeGroupFromCollectionsWithoutUserValidation(globalGroup.getCode(), collections);

		syncUsersCredentials(users);
	}

	public void removeGroupFromCollections(UserCredential userCredential, String group, List<String> collections) {
		permissionValidateCredential(userCredential);
		removeGroupFromCollectionsWithoutUserValidation(group, collections);
		//		removeChildren(group, collections);
		//		removeFromBigVault(group, collections);
	}

	private void removeGroupFromCollectionsWithoutUserValidation(String group, List<String> collections) {
		removeChildren(group, collections);
		removeFromBigVault(group, collections);
	}

	private void removeChildren(String group, List<String> collections) {
		for (String collection : collections) {
			for (Group child : getChildrenOfGroupInCollection(group, collection)) {
				removeFromBigVault(child.getCode(), collections);
				removeChildren(child.getCode(), collections);
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

	public void givenSystemAdminPermissionsToUser(UserCredential user) {
		addUpdateUserCredential(user.withSystemAdminPermission());
	}

	public String giveNewServiceToken(UserCredential user) {
		UserCredential modifiedUser = user.withNewServiceKey();
		addUpdateUserCredential(modifiedUser);
		return modifiedUser.getServiceKey();
	}

	public void sync(UserCredential user) {
		for (String collection : user.getCollections()) {
			Transaction transaction = new Transaction();
			sync(user, collection, transaction);
			try {
				recordServices.execute(transaction);
			} catch (RecordServicesException e) {
				throw new UserServicesRuntimeException_CannotExcuteTransaction(e);
			}
		}
	}

	public boolean canAddOrModifyUserAndGroup() {
		if (ldapConfigurationManager.isLDAPAuthentication()
				&& ldapConfigurationManager.idUsersSynchActivated() != null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean canModifyPassword(UserCredential currentUser) {
		if (!currentUser.getUsername().equals("admin") && (ldapConfigurationManager.isLDAPAuthentication()
				|| ldapConfigurationManager.idUsersSynchActivated() != null)) {
			return false;
		} else {
			return true;
		}
	}

	private List<String> toUserNames(List<UserCredential> users) {
		List<String> usernames = new ArrayList<>();
		for (UserCredential user : users) {
			usernames.add(user.getUsername());
		}
		return usernames;
	}

	private void sync(UserCredential user, String collection, Transaction transaction) {
		User userInCollection = getUserInCollection(user.getUsername(), collection);
		if (userInCollection == null) {
			userInCollection = newUserInCollection(collection);
		} else {
			userInCollection.set("deleted", false);
		}
		userInCollection.setEmail(StringUtils.isBlank(user.getEmail()) ? null : user.getEmail());
		userInCollection.setFirstName(user.getFirstName());
		userInCollection.setLastName(user.getLastName());
		userInCollection.setUsername(user.getUsername());
		userInCollection.setSystemAdmin(user.isSystemAdmin());
		changeUserStatus(userInCollection, user);
		List<String> groupIds = getGroupIds(user.getGlobalGroups(), collection);
		List<String> UserInCollectionGroupIds = userInCollection.getUserGroups();
		if (!hasSameGroups(groupIds, UserInCollectionGroupIds)) {
			userInCollection.setUserGroups(groupIds);
		}
		if (userInCollection.isDirty()) {
			transaction.add(userInCollection.getWrappedRecord());
		}
	}

	private void changeUserStatus(User userInCollection, UserCredential userCredential) {
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

	List<String> getGroupIds(List<String> groupCodes, String collection) {
		List<String> groupIds = new ArrayList<>();
		for (String groupCode : groupCodes) {
			String groupId = getGroupIdInCollection(groupCode, collection);
			if (groupId == null) {
				throw new ImpossibleRuntimeException("No group with code '" + groupCode + "' in collection '" + collection + "'");
			}
			groupIds.add(groupId);
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

	private MetadataSchemaTypes schemaTypes(String collection) {
		return metadataSchemasManager.getSchemaTypes(collection);
	}

	private MetadataSchema userSchema(String collection) {
		return schemaTypes(collection).getSchema(User.SCHEMA_TYPE + "_default");
	}

	Metadata usernameMetadata(String collection) {
		return userSchema(collection).getMetadata(User.USERNAME);
	}

	Metadata userGroupsMetadata(String collection) {
		return userSchema(collection).getMetadata(User.GROUPS);
	}

	private MetadataSchema groupSchema(String collection) {
		return schemaTypes(collection).getSchema(Group.SCHEMA_TYPE + "_default");
	}

	Metadata groupCodeMetadata(String collection) {
		return groupSchema(collection).getMetadata(Group.CODE);
	}

	Metadata groupParentMetadata(String collection) {
		return groupSchema(collection).getMetadata(Group.PARENT);
	}

	User newUserInCollection(String collection) {
		Record record = recordServices.newRecordWithSchema(userSchema(collection));
		return new User(record, schemaTypes(collection), rolesManager.getCollectionRoles(collection));
	}

	Group newGroupInCollection(String collection) {
		Record record = recordServices.newRecordWithSchema(groupSchema(collection));
		return new Group(record, schemaTypes(collection));
	}

	List<Group> getAllGroupsInCollections(String collection) {
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

	OngoingLogicalSearchCondition allGroups(MetadataSchemaTypes types) {
		MetadataSchema groupSchema = types.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
		return LogicalSearchQueryOperators.from(groupSchema);
	}

	OngoingLogicalSearchConditionWithDataStoreFields allGroupsWhereGlobalGroupFlag(MetadataSchemaTypes types) {
		MetadataSchema groupSchema = types.getSchemaType(Group.SCHEMA_TYPE).getDefaultSchema();
		Metadata isGlobalGroup = groupSchema.getMetadata(Group.IS_GLOBAL);
		return LogicalSearchQueryOperators.from(groupSchema).where(isGlobalGroup);
	}

	private void syncUsersCredentials(List<UserCredential> users) {
		for (UserCredential userCredential : users) {
			UserCredential userCredentialUpdated = getUser(userCredential.getUsername());
			sync(userCredentialUpdated);
		}
	}

	private void removeFromBigVault(String group, List<String> collections) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		LogicalSearchCondition condition;
		for (String collection : collections) {
			condition = LogicalSearchQueryOperators.fromAllSchemasIn(collection).where(groupCodeMetadata(collection))
					.isEqualTo(group);
			query.setCondition(condition);
			Record recordGroup = searchServices.searchSingleResult(condition);
			recordServices.logicallyDelete(recordGroup, User.GOD);
		}
	}

	private void permissionValidateCredential(UserCredential userCredential) {
		if (userCredential == null || !userCredential.isSystemAdmin()) {
			String username = null;
			if (userCredential != null) {
				username = userCredential.getUsername();
			}
			throw new UserServicesRuntimeException_UserPermissionDeniedToDelete(username);
		}
	}

	public void removeUserFromGlobalGroup(String username, String globalGroupCode) {
		UserCredential user = getUser(username);
		List<String> newGlobalGroups = new ArrayList<>();
		newGlobalGroups.addAll(user.getGlobalGroups());
		if (!newGlobalGroups.remove(globalGroupCode)) {
			throw new UserServicesRuntimeException_NoSuchGroup(globalGroupCode);
		}
		addUpdateUserCredential(user.withGlobalGroups(newGlobalGroups));
	}

	public String getToken(String serviceKey, String username, String password) {
		if (authenticationService.authenticate(username, password) && serviceKey
				.equals(getUser(username).getServiceKey())) {
			String token = generateToken(username);
			return token;
		} else {
			throw new UserServicesRuntimeException_InvalidUserNameOrPassword(username);
		}
	}

	public String getToken(String serviceKey, String token) {
		if (userCredentialsManager.getServiceKeyByToken(token) != null) {
			userCredentialsManager.removeToken(token);
			String username = userCredentialsManager.getUserCredentialByServiceKey(serviceKey);
			String newToken = generateToken(username);
			return newToken;
		} else {
			throw new UserServicesRuntimeException_InvalidToken();
		}
	}

	private String generateToken(String username) {
		String token = UUID.randomUUID().toString();
		Map<String, LocalDateTime> tokens = new HashMap<String, LocalDateTime>();
		tokens.put(token, new LocalDateTime().plusMinutes(modelLayerConfiguration.getTokenDuration()));
		UserCredential userCredential = getUser(username).withTokens(tokens);
		userCredentialsManager.addUpdate(userCredential);
		return token;
	}

	void validateAdminIsActive(UserCredential userCredential) {
		if (userCredential.getUsername().equals(ADMIN) && userCredential.getStatus() != UserCredentialStatus.ACTIVE) {
			throw new UserServicesRuntimeException_CannotRemoveAdmin();
		}
	}

	public String getTokenUser(String serviceKey, String token) {
		String retrivedServiceKey = getServiceKeyByToken(token);
		if (retrivedServiceKey == null || !retrivedServiceKey.equals(serviceKey)) {
			throw new UserServicesRuntimeException_InvalidToken();
		}
		return userCredentialsManager.getUserCredentialByServiceKey(serviceKey);
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
		return userCredentialsManager.getUserCredentialByServiceKey(serviceKey);
	}

	public UserCredential getUserCredential(String username) {
		return userCredentialsManager.getUserCredential(username);
	}

	public List<UserCredential> getActifUserCredentials() {
		return userCredentialsManager.getActifUserCredentials();
	}

	public List<UserCredential> getAllUserCredentials() {
		return userCredentialsManager.getUserCredentials();
	}

	public List<Group> getChildrenOfGroupInCollection(String groupParentCode, String collection) {
		List<Group> groups = new ArrayList<>();
		String parentId = getGroupIdInCollection(groupParentCode, collection);
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(groupSchema(collection))
				.where(groupParentMetadata(collection))
				.is(parentId).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull();
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(condition);
		for (Record record : searchServices.search(query)) {
			groups.add(Group.wrapNullable(record, schemaTypes(collection)));
		}
		return groups;
	}

	public CredentialUserPermissionChecker has(String username) {
		List<User> users = new ArrayList<>();
		UserCredential user = getUser(username);
		for (String collection : user.getCollections()) {
			users.add(getUserInCollection(username, collection));
		}
		return new CredentialUserPermissionChecker(users);
	}
}
