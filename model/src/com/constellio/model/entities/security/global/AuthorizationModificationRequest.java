package com.constellio.model.entities.security.global;

import static java.util.Arrays.asList;

import java.util.List;

import org.joda.time.LocalDate;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.security.CustomizedAuthorizationsBehavior;

public class AuthorizationModificationRequest {

	final String authorizationId;

	final String collection;

	final LocalDate newStartDate;

	final LocalDate newEndDate;

	final List<String> newPrincipalIds;

	final String recordId;

	final List<String> newAccessAndRoles;

	final CustomizedAuthorizationsBehavior behavior;

	final boolean removedOnRecord;

	final boolean reenabledOnRecord;

	public AuthorizationModificationRequest(String authorizationId, String recordId, String collection) {
		this.authorizationId = authorizationId;
		this.collection = collection;
		this.recordId = recordId;
		this.newStartDate = null;
		this.newEndDate = null;
		this.newPrincipalIds = null;

		this.newAccessAndRoles = null;
		this.behavior = null;
		this.removedOnRecord = false;
		this.reenabledOnRecord = false;
	}

	public AuthorizationModificationRequest(String authorizationId, String collection, String recordId, LocalDate newStartDate,
			LocalDate newEndDate, List<String> newPrincipalIds, List<String> newAccessAndRoles,
			CustomizedAuthorizationsBehavior behavior, boolean removedOnRecord, boolean reenabledOnRecord) {
		this.authorizationId = authorizationId;
		this.collection = collection;
		this.recordId = recordId;
		this.newStartDate = newStartDate;
		this.newEndDate = newEndDate;
		this.newPrincipalIds = newPrincipalIds;

		this.newAccessAndRoles = newAccessAndRoles;
		this.behavior = behavior;
		this.removedOnRecord = removedOnRecord;
		this.reenabledOnRecord = reenabledOnRecord;

	}

	public AuthorizationModificationRequest withNewStartDate(LocalDate newStartDate) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest withNewEndDate(LocalDate newEndDate) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest withNewPrincipalIds(List<String> newPrincipalIds) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest withNewPrincipalIds(String... newPrincipalIds) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				asList(newPrincipalIds), newAccessAndRoles, behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest withNewAccessAndRoles(List<String> newAccessAndRoles) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest withNewAccessAndRoles(String... newAccessAndRoles) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, asList(newAccessAndRoles), behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest withPreferedBehavior(CustomizedAuthorizationsBehavior behavior) {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, removedOnRecord, reenabledOnRecord);
	}

	public AuthorizationModificationRequest removingItOnRecord() {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, true, reenabledOnRecord);
	}

	public AuthorizationModificationRequest reenablingItOnRecord() {
		return new AuthorizationModificationRequest(authorizationId, collection, recordId, newStartDate, newEndDate,
				newPrincipalIds, newAccessAndRoles, behavior, removedOnRecord, true);
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

	public CustomizedAuthorizationsBehavior getBehavior() {
		return behavior;
	}

	public boolean isRemovedOnRecord() {
		return removedOnRecord;
	}

	public boolean isReenabledOnRecord() {
		return reenabledOnRecord;
	}

	public AuthorizationModificationRequest detaching() {
		return withPreferedBehavior(CustomizedAuthorizationsBehavior.DETACH);
	}
}
