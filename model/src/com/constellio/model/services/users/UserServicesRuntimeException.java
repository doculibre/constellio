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

	public static class UserServicesRuntimeException_AtLeastOneCollectionRequired extends UserServicesRuntimeException {
		public UserServicesRuntimeException_AtLeastOneCollectionRequired(String code) {
			super("At least one collection is required for new user/group '" + code + "'");
		}
	}

	public static class UserServicesRuntimeException_ParentGroupDoesNotExistInCollection extends UserServicesRuntimeException {
		public UserServicesRuntimeException_ParentGroupDoesNotExistInCollection(String code, String parentCode,
																				String collection) {
			super("Parent group with code '" + parentCode + "' does not exist in collection '" + collection + "' for child group '" + code + "'");
		}
	}

	public static class UserServicesRuntimeException_InvalidUsername extends UserServicesRuntimeException {
		public UserServicesRuntimeException_InvalidUsername(String username) {
			super("Username is invalid : '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_UserAlreadyExists extends UserServicesRuntimeException {
		public UserServicesRuntimeException_UserAlreadyExists(String username) {
			super("Username already exist in '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_InvalidCollectionForUser extends UserServicesRuntimeException {
		public UserServicesRuntimeException_InvalidCollectionForUser(String username, String collection) {
			super("Cannot add user in this invalid collection name '" + collection + "'");
		}
	}

	public static class UserServicesRuntimeException_InvalidCollectionForGroup extends UserServicesRuntimeException {
		public UserServicesRuntimeException_InvalidCollectionForGroup(String username, String collection) {
			super("Cannot add group in this invalid collection name '" + collection + "'");
		}
	}

	public static class UserServicesRuntimeException_FirstNameRequired extends UserServicesRuntimeException {
		public UserServicesRuntimeException_FirstNameRequired(String username) {
			super("First name is required for new user '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_LastNameRequired extends UserServicesRuntimeException {
		public UserServicesRuntimeException_LastNameRequired(String username) {
			super("Last name is required for new user '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_EmailRequired extends UserServicesRuntimeException {
		public UserServicesRuntimeException_EmailRequired(String username) {
			super("Email is required for new user '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_NameRequired extends UserServicesRuntimeException {
		public UserServicesRuntimeException_NameRequired(String code) {
			super("Name is required for new group '" + code + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotAssignUserToInexistingGroupInCollection(String username, String code,
																						  String collection) {
			super("Cannot assign '" + username + "' to inexisting group '" + code + "' in collection '" + collection + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotChangeNameOfSyncedUser extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeNameOfSyncedUser(String username) {
			super("Cannot change name of synced user '" + username + "'");
		}
	}


	public static class UserServicesRuntimeException_CannotChangeEmailOfSyncedUser extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeEmailOfSyncedUser(String username) {
			super("Cannot change email of synced user '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotChangeStatusOfSyncedUser extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeStatusOfSyncedUser(String username) {
			super("Cannot change status of synced user '" + username + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotChangeStatusOfSyncedGroup extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeStatusOfSyncedGroup(String groupCode) {
			super("Cannot change status of synced group '" + groupCode + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotChangeNameOfSyncedGroup extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeNameOfSyncedGroup(String groupCode) {
			super("Cannot change name of synced group '" + groupCode + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotChangeParentOfSyncedGroup extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeParentOfSyncedGroup(String groupCode) {
			super("Cannot change parent of synced group '" + groupCode + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotChangeAssignmentOfSyncedUserToSyncedGroup(String username,
																							String group) {
			super("Cannot change assignment of synced user '" + username + "' to synced group '" + group + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotRemoveUserFromSyncedCollection(String username, String collection) {
			super("Cannot remove synced user '" + username + "' from synced collection '" + collection + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotRemoveSyncedGroupFromSyncedCollection extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotRemoveSyncedGroupFromSyncedCollection(String groupCode,
																						String collection) {
			super("Cannot remove synced group '" + groupCode + "' from synced collection '" + collection + "'");
		}
	}

	public static class UserServicesRuntimeException_CannotAssignUserToGroupsInOtherCollection extends UserServicesRuntimeException {
		public UserServicesRuntimeException_CannotAssignUserToGroupsInOtherCollection(String username, String code,
																					  String collection) {
			super("User '" + username + "' cannot be assigned to group '" + code + "' in collection '" + collection + "', because the user is not in this collection");
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
