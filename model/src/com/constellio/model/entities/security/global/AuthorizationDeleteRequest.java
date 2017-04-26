package com.constellio.model.entities.security.global;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.Authorization;

public class AuthorizationDeleteRequest {

	final User executedBy;

	final String collection;

	final String authId;

	final boolean reattachIfLastAuthDeleted;

	public AuthorizationDeleteRequest(String authId, String collection, User executedBy,
			boolean reattachIfLastAuthDeleted) {
		this.collection = collection;
		this.authId = authId;
		this.executedBy = executedBy;
		this.reattachIfLastAuthDeleted = reattachIfLastAuthDeleted;
	}

	public AuthorizationDeleteRequest setReattachIfLastAuthDeleted(boolean reattachIfLastAuthDeleted) {
		return new AuthorizationDeleteRequest(authId, collection, executedBy, reattachIfLastAuthDeleted);
	}

	public AuthorizationDeleteRequest setExecutedBy(User executedBy) {
		return new AuthorizationDeleteRequest(authId, collection, executedBy, reattachIfLastAuthDeleted);
	}

	public static AuthorizationDeleteRequest authorizationDeleteRequest(String authId, String collection) {
		return new AuthorizationDeleteRequest(authId, collection, null, true);
	}

	public static AuthorizationDeleteRequest authorizationDeleteRequest(Authorization authorization) {
		return authorizationDeleteRequest(authorization.getDetail());
	}

	public static AuthorizationDeleteRequest authorizationDeleteRequest(AuthorizationDetails authorizationDetails) {
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

	public boolean isReattachIfLastAuthDeleted() {
		return reattachIfLastAuthDeleted;
	}
}
