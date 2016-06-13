package com.constellio.model.entities.security.global;

public class AuthorizationModificationResponse {

	private boolean authorizationDeleted;

	private String idOfAuthorizationCopy;

	public AuthorizationModificationResponse(boolean authorizationDeleted, String idOfAuthorizationCopy) {
		this.authorizationDeleted = authorizationDeleted;
		this.idOfAuthorizationCopy = idOfAuthorizationCopy;
	}

	public boolean isAuthorizationDeleted() {
		return authorizationDeleted;
	}

	public String getIdOfAuthorizationCopy() {
		return idOfAuthorizationCopy;
	}
}
