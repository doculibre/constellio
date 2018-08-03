package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapper;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;
import org.joda.time.LocalDate;

import java.util.List;

import static java.util.Arrays.asList;

public class AuthorizationModificationRequest {

	final String authorizationId;

	final String collection;

	final LocalDate newStartDate;

	final LocalDate newEndDate;

	final List<String> newPrincipalIds;

	final String recordId;

	final List<String> newAccessAndRoles;

	final boolean removedOnRecord;

	final User executedBy;

	final Boolean newOverridingInheritedAuths;

	public AuthorizationModificationRequest(String authorizationId, String collection, String recordId) {
		this.authorizationId = authorizationId;
		this.collection = collection;
		this.recordId = recordId;
		this.newStartDate = null;
		this.newEndDate = null;
		this.newPrincipalIds = null;

		this.newAccessAndRoles = null;
		this.removedOnRecord = false;
		this.executedBy = null;
		this.newOverridingInheritedAuths = null;
	}

	public AuthorizationModificationRequest(String authorizationId, String collection, String recordId,
											LocalDate newStartDate,
											LocalDate newEndDate, List<String> newPrincipalIds,
											List<String> newAccessAndRoles,
											boolean removedOnRecord, User executedBy,
											Boolean newOverridingInheritedAuths) {
		this.authorizationId = authorizationId;
		this.collection = collection;
		this.recordId = recordId;
		this.newStartDate = newStartDate;
		this.newEndDate = newEndDate;
		this.newPrincipalIds = newPrincipalIds;
		this.newAccessAndRoles = newAccessAndRoles;
		this.removedOnRecord = removedOnRecord;
		this.executedBy = executedBy;
		this.newOverridingInheritedAuths = newOverridingInheritedAuths;

	}

	public AuthorizationModificationRequest withNewStartDate(LocalDate newStartDate) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest withNewEndDate(LocalDate newEndDate) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest withNewPrincipalIds(List<String> newPrincipalIds) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest withNewPrincipalIds(String... newPrincipalIds) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				asList(newPrincipalIds), newAccessAndRoles, removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest withNewAccessAndRoles(List<String> newAccessAndRoles) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest withNewAccessAndRoles(String... newAccessAndRoles) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, asList(newAccessAndRoles), removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest removingItOnRecord() {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, true, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest setExecutedBy(User executedBy) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord, executedBy, newOverridingInheritedAuths);
	}

	public AuthorizationModificationRequest withNewOverridingInheritedAuths(boolean newValue) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord, executedBy, newValue);
	}

	public static AuthorizationModificationRequest modifyAuthorization(Authorization authorization) {
		if (authorization == null) {
			throw new IllegalArgumentException("Authorization required");
		}
		return new AuthorizationModificationRequest(authorization.getDetail().getId(), authorization.getDetail().getCollection(),
				authorization.getGrantedOnRecord());
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(Authorization authorization,
																			   Record record) {
		if (authorization == null) {
			throw new IllegalArgumentException("Authorization required");
		}
		if (record == null) {
			throw new IllegalArgumentException("Record required");
		}
		return new AuthorizationModificationRequest(authorization.getDetail().getId(), record.getCollection(), record.getId());
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(Authorization authorization,
																			   String record) {
		if (authorization == null) {
			throw new IllegalArgumentException("Authorization required");
		}
		if (record == null) {
			throw new IllegalArgumentException("Record required");
		}
		return new AuthorizationModificationRequest(authorization.getDetail().getId(), authorization.getDetail().getCollection(),
				record);
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(String authorizationId,
																			   RecordWrapper recordWrapper) {
		if (authorizationId == null) {
			throw new IllegalArgumentException("Authorization id required");
		}
		if (recordWrapper == null) {
			throw new IllegalArgumentException("Record required");
		}
		return new AuthorizationModificationRequest(authorizationId, recordWrapper.getCollection(), recordWrapper.getId());
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(String authorizationId, Record record) {
		if (authorizationId == null) {
			throw new IllegalArgumentException("Authorization id required");
		}
		if (record == null) {
			throw new IllegalArgumentException("Record required");
		}
		return new AuthorizationModificationRequest(authorizationId, record.getCollection(), record.getId());
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(String authorizationId,
																			   String collection,
																			   String recordId) {
		if (authorizationId == null) {
			throw new IllegalArgumentException("Authorization id required");
		}
		if (collection == null) {
			throw new IllegalArgumentException("Collection required");
		}
		if (recordId == null) {
			throw new IllegalArgumentException("Record id required");
		}
		return new AuthorizationModificationRequest(authorizationId, collection, recordId);
	}

	public String getAuthorizationId() {
		return authorizationId;
	}

	public String getCollection() {
		return collection;
	}

	public LocalDate getNewStartDate() {
		return newStartDate;
	}

	public LocalDate getNewEndDate() {
		return newEndDate;
	}

	public List<String> getNewPrincipalIds() {
		return newPrincipalIds;
	}

	public String getRecordId() {
		return recordId;
	}

	public List<String> getNewAccessAndRoles() {
		return newAccessAndRoles;
	}

	public boolean isRemovedOnRecord() {
		return removedOnRecord;
	}

	public User getExecutedBy() {
		return executedBy;
	}

	public Boolean getNewOverridingInheritedAuths() {
		return newOverridingInheritedAuths;
	}
}
