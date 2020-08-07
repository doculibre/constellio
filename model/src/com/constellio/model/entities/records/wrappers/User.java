package com.constellio.model.entities.records.wrappers;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.Role;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.security.AuthorizationsServices;
import com.constellio.model.services.security.roles.Roles;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.entities.records.Record.GetMetadataOption.DIRECT_GET_FROM_DTO;
import static com.constellio.model.entities.records.Record.GetMetadataOption.RARELY_HAS_VALUE;
import static com.constellio.model.entities.security.Role.DELETE;
import static com.constellio.model.entities.security.Role.READ;
import static com.constellio.model.entities.security.Role.WRITE;
import static java.util.Arrays.asList;

public class User extends RecordWrapper {
	public static final User GOD = null;
	public static final String SCHEMA_TYPE = "user";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String USERNAME = "username";
	public static final String FIRSTNAME = "firstname";
	public static final String LASTNAME = "lastname";
	public static final String EMAIL = "email";
	public static final String PERSONAL_EMAILS = "personalEmails";
	public static final String GROUPS = "groups";
	public static final String ROLES = "userroles";
	public static final String ALL_ROLES = "allroles";
	//public static final String GROUPS_AUTHORIZATIONS = "groupsauthorizations";
	//public static final String ALL_USER_AUTHORIZATIONS = "alluserauthorizations";
	public static final String USER_TOKENS = "usertokens";
	public static final String AZURE_USER = "azureuser";
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
	public static final String LOGIN_LANGUAGE_CODE = "loginLanguageCode";
	public static final String FAX = "fax";
	public static final String ADDRESS = "address";
	public static final String AGENT_ENABLED = "agentEnabled";
	public static final String DEFAULT_PAGE_LENGTH = "defaultPageLength";
	public static final String USER_DOCUMENT_SIZE_SUM = "userDocumentSizeSum";
	public static final String TAXONOMY_DISPLAY_ORDER = "taxonomyDisplayOrder";
	public static final String DO_NOT_RECEIVE_EMAILS = "doNotReceiveEmails";
	public static final String ENABLE_FACETS_APPLY_BUTTON = "enableFacetsApplyButton";
	public static final String AUTOMATIC_TASK_ASSIGNATION = "automaticTaskAssignation";
	public static final String AUTOMATIC_TASK_ASSIGNATION_WORKFLOWS = "automaticTaskAssignationWorkflows";
	public static final String ASSIGNATION_EMAIL_RECEPTION_DISABLED = "assignationEmailReceptionDisabled";
	public static final String DOMAIN = "domain";
	public static final String MS_EXCHANGE_DELEGATE_LIST = "msExchangeDelegateList";

	private Logger LOGGER = LoggerFactory.getLogger(User.class);

	private transient Roles roles;
	AuthorizationsServices authorizationsServices;

	public User(Record record, MetadataSchemaTypes types, Roles roles) {
		super(record, types, SCHEMA_TYPE);
		this.roles = roles;
		this.authorizationsServices = this.roles.getSchemasRecordsServices().getModelLayerFactory().newAuthorizationsServices();
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

	public String getDomain() {
		return get(DOMAIN);
	}

	public User setDomain(String domain) {
		set(DOMAIN, domain);
		return this;
	}

	public List<String> getMsExchDelegateListBL() {
		return getList(MS_EXCHANGE_DELEGATE_LIST);
	}

	public User setMsExchDelegateListBL(List<String> delegateList) {
		set(MS_EXCHANGE_DELEGATE_LIST, delegateList);
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

	public String getAzureUser() {
		return get(AZURE_USER);
	}

	public User setAzureUser(String azureuser) {
		set(AZURE_USER, azureuser);
		return this;
	}

	public User setLastIPAddress(String value) {
		set(LAST_IP_ADDRESS, value);
		return this;
	}

	public User setLoginLanguageCode(String loginLanguageCode) {
		set(LOGIN_LANGUAGE_CODE, loginLanguageCode);
		return this;
	}

	public String getLoginLanguageCode() {
		return get(LOGIN_LANGUAGE_CODE);
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

	public List<String> getPersonalEmails() {
		return get(PERSONAL_EMAILS);
	}

	public User setPersonalEmails(List<String> emails) {
		set(PERSONAL_EMAILS, emails);
		return this;
	}

	public List<String> getUserGroups() {
		return get(GROUPS);
	}

	public List<String> getUserGroupsOrEmpty() {
		return getList(GROUPS);
	}

	public User setUserGroups(List<String> groups) {
		set(GROUPS, groups);
		return this;
	}

	public User addUserGroups(String... groups) {
		add(GROUPS, groups);
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

	public User addUserRoles(String... roles) {
		add(ROLES, roles);
		return this;
	}

	public boolean hasCollectionReadWriteOrDeleteAccess() {
		return hasCollectionReadAccess() || hasCollectionWriteAccess() || hasCollectionDeleteAccess();
	}

	public boolean hasCollectionReadAccess() {
		Boolean value = get(COLLECTION_READ_ACCESS, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE);
		return value == null ? false : value;
	}

	public User setCollectionReadAccess(boolean access) {
		set(COLLECTION_READ_ACCESS, access);
		return this;
	}

	public boolean hasCollectionWriteAccess() {
		Boolean value = get(COLLECTION_WRITE_ACCESS, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE);
		return value == null ? false : value;
	}

	public User setCollectionWriteAccess(boolean access) {
		set(COLLECTION_WRITE_ACCESS, access);
		return this;
	}

	public boolean hasCollectionDeleteAccess() {
		Boolean value = get(COLLECTION_DELETE_ACCESS, DIRECT_GET_FROM_DTO, RARELY_HAS_VALUE);
		return value == null ? false : value;
	}

	public User setCollectionDeleteAccess(boolean access) {
		set(COLLECTION_DELETE_ACCESS, access);
		return this;
	}

	public boolean hasGlobalAccessToMetadata(Metadata m) {
		if (!m.isSecured()) {
			return true;
		}

		List<String> userAllRole = this.getAllRoles();

		if (userAllRole != null) {
			for (String roleCode : m.getAccessRestrictions().getRequiredReadRoles()) {
				if (userAllRole.contains(roleCode)) {
					return true;
				}
			}
		}

		return false;
	}


	private boolean isAccessRole(String role) {
		return role.equals(Role.READ) || role.equals(Role.WRITE) || role.equals(Role.DELETE);
	}

	private boolean isGroupPresent(List<String> principal) {
		for (String group : this.getUserGroups()) {
			if (principal.contains(group)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasAccessToMetadata(Metadata m, Record record) {

		if (!m.isSecured()) {
			return true;
		}

		List<Authorization> authorizations = authorizationsServices.getRecordAuthorizations(record);
		List<String> roleListFromAuthorization = new ArrayList<>();

		boolean hasCollectionAcces = Boolean.TRUE.equals(this.get(COLLECTION_READ_ACCESS));
		boolean hasAtleastOneAuthorization = false;

		for (Authorization authorization : authorizations) {
			if (authorization.getPrincipals().contains(this.getId()) || isGroupPresent(authorization.getPrincipals())) {
				hasAtleastOneAuthorization = true;
				roleListFromAuthorization.addAll(authorization.getRoles());
			}
		}

		if (!hasCollectionAcces && !hasAtleastOneAuthorization) {
			return false;
		}

		if (m.getAccessRestrictions().getRequiredReadRoles().size() > 0) {
			for (String roleFromauthorization : roleListFromAuthorization) {
				if (m.getAccessRestrictions().getRequiredReadRoles().contains(roleFromauthorization)) {
					return true;
				}
			}

			return hasGlobalAccessToMetadata(m);
		}

		return true;
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

	public String getCollection() {
		return wrappedRecord.getCollection();
	}

	public UserCredentialStatus getStatus() {
		RecordDTO recordDTO = wrappedRecord.getRecordDTO();
		if (recordDTO != null && !wrappedRecord.isDirty()) {
			return UserCredentialStatus.fastConvert((String) recordDTO.getFields().get("status_s"));

		}

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
			checker.permissions = new String[]{permission};
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

	public Authorization getAuthorizationDetail(String id) {
		return roles.getSchemasRecordsServices().getSolrAuthorizationDetails(id);
	}

	public Roles getRolesDetails() {
		return roles;
	}

	public String getFax() {
		return get(FAX);
	}

	public User setFax(String fax) {
		set(FAX, fax);
		return this;
	}

	public String getAddress() {
		return get(ADDRESS);
	}

	public User setAddress(String address) {
		set(ADDRESS, address);
		return this;
	}

	public boolean isAgentEnabled() {
		return get(AGENT_ENABLED);
	}

	public boolean hasGlobalTypeAccess(String typeCode, String access) {
		return roles.getSchemasRecordsServices().getModelLayerFactory().getSecurityTokenManager()
				.hasGlobalTypeAccess(this, typeCode, access);
	}

	public SearchPageLength getDefaultPageLength() {
		return get(DEFAULT_PAGE_LENGTH);
	}

	public User setDefaultPageLength(SearchPageLength defaultPageLength) {
		set(DEFAULT_PAGE_LENGTH, defaultPageLength);
		return this;
	}

	public User getCopyOfOriginalRecord() {
		return User.wrapNullable(wrappedRecord.getCopyOfOriginalRecord(), types, roles);
	}

	public User getUnmodifiableCopyOfOriginalRecord() {
		return User.wrapNullable(wrappedRecord.getUnmodifiableCopyOfOriginalRecord(), types, roles);
	}

	public boolean isActiveUser() {
		return getStatus() == UserCredentialStatus.ACTIVE || getStatus() == null;
	}

	public Double getUserDocumentSizeSum() {
		return get(USER_DOCUMENT_SIZE_SUM);
	}

	public List<String> getTaxonomyDisplayOrder() {
		return get(TAXONOMY_DISPLAY_ORDER);
	}

	public boolean isNotReceivingEmails() {
		return Boolean.TRUE.equals(DO_NOT_RECEIVE_EMAILS);
	}

	public boolean isApplyFacetsEnabled() {
		return Boolean.TRUE.equals(get(ENABLE_FACETS_APPLY_BUTTON));
	}

	public boolean isAutomaticTaskAssignation() {
		return Boolean.TRUE.equals(get(AUTOMATIC_TASK_ASSIGNATION));
	}

	public boolean isAssignationEmailReceptionDisabled() {
		return Boolean.TRUE.equals(get(ASSIGNATION_EMAIL_RECEPTION_DISABLED));
	}

	public List<String> getAutomaticTaskAssignationWorkflows() {
		return getList(AUTOMATIC_TASK_ASSIGNATION_WORKFLOWS);
	}
}
