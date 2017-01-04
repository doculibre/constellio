package com.constellio.model.entities.security.global;

import java.util.Map;

public class AuthorizationModificationResponse {

	private boolean authorizationDeleted;

	private String idOfAuthorizationCopy;

	private Map<String, String> idOfAuthorizationCopies;

	public AuthorizationModificationResponse(boolean authorizationDeleted, String idOfAuthorizationCopy,
			Map<String, String> idOfAuthorizationCopies) {
		this.authorizationDeleted = authorizationDeleted;
		this.idOfAuthorizationCopy = idOfAuthorizationCopy;
		this.idOfAuthorizationCopies = idOfAuthorizationCopies;
	}

	public boolean isAuthorizationDeleted() {
		return authorizationDeleted;
	}

	public String getIdOfAuthorizationCopy() {
		return idOfAuthorizationCopy;
	}

//	public Map<String, String> getIdOfAuthorizationCopies() {
	//		return idOfAuthorizationCopies;
	//	}
	//
	//	public String getIdOfAuthorizationCopy(String originalId) {
//		return idOfAuthorizationCopies.get(originalId);
//	}
}
