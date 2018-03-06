package com.constellio.model.extensions.params;

public class CanAuthenticateUsingPasswordFileIfLDAPFailedParams {

	String username;

	public CanAuthenticateUsingPasswordFileIfLDAPFailedParams(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}
}
