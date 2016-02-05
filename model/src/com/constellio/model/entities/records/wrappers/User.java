package com.constellio.model.entities.records.wrappers;

import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.security.roles.Roles;

public class User extends RecordWrapper {
	public static final User GOD = null;
	public static final String SCHEMA_TYPE = "user";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String USERNAME = "username";
	public static final String FIRSTNAME = "firstname";
	public static final String LASTNAME = "lastname";
	public static final String EMAIL = "email";
	public static final String GROUPS = "groups";
	public static final String ROLES = "userroles";
	public static final String ALL_ROLES = "allroles";
	public static final String GROUPS_AUTHORIZATIONS = "groupsauthorizations";
	public static final String ALL_USER_AUTHORIZATIONS = "alluserauthorizations";
	public static final String USER_TOKENS = "usertokens";
	public static final String COLLECTION_READ_ACCESS = "collectionReadAccess";
	public static final String COLLECTION_WRITE_ACCESS = "collectionWriteAccess";
	public static final String COLLECTION_DELETE_ACCESS = "collectionDeleteAccess";
	public static final String SYSTEM_ADMIN = "systemAdmin";
	public static final String ADMIN = "admin";
	public static final String JOB_TITLE = "jobTitle";
	public static final String PHONE = "phone";
	public static final String LAST_LOGIN = "lastLogin";
	public static final String LAST_IP_ADDRESS = "lastIPAddress";
	public static final String START_TAB = "startTab";
	public static final String DEFAULT_TAB_IN_FOLDER_DISPLAY = "defaultTabInFolderDisplay";
	public static final String DEFAULT_TAXONOMY = "defaultTaxonomy";
	public static final String STATUS = "status";
	public static final String SIGNATURE = "signature";

	private transient Roles roles;

	public User(Record record, MetadataSchemaTypes types, Roles roles) {
		super(record, types, SCHEMA_TYPE);
		this.roles = roles;
	}

	@Override
	public RecordWrapper setTitle(String title) {
		throw new UnsupportedOperationException("Title cannot be set on a user, this metadata is calculated.");
	}

	public static User wrapNullable(Record record, MetadataSchemaTypes types, Roles roles) {
		return record == null ? null : new User(record, types, roles);
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public User setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public String getJobTitle() {
		return get(JOB_TITLE);
	}

	public User setJobTitle(String jobTitle) {
		set(JOB_TITLE, jobTitle);
		return this;
	}

	public String getPhone() {
		return get(PHONE);
	}

	public User setPhone(String phone) {
		set(PHONE, phone);
		return this;
	}

	public String getStartTab() {
		return get(START_TAB);
	}

	public User setStartTab(String startTab) {
		set(START_TAB, startTab);
		return this;
	}

	public String getDefaultTabInFolderDisplay() {
		return get(DEFAULT_TAB_IN_FOLDER_DISPLAY);
	}

	public User setDefaultTabInFolderDisplay(String defaultTab) {
		set(DEFAULT_TAB_IN_FOLDER_DISPLAY, defaultTab);
		return this;
	}

	public String getDefaultTaxonomy() {
		return get(DEFAULT_TAXONOMY);
	}

	public User setDefaultTaxonomy(String defaultTaxonomy) {
		set(DEFAULT_TAXONOMY, defaultTaxonomy);
		return this;
	}

	public String getFirstName() {
		return get(FIRSTNAME);
	}

	public User setFirstName(String firstName) {
		set(FIRSTNAME, firstName);
		return this;
	}

	public String getLastName() {
		return get(LASTNAME);
	}

	public User setLastName(String lastName) {
		set(LASTNAME, lastName);
		return this;
	}

	public User setLastIPAddress(String value) {
		set(LAST_IP_ADDRESS, value);
		return this;
	}

	public String getLastIPAddress() {
		return get(LAST_IP_ADDRESS);
	}

	public User setLastLogin(LocalDateTime value) {
		set(LAST_LOGIN, value);
		return this;
	}

	public LocalDateTime getLastLogin() {
		return get(LAST_LOGIN);
	}

	public String getEmail() {
		return get(EMAIL);
	}

	public User setEmail(String email) {
		set(EMAIL, email);
		return this;
	}

	public List<String> getUserGroups() {
		return get(GROUPS);
	}

	public User setUserGroups(List<String> groups) {
		set(GROUPS, groups);
		return this;
	}

	public List<String> getUserRoles() {
		return getList(ROLES);
	}

	public User setUserRoles(List<String> roles) {
		set(ROLES, roles);
		return this;
	}

	public User setUserRoles(String roles) {
		set(ROLES, asList(roles));
		return this;
	}

	public boolean hasCollectionReadWriteOrDeleteAccess() {
		return hasCollectionReadAccess() || hasCollectionWriteAccess() || hasCollectionDeleteAccess();
	}

	public boolean hasCollectionReadAccess() {
		return getBooleanWithDefaultValue(COLLECTION_READ_ACCESS, false);
	}

	public User setCollectionReadAccess(boolean access) {
		set(COLLECTION_READ_ACCESS, access);
		return this;
	}

	public boolean hasCollectionWriteAccess() {
		return getBooleanWithDefaultValue(COLLECTION_WRITE_ACCESS, false);
	}

	public User setCollectionWriteAccess(boolean access) {
		set(COLLECTION_WRITE_ACCESS, access);
		return this;
	}

	public boolean hasCollectionDeleteAccess() {
		return getBooleanWithDefaultValue(COLLECTION_DELETE_ACCESS, false);
	}

	public User setCollectionDeleteAccess(boolean access) {
		set(COLLECTION_DELETE_ACCESS, access);
		return this;
	}

	public User setCollectionAllAccess(boolean access) {
		setCollectionReadAccess(access);
		setCollectionWriteAccess(access);
		setCollectionDeleteAccess(access);
		return this;
	}

	public User setSystemAdmin(boolean systemAdmin) {
		set(SYSTEM_ADMIN, systemAdmin);
		return this;
	}

	public boolean isSystemAdmin() {
		return getBooleanWithDefaultValue(SYSTEM_ADMIN, false);
	}

	public List<String> getAllRoles() {
		return get(ALL_ROLES);
	}

	public List<String> getGroupsAuthorizations() {
		return get(GROUPS_AUTHORIZATIONS);
	}

	public List<String> getAllUserAuthorizations() {
		return get(ALL_USER_AUTHORIZATIONS);
	}

	public List<String> getUserTokens() {
		List<String> recordTokens = getList(USER_TOKENS);
		List<String> tokens = new ArrayList<String>(recordTokens);
		tokens.add("r_" + getId());
		tokens.add("w_" + getId());
		tokens.add("d_" + getId());
		for (String groupId : getUserGroups()) {
			tokens.add("r_" + groupId);
			tokens.add("w_" + groupId);
			tokens.add("d_" + groupId);
		}
		return tokens;
	}

	public String getCollection() {
		return wrappedRecord.getCollection();
	}

	public UserCredentialStatus getStatus() {
		return get(STATUS);
	}

	public User setStatus(UserCredentialStatus status) {
		set(STATUS, status);
		return this;
	}

	public String getSignature() {
		return get(SIGNATURE);
	}

	public User setSignature(String signature) {
		set(SIGNATURE, signature);
		return this;
	}

	public boolean isDirty() {
		return wrappedRecord.isDirty();
	}

	public String toString() {
		return getUsername();
	}

	public UserPermissionsChecker hasReadAccess() {
		return new AccessUserPermissionsChecker(this, true, false, false);
	}

	public UserPermissionsChecker hasWriteAccess() {
		return new AccessUserPermissionsChecker(this, false, true, false);
	}

	public UserPermissionsChecker hasDeleteAccess() {
		return new AccessUserPermissionsChecker(this, false, false, true);
	}

	public UserPermissionsChecker hasRequiredAccess(String requiredAccess) {
		if (Role.READ.equals(requiredAccess)) {
			return hasReadAccess();

		} else if (Role.WRITE.equals(requiredAccess)) {
			return hasWriteAccess();

		} else if (Role.DELETE.equals(requiredAccess)) {
			return hasDeleteAccess();

		}
		throw new ImpossibleRuntimeException("Invalid access :" + requiredAccess);
	}

	public UserPermissionsChecker hasWriteAndDeleteAccess() {
		return new AccessUserPermissionsChecker(this, false, true, true);
	}

	public UserPermissionsChecker hasAllAccess(List<String> access) {
		return new AccessUserPermissionsChecker(this, access.contains(READ), access.contains(WRITE), access.contains(DELETE));
	}

	public UserPermissionsChecker hasMetadataReadAccess() {
		MetadataAccessUserPermissionsChecker checker = new MetadataAccessUserPermissionsChecker(this, types, roles);
		checker.metadataRead = true;
		return checker;
	}

	public UserPermissionsChecker hasMetadataWriteAccess() {
		MetadataAccessUserPermissionsChecker checker = new MetadataAccessUserPermissionsChecker(this, types, roles);
		checker.metadataWrite = true;
		return checker;
	}

	public UserPermissionsChecker hasMetadataModificationAccess() {
		MetadataAccessUserPermissionsChecker checker = new MetadataAccessUserPermissionsChecker(this, types, roles);
		checker.metadataModification = true;
		return checker;
	}

	public UserPermissionsChecker hasMetadataDeletionAccess() {
		MetadataAccessUserPermissionsChecker checker = new MetadataAccessUserPermissionsChecker(this, types, roles);
		checker.metadataDelete = true;
		return checker;
	}

	public UserPermissionsChecker has(String permission) {

		if (permission == null) {
			return new AlwaysTrueUserPermissionsChecker(this);

		} else {
			RolesUserPermissionsChecker checker = new RolesUserPermissionsChecker(this, types, roles);
			checker.permissions = new String[] { permission };
			return checker;
		}
	}

	public UserPermissionsChecker hasAll(List<String> permissions) {
		return hasAll(permissions.toArray(new String[1]));
	}

	public UserPermissionsChecker hasAll(String... permissions) {

		if (permissions.length == 0) {
			return new AlwaysTrueUserPermissionsChecker(this);

		} else {
			RolesUserPermissionsChecker checker = new RolesUserPermissionsChecker(this, types, roles);
			checker.permissions = permissions;
			checker.anyRoles = false;
			return checker;
		}
	}

	public UserPermissionsChecker hasAny(List<String> permissions) {
		return hasAny(permissions.toArray(new String[1]));
	}

	public UserPermissionsChecker hasAny(String... permissions) {

		if (permissions.length == 0) {
			return new AlwaysTrueUserPermissionsChecker(this);

		} else {
			RolesUserPermissionsChecker checker = new RolesUserPermissionsChecker(this, types, roles);
			checker.permissions = permissions;
			checker.anyRoles = true;
			return checker;
		}
	}

	public boolean hasCollectionAccess(String requiredAccess) {
		if (Role.READ.equals(requiredAccess)) {
			return hasCollectionReadWriteOrDeleteAccess();

		} else if (Role.WRITE.equals(requiredAccess)) {
			return hasCollectionWriteAccess();

		} else if (Role.DELETE.equals(requiredAccess)) {
			return hasCollectionDeleteAccess();

		} else {
			return false;
		}

	}

}
