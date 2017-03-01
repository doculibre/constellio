package com.constellio.model.services.users;

@SuppressWarnings("serial")
public class UserServicesRuntimeException extends RuntimeException {

	public UserServicesRuntimeException() {
		super();
	}

	public UserServicesRuntimeException(String message) {
		super(message);
	}

	public UserServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public UserServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class UserServicesRuntimeException_UserIsNotInCollection extends UserServicesRuntimeException {
		public UserServicesRuntimeException_UserIsNotInCollection(String username, String collection) {
			super("User '" + username + "' is not in collection '" + collection + "'");
		}
	}

	public static class UserServicesRuntimeException_NoSuchUser extends UserServicesRuntimeException {
		public UserServicesRuntimeException_NoSuchUser(String username) {
			super("No such user '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_NoSuchGroup extends UserServicesRuntimeException {
		public UserServicesRuntimeException_NoSuchGroup(String groupCode) {
			super("No such group '" + groupCode + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotExcuteTransaction extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotExcuteTransaction(Exception e) {
			super("Cannot excute transaction", e);
		}
	}

	public static class UserServicesRuntimeException_CannotRemoveAdmin extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotRemoveAdmin() {
			super("Cannot remove admin");
		}
	}

	public static class UserServicesRuntimeException_UserPermissionDeniedToDelete extends UserServicesRuntimeException {
		public UserServicesRuntimeException_UserPermissionDeniedToDelete(String user) {
			super("User " + user + " has not permission to delete");
		}
	}

	public static class UserServicesRuntimeException_InvalidUserNameOrPassword extends UserServicesRuntimeException {
		public UserServicesRuntimeException_InvalidUserNameOrPassword(String username) {
			super("Invalid username " + username + " or password");
		}
	}

	public static class UserServicesRuntimeException_InvalidToken extends UserServicesRuntimeException {
		public UserServicesRuntimeException_InvalidToken() {
			super("Invalid token");
		}
	}

	public static class UserServicesRuntimeException_CannotSafeDeletePhysically extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotSafeDeletePhysically(String username) {
			super(username.equals("chuck") ? "You cannot delete chuck, chuck deletes you." : "Cannot delete " + username + " since it's been doing things.");
		}
	}

	public static class UserServicesRuntimeException_InvalidGroup extends UserServicesRuntimeException {
		private final String groupCode;

		public UserServicesRuntimeException_InvalidGroup(String groupCode) {
			super("Invalid group code " + groupCode);
			this.groupCode = groupCode;
		}

		public String getGroupCode() {
			return groupCode;
		}
	}
}
