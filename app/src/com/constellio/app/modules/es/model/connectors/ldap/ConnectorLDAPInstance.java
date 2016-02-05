package com.constellio.app.modules.es.model.connectors.ldap;

import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ldap.enums.DirectoryType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorLDAPInstance extends ConnectorInstance<ConnectorLDAPInstance> {
	public static final String SCHEMA_LOCAL_CODE = "ldap";
	public static final String SCHEMA_CODE = SCHEMA_TYPE + "_" + SCHEMA_LOCAL_CODE;
	public static final String NUMBER_OF_JOBS_IN_PARALLEL = "jobsInParallel";
	public static final String NUMBER_OF_DOCUMENTS_PER_JOB = "documentsPerJob";
	public static final String USER_BASE_CONTEXT_LIST = "usersBaseContextList";

	//connector instance config
	public static String FETCH_GROUPS = "fetchGroups";
	public static String FETCH_COMPUTERS = "fetchComputers";
	public static String FETCH_USERS = "fetchUsers";
	public static String DIRECTORY_TYPE = "directoryType";
	public static String URLS = "url";
	public static String CONNECTION_USERNAME = "connectionUsername";
	public static String FOLLOW_REFERENCES = "followReferences";
	public static String PASSWORD = "password";
	public static final String INCLUDE_REGEX = "includeRegex";
	public static final String EXCLUDE_REGEX = "excludeRegex";
	//user attributes config
	public static String DISTINGUISHED_NAME_ATTRIBUTE_NAME = "dn";
	public static String FIRST_NAME_ATTRIBUTE_NAME = "firstName";
	public static String LAST_NAME_ATTRIBUTE_NAME = "lastName";
	public static String USERNAME_ATTRIBUTE_NAME = "username";
	public static String EMAIL_ATTRIBUTE_NAME = "email";
	public static String ADDRESS_ATTRIBUTE_NAME = "address";

	public static String WORK_TITLE_ATTRIBUTE_NAME = "jobTitle";
	public static String TELEPHONE_ATTRIBUTE_NAME = "telephone";
	public static String DISPLAY_NAME_ATTRIBUTE_NAME = "displayName";
	public static String COMPANY_ATTRIBUTE_NAME = "company";
	public static String DEPARTMENT_ATTRIBUTE_NAME = "department";
	public static String MANAGER_ATTRIBUTE_NAME = "manager";

	public ConnectorLDAPInstance(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_CODE);
	}

	public List<String> getUrls() {
		return get(URLS);
	}

	public ConnectorLDAPInstance setUrls(List<String> urls) {
		set(URLS, urls);
		return this;
	}

	public String getConnectionUsername() {
		return get(CONNECTION_USERNAME);
	}

	public ConnectorLDAPInstance setConnectionUsername(String username) {
		set(CONNECTION_USERNAME, username);
		return this;
	}

	public Boolean getFollowReferences() {
		return get(FOLLOW_REFERENCES);
	}

	public ConnectorLDAPInstance getFollowReferences(Boolean followReferences) {
		set(FOLLOW_REFERENCES, followReferences);
		return this;
	}

	public String getPassword() {
		return get(PASSWORD);
	}

	public ConnectorLDAPInstance setPassword(String password) {
		set(PASSWORD, password);
		return this;
	}

	public String getIncludeRegex() {
		return get(INCLUDE_REGEX);
	}

	public ConnectorLDAPInstance setIncludeRegex(String includeRegex) {
		set(INCLUDE_REGEX, includeRegex);
		return this;
	}

	public String getExcludeRegex() {
		return get(EXCLUDE_REGEX);
	}

	public ConnectorLDAPInstance setExcludeRegex(String excludeRegex) {
		set(EXCLUDE_REGEX, excludeRegex);
		return this;
	}

	public DirectoryType getDirectoryType() {
		return get(DIRECTORY_TYPE);
	}

	public ConnectorLDAPInstance setDirectoryType(DirectoryType directoryType) {
		set(DIRECTORY_TYPE, directoryType);
		return this;
	}

	public Boolean getFetchUsers() {
		return get(FETCH_USERS);
	}

	public ConnectorLDAPInstance setFetchUsers(Boolean fetchUsers) {
		set(FETCH_USERS, fetchUsers);
		return this;
	}

	public Boolean getFetchGroups() {
		return get(FETCH_GROUPS);
	}

	public ConnectorLDAPInstance setFetchGroups(Boolean fetchGroups) {
		set(FETCH_GROUPS, fetchGroups);
		return this;
	}

	public Boolean getFetchComputers() {
		return get(FETCH_COMPUTERS);
	}

	public ConnectorLDAPInstance setFetchComputers(Boolean fetchComputers) {
		set(FETCH_COMPUTERS, fetchComputers);
		return this;
	}

	public String getDistinguishedName() {
		return get(DISTINGUISHED_NAME_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setDistinguishedName(String distinguishedName) {
		set(DISTINGUISHED_NAME_ATTRIBUTE_NAME, distinguishedName);
		return this;
	}

	public String getFirstName() {
		return get(FIRST_NAME_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setFirstName(String firstName) {
		set(FIRST_NAME_ATTRIBUTE_NAME, firstName);
		return this;
	}

	public String getLastName() {
		return get(LAST_NAME_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setLastName(String lastName) {
		set(LAST_NAME_ATTRIBUTE_NAME, lastName);
		return this;
	}

	public String getUsername() {
		return get(USERNAME_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setUsername(String username) {
		set(LAST_NAME_ATTRIBUTE_NAME, username);
		return this;
	}

	public String getEmail() {
		return get(EMAIL_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setEmail(String email) {
		set(EMAIL_ATTRIBUTE_NAME, email);
		return this;
	}

	public List<String> getAddress() {
		return get(ADDRESS_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setAddress(List<String> address) {
		set(ADDRESS_ATTRIBUTE_NAME, address);
		return this;
	}

	public String getManager() {
		return get(MANAGER_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setManager(String manager) {
		set(MANAGER_ATTRIBUTE_NAME, manager);
		return this;
	}

	public String getWorkTitle() {
		return get(WORK_TITLE_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setWorkTitle(String title) {
		set(WORK_TITLE_ATTRIBUTE_NAME, title);
		return this;
	}

	public List<String> getTelephone() {
		return get(TELEPHONE_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setTelephone(List<String> telephone) {
		set(TELEPHONE_ATTRIBUTE_NAME, telephone);
		return this;
	}

	public String getDisplayName() {
		return get(DISPLAY_NAME_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setDisplayName(String displayName) {
		set(DISPLAY_NAME_ATTRIBUTE_NAME, displayName);
		return this;
	}

	public String getCompany() {
		return get(COMPANY_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setCompany(String company) {
		set(COMPANY_ATTRIBUTE_NAME, company);
		return this;
	}

	public String getDepartment() {
		return get(DEPARTMENT_ATTRIBUTE_NAME);
	}

	public ConnectorLDAPInstance setDepartment(String department) {
		set(DEPARTMENT_ATTRIBUTE_NAME, department);
		return this;
	}

	public int getNumberOfJobsInParallel() {
		return getInteger(NUMBER_OF_JOBS_IN_PARALLEL);
	}

	public ConnectorLDAPInstance setNumberOfJobsInParallel(int jobsInParallel) {
		set(NUMBER_OF_JOBS_IN_PARALLEL, jobsInParallel);
		return this;
	}

	public int getDocumentsPerJobs() {
		return getInteger(NUMBER_OF_DOCUMENTS_PER_JOB);
	}

	public ConnectorLDAPInstance setDocumentsPerJobs(int documentsPerJobs) {
		set(NUMBER_OF_DOCUMENTS_PER_JOB, documentsPerJobs);
		return this;
	}

	public List<String> getUsersBaseContextList() {
		return get(USER_BASE_CONTEXT_LIST);
	}

	public ConnectorLDAPInstance setUsersBaseContextList(List<String> contextList) {
		set(USER_BASE_CONTEXT_LIST, contextList);
		return this;
	}
}
