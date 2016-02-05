package com.constellio.model.services.users;

public class UserPhotosServicesRuntimeException extends RuntimeException {

	public UserPhotosServicesRuntimeException(String message) {
		super(message);
	}

	public UserPhotosServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserPhotosServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class UserPhotosServicesRuntimeException_UserHasNoPhoto extends UserPhotosServicesRuntimeException {

		public UserPhotosServicesRuntimeException_UserHasNoPhoto(String username) {
			super("User '" + username + "' has no photo");
		}
	}

	public static class UserPhotosServicesRuntimeException_NoSuchUserLog extends UserPhotosServicesRuntimeException {

		public UserPhotosServicesRuntimeException_NoSuchUserLog(String username, String userLog) {
			super("No such log '" + userLog + "' for user '" + username + "'");
		}
	}
}