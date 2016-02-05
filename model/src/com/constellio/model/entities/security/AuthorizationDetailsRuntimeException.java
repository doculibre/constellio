package com.constellio.model.entities.security;

public class AuthorizationDetailsRuntimeException extends RuntimeException {

	public AuthorizationDetailsRuntimeException(String message) {
		super(message);
	}

	public AuthorizationDetailsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthorizationDetailsRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class AuthorizationDetailsRuntimeException_RoleRequired extends AuthorizationDetailsRuntimeException {
		public AuthorizationDetailsRuntimeException_RoleRequired() {
			super("At least one role is required to create an authorization detail");
		}
	}

	public static class AuthorizationDetailsRuntimeException_SameCollectionRequired extends AuthorizationDetailsRuntimeException {
		public AuthorizationDetailsRuntimeException_SameCollectionRequired() {
			super("AuthorizationDetail's roles must be in the same collection");
		}
	}

}
