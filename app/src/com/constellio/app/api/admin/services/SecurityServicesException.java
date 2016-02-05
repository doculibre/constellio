package com.constellio.app.api.admin.services;

public class SecurityServicesException extends RuntimeException {

	public SecurityServicesException() {
		super();
	}

	public SecurityServicesException(String message, Throwable cause) {
		super(message, cause);
	}

	public SecurityServicesException(String message) {
		super(message);
	}

	public SecurityServicesException(Throwable cause) {
		super(cause);
	}

	public static class SecurityServicesException_CannotUpdateGroup extends SecurityServicesException {
		public SecurityServicesException_CannotUpdateGroup(String groupCode, Throwable cause) {
			super("Cannot update group " + groupCode, cause);
		}
	}

	public static class SecurityServicesException_CannotUpdateUser extends SecurityServicesException {
		public SecurityServicesException_CannotUpdateUser(String username, Throwable cause) {
			super("Cannot update user " + username, cause);
		}
	}

}
