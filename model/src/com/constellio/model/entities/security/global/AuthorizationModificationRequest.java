package com.constellio.model.entities.security.global;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.Authorization;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;

public class AuthorizationModificationRequest {

	final String authorizationId;

	final String collection;

	final LocalDate newStartDate;

	final LocalDate newEndDate;

	final List<String> newPrincipalIds;

	final String recordId;

	final List<String> newAccessAndRoles;

	final boolean removedOnRecord;

	public AuthorizationModificationRequest(String authorizationId, String collection, String recordId) {
		this.authorizationId = authorizationId;
		this.collection = collection;
		this.recordId = recordId;
		this.newStartDate = null;
		this.newEndDate = null;
		this.newPrincipalIds = null;

		this.newAccessAndRoles = null;
		this.removedOnRecord = false;
	}

	public AuthorizationModificationRequest(String authorizationId, String collection, String recordId, LocalDate newStartDate,
			LocalDate newEndDate, List<String> newPrincipalIds, List<String> newAccessAndRoles,
			boolean removedOnRecord) {
		this.authorizationId = authorizationId;
		this.collection = collection;
		this.recordId = recordId;
		this.newStartDate = newStartDate;
		this.newEndDate = newEndDate;
		this.newPrincipalIds = newPrincipalIds;
		this.newAccessAndRoles = newAccessAndRoles;
		this.removedOnRecord = removedOnRecord;

	}

	public AuthorizationModificationRequest withNewStartDate(LocalDate newStartDate) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord);
	}

	public AuthorizationModificationRequest withNewEndDate(LocalDate newEndDate) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord);
	}

	public AuthorizationModificationRequest withNewPrincipalIds(List<String> newPrincipalIds) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord);
	}

	public AuthorizationModificationRequest withNewPrincipalIds(String... newPrincipalIds) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				asList(newPrincipalIds), newAccessAndRoles, removedOnRecord);
	}

	public AuthorizationModificationRequest withNewAccessAndRoles(List<String> newAccessAndRoles) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, removedOnRecord);
	}

	public AuthorizationModificationRequest withNewAccessAndRoles(String... newAccessAndRoles) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, asList(newAccessAndRoles), removedOnRecord);
	}

	public AuthorizationModificationRequest removingItOnRecord() {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, true);
	}

	public static AuthorizationModificationRequest modifyAuthorization(Authorization authorization) {
		if (authorization == null) {
			throw new IllegalArgumentException("Authorization required");
		}
		return new AuthorizationModificationRequest(authorization.getDetail().getId(), authorization.getDetail().getCollection(),
				authorization.getGrantedOnRecord());
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(Authorization authorization, Record record) {
		if (authorization == null) {
			throw new IllegalArgumentException("Authorization required");
		}
		if (record == null) {
			throw new IllegalArgumentException("Record required");
		}
		return new AuthorizationModificationRequest(authorization.getDetail().getId(), record.getCollection(), record.getId());
	}

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(Authorization authorization, String record) {
		if (authorization == null) {
			throw new IllegalArgumentException("Authorization required");
		}
		if (record == null) {
			throw new IllegalArgumentException("Record required");
		}
		return new AuthorizationModificationRequest(authorization.getDetail().getId(), authorization.getDetail().getCollection(),
				record);
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

	public static AuthorizationModificationRequest modifyAuthorizationOnRecord(String authorizationId, String collection,
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

}
