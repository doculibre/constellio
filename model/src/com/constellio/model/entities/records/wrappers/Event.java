package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;

public class Event extends RecordWrapper {
	public static final String SCHEMA_TYPE = "event";
	public static final String DEFAULT_SCHEMA = SCHEMA_TYPE + "_default";
	public static final String USERNAME = "username";
	public static final String USER_ROLES = "userRoles";
	public static final String TYPE = "type";
	public static final String IP = "ip";
	public static final String RECORD_ID = "recordIdentifier";
	public static final String DELTA = "delta";
	public static final String EVENT_PRINCIPAL_PATH = "eventPrincipalPath";
	public static final String PERMISSION_DATE_RANGE = "permissionDateRange";
	public static final String PERMISSION_ROLES = "permissionRoles";
	public static final String PERMISSION_USERS = "permissionUsers";
	public static final String REASON = "reason";
	public static final String RECORD_VERSION = "recordVersion";
	
	public Event(Record record, MetadataSchemaTypes types) {
		super(record, types, SCHEMA_TYPE + "_");
	}

	public Event setTitle(String title) {
		super.setTitle(title);
		return this;
	}

	public String getUsername() {
		return get(USERNAME);
	}

	public Event setUsername(String username) {
		set(USERNAME, username);
		return this;
	}

	public Event setUserRoles(String userRoles) {
		set(USER_ROLES, userRoles);
		return this;
	}

	public Event setDelta(String recordUrl) {
		set(DELTA, recordUrl);
		return this;
	}

	public Event setPermissionRoles(String permissionRoles) {
		set(PERMISSION_ROLES, permissionRoles);
		return this;
	}

	public Event setPermissionUsers(String permissionUsers) {
		set(PERMISSION_USERS, permissionUsers);
		return this;
	}

	public Event setPermissionDateRange(String permissionDateRange) {
		set(PERMISSION_DATE_RANGE, permissionDateRange);
		return this;
	}

	public String getType() {
		return get(TYPE);
	}

	public Event setType(String type) {
		set(TYPE, type);
		return this;
	}

	public String getRecordId() {
		return get(RECORD_ID);
	}

	public String getDelta() {
		return get(DELTA);
	}

	public String getUserRoles() {
		return get(USER_ROLES);
	}

	public String getPermissionRoles() {
		return get(PERMISSION_ROLES);
	}

	public String getPermissionUsers() {
		return get(PERMISSION_USERS);
	}

	public String getPermissionDateRange() {
		return get(PERMISSION_DATE_RANGE);
	}

	public Event setRecordId(String recordId) {
		set(RECORD_ID, recordId);
		return this;
	}

	public Event setIp(String ip) {
		set(IP, ip);
		return this;
	}

	public String getEventPrincipalPath() {
		return get(EVENT_PRINCIPAL_PATH);
	}

	public Event setEventPrincipalPath(String eventPrincipalPath) {
		set(EVENT_PRINCIPAL_PATH, eventPrincipalPath);
		return this;
	}

	public String getReason() {
		return get(REASON);
	}

	public Event setReason(String reason) {
		set(REASON, reason);
		return this;
	}

	public String getRecordVersion() {
		return get(RECORD_VERSION);
	}

	public Event setRecordVersion(String recordVersion) {
		set(RECORD_VERSION, recordVersion);
		return this;
	}
}
