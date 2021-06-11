package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;

public class AuthorizationDeleteRequest {

	final User executedBy;

	final String collection;

	final String authId;

	public AuthorizationDeleteRequest(String authId, String collection, User executedBy) {
		this.collection = collection;
		this.authId = authId;
		this.executedBy = executedBy;
	}

	public AuthorizationDeleteRequest setExecutedBy(User executedBy) {
		return new AuthorizationDeleteRequest(authId, collection, executedBy);
	}

	public static AuthorizationDeleteRequest authorizationDeleteRequest(String authId, String collection) {
		return new AuthorizationDeleteRequest(authId, collection, null);
	}

	public static AuthorizationDeleteRequest authorizationDeleteRequest(Authorization authorizationDetails) {
		return authorizationDeleteRequest(authorizationDetails.getId(), authorizationDetails.getCollection());
	}

	public User getExecutedBy() {
		return executedBy;
	}

	public String getCollection() {
		return collection;
	}

	public String getAuthId() {
		return authId;
	}
}
