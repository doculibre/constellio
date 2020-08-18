package com.constellio.app.ui.entities;

import com.constellio.model.entities.security.Role;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class AuthorizationVO implements Serializable {
	String authId;
	String receivedFromMetadataLabel;
	String receivedFromRecordCaption;
	String negative;
	List<String> users;
	List<String> groups;
	List<String> records;
	List<String> accessRoles;
	List<String> userRoles;
	List<String> userRolesTitles;
	LocalDate startDate;
	LocalDate endDate;
	String sharedBy;
	boolean synched;

	public static AuthorizationVO forUsers(String id) {
		return new AuthorizationVO(
				asList(id), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null, null, null, null, false, null, null, null);
	}

	public static AuthorizationVO forGroups(String id) {
		return new AuthorizationVO(
				new ArrayList<String>(), asList(id), new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null, null, null, null, false, null, null, null);
	}

	public static AuthorizationVO forContent(String id) {
		return new AuthorizationVO(
				new ArrayList<String>(), new ArrayList<String>(), asList(id), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null, null, null, null, false, null, null, null);
	}

	public static AuthorizationVO forShare(String id) {
		return new AuthorizationVO(
				new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>(), null, null, null, id, false, null, null, $("AuthorizationsView.enable"));
	}

	public AuthorizationVO(List<String> users, List<String> groups, List<String> records, List<String> accessRoles,
						   List<String> userRoles, List<String> userRolesTitles, String authId, LocalDate startDate,
						   LocalDate endDate, String sharedBy,
						   boolean synched, String receivedFromMetadataLabel, String receivedFromRecordCaption,
						   String negative) {
		this.users = users;
		this.records = records;
		this.accessRoles = accessRoles;
		this.userRoles = userRoles;
		this.userRolesTitles = userRolesTitles;
		this.authId = authId;
		this.startDate = startDate;
		this.endDate = endDate;
		this.groups = groups;
		this.sharedBy = sharedBy;
		this.synched = synched;
		this.receivedFromMetadataLabel = receivedFromMetadataLabel;
		this.receivedFromRecordCaption = receivedFromRecordCaption;
		this.negative = negative;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getRecords() {
		return records;
	}

	public String getSharedBy() {
		return sharedBy;
	}

	public void setSharedBy(String sharedBy) {
		this.sharedBy = sharedBy;
	}

	public void setRecords(List<String> records) {
		this.records = records;
	}

	public String getRecord() {
		return records.isEmpty() ? null : records.get(0);
	}

	public void setRecord(String record) {
		records = asList(record);
	}

	public List<String> getAccessRoles() {
		return accessRoles;
	}

	public void setAccessRoles(List<String> accessRoles) {
		this.accessRoles = accessRoles;
	}

	public List<String> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<String> userRoles) {
		this.userRoles = userRoles;
	}

	public List<String> getUserRolesTitles() {
		return userRolesTitles;
	}

	public void setUserRolesTitles(List<String> userRolesTitles) {
		this.userRolesTitles = userRolesTitles;
	}

	public String getAuthId() {
		return authId;
	}

	public void setAuthId(String authId) {
		this.authId = authId;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public void setNegative(String negative) {
		this.negative = negative;
	}

	public boolean isSynched() {
		return synched;
	}

	public String getNegative() {
		return negative;
	}

	public String getReceivedFromMetadataLabel() {
		return receivedFromMetadataLabel;
	}

	public AuthorizationVO setReceivedFromMetadataLabel(String receivedFromMetadataLabel) {
		this.receivedFromMetadataLabel = receivedFromMetadataLabel;
		return this;
	}

	public String getReceivedFromRecordCaption() {
		return receivedFromRecordCaption;
	}

	public AuthorizationVO setReceivedFromRecordCaption(String receivedFromRecordCaption) {
		this.receivedFromRecordCaption = receivedFromRecordCaption;
		return this;
	}

	public AuthorizationVO withUsers(String... users) {
		this.users = asList(users);
		return this;
	}

	public AuthorizationVO withGroups(String... groups) {
		this.groups = asList(groups);
		return this;
	}

	public AuthorizationVO on(String... records) {
		this.records = asList(records);
		return this;
	}

	public AuthorizationVO givingReadAccess() {
		this.accessRoles = asList(Role.READ);
		return this;
	}

	public AuthorizationVO givingReadWriteAccess() {
		this.accessRoles = asList(Role.READ, Role.WRITE);
		return this;
	}

	public AuthorizationVO givingReadWriteDeleteAccess() {
		this.accessRoles = asList(Role.READ, Role.WRITE, Role.DELETE);
		return this;
	}

	public AuthorizationVO giving(String... roles) {
		this.userRoles = asList(roles);
		return this;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}

