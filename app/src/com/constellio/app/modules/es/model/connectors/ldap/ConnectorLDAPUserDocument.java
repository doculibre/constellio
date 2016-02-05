package com.constellio.app.modules.es.model.connectors.ldap;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class ConnectorLDAPUserDocument extends ConnectorDocument<ConnectorLDAPUserDocument> {
	//inhereted to be automatically generated :
	public static final String CONNECTOR = ConnectorDocument.CONNECTOR;
	public static final String CONNECTOR_TYPE = ConnectorDocument.CONNECTOR_TYPE;
	public static final String URL = ConnectorDocument.URL;
	public static final String FETCHED = ConnectorDocument.FETCHED;
	public static final String FETCHED_DATETIME = ConnectorDocument.FETCHED_DATETIME;
	public static final String STATUS = ConnectorDocument.STATUS;
	public static final String SEARCHABLE = ConnectorDocument.SEARCHABLE;
	public static final String FETCH_FREQUENCY = ConnectorDocument.FETCH_FREQUENCY;
	public static final String FETCH_DELAY = ConnectorDocument.FETCH_DELAY;
	public static final String NEXT_FETCH = ConnectorDocument.NEXT_FETCH;
	public static final String NEVER_FETCH = ConnectorDocument.NEVER_FETCH;
	public static final String ERROR_CODE = ConnectorDocument.ERROR_CODE;
	public static final String ERROR_MESSAGE = ConnectorDocument.ERROR_MESSAGE;
	public static final String ERROR_STACK_TRACE = ConnectorDocument.ERROR_STACK_TRACE;
	public static final String ERRORS_COUNT = ConnectorDocument.ERRORS_COUNT;

	public static final String SCHEMA_TYPE = "connectorLdapUserDocument";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static String DISTINGUISHED_NAME = "distinguishedName";
	public static String FIRST_NAME = "firstName";
	public static String LAST_NAME = "lastName";
	public static String USERNAME = "username";
	public static String EMAIL = "email";
	public static String ADDRESS = "address";
	public static String WORK_TITLE = "workTitle";
	public static String TELEPHONE = "telephone";
	public static String DISPLAY_NAME = "displayName";
	public static String COMPANY = "company";
	public static String DEPARTMENT = "department";
	public static String MANAGER = "manager";
	public static String ENABLED = "enabled";

	public ConnectorLDAPUserDocument(Record record,
			MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE);
	}

	@Override
	public List<String> getDefaultMetadata() {
		return new ArrayList<>();
	}

	public String getDistinguishedName() {
		return get(DISTINGUISHED_NAME);
	}

	public ConnectorLDAPUserDocument setDistinguishedName(String distinguishedName) {
		set(DISTINGUISHED_NAME, distinguishedName);
		return this;
	}

	public String getFirstName() {
		return get(FIRST_NAME);
	}

	public ConnectorLDAPUserDocument setFirstName(String firstName) {
		set(FIRST_NAME, firstName);
		return this;
	}

	public String getLastName() {
		return get(LAST_NAME);
	}

	public ConnectorLDAPUserDocument setLastName(String lastName) {
		set(LAST_NAME, lastName);
		return this;
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public ConnectorLDAPUserDocument setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public String getEmail() {
		return get(EMAIL);
	}

	public ConnectorLDAPUserDocument setEmail(String email) {
		set(EMAIL, email);
		return this;
	}

	public String getAddress() {
		return get(ADDRESS);
	}

	public ConnectorLDAPUserDocument setAddress(String address) {
		set(ADDRESS, address);
		return this;
	}

	public String getManager() {
		return get(MANAGER);
	}

	public ConnectorLDAPUserDocument setManager(String manager) {
		set(MANAGER, manager);
		return this;
	}

	public String getWorkTitle() {
		return get(WORK_TITLE);
	}

	public ConnectorLDAPUserDocument setWorkTitle(String title) {
		set(WORK_TITLE, title);
		return this;
	}

	public List<String> getTelephone() {
		return get(TELEPHONE);
	}

	public ConnectorLDAPUserDocument setTelephone(List<String> telephone) {
		set(TELEPHONE, telephone);
		return this;
	}

	public String getDisplayName() {
		return get(DISPLAY_NAME);
	}

	public ConnectorLDAPUserDocument setDisplayName(String displayName) {
		set(DISPLAY_NAME, displayName);
		return this;
	}

	public String getCompany() {
		return get(COMPANY);
	}

	public ConnectorLDAPUserDocument setCompany(String company) {
		set(COMPANY, company);
		return this;
	}

	public String getDepartment() {
		return get(DEPARTMENT);
	}

	public ConnectorLDAPUserDocument setDepartment(String department) {
		set(DEPARTMENT, department);
		return this;
	}

	public Boolean getEnabled() {
		return get(ENABLED);
	}

	public ConnectorLDAPUserDocument setEnabled(Boolean enabled) {
		set(ENABLED, enabled);
		return this;
	}
}