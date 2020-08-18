package com.constellio.model.entities.records.wrappers;

import com.constellio.model.entities.records.Content;
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
	public static final String SHARED_BY = "sharedBy";
	public static final String REASON = "reason";
	public static final String RECORD_VERSION = "recordVersion";
	public static final String RECEIVER_NAME = "receiverName";
	public static final String TASK = "task";
	public static final String DESCRIPTION = "description";
	public static final String ACCEPTED = "accepted";
	public static final String NEGATIVE_AUTHORIZATION = "negative";
	public static final String BATCH_PROCESS_ID = "batchProcessIdentifier";
	public static final String TOTAL_MODIFIED_RECORD = "totalModifiedRecord";
	public static final String CONTENT = "content";

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

	public String getSharedBy() {
		return get(SHARED_BY);
	}

	public Event setSharedBy(String sharedBy) {
		set(SHARED_BY, sharedBy);
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

	public String getReceiver() {
		return get(RECEIVER_NAME);
	}

	public Event setReceiver(User user) {
		set(RECEIVER_NAME, user);
		return this;
	}

	public String getDescription() {
		return get(DESCRIPTION);
	}

	public Event setDescription(String description) {
		set(DESCRIPTION, description);
		return this;
	}

	public String getTask() {
		return get(TASK);
	}

	public Event setTask(String taskID) {
		set(TASK, taskID);
		return this;
	}

	public Boolean getAccepted() {
		return get(ACCEPTED);
	}

	public Event setAccepted(Boolean isAccepted) {
		set(ACCEPTED, isAccepted);
		return this;
	}

	public Boolean getNegative() {
		return get(NEGATIVE_AUTHORIZATION);
	}

	public Event setNegative(Boolean negative) {
		set(NEGATIVE_AUTHORIZATION, negative);
		return this;
	}

	public String getBatchProcessId() {
		return get(BATCH_PROCESS_ID);
	}

	public Event setBatchProcessId(String batchProcessId) {
		set(BATCH_PROCESS_ID, batchProcessId);
		return this;
	}

	public int getTotalModifiedRecord() {
		return get(TOTAL_MODIFIED_RECORD);
	}

	public Event setTotalModifiedRecord(int totalModifiedRecord) {
		set(TOTAL_MODIFIED_RECORD, totalModifiedRecord);
		return this;
	}

	public Content getContent() {
		return get(CONTENT);
	}

	public Event setContent(Content content) {
		set(CONTENT, content);
		return this;
	}
}
