package com.constellio.model.services.contents;

import com.constellio.model.entities.records.wrappers.User;

public class ContentImplRuntimeException extends RuntimeException {

	public ContentImplRuntimeException(String message) {
		super(message);
	}

	public ContentImplRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentImplRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ContentImplRuntimeException_ContentMustBeCheckedOut extends ContentImplRuntimeException {

		public ContentImplRuntimeException_ContentMustBeCheckedOut(String id) {
			super("Content with id '" + id + "' must be checked out");
		}
	}

	public static class ContentImplRuntimeException_ContentMustNotBeCheckedOut extends ContentImplRuntimeException {

		public ContentImplRuntimeException_ContentMustNotBeCheckedOut(String id) {
			super("Content with id '" + id + "' must not be checked out");
		}
	}

	public static class ContentImplRuntimeException_InvalidArgument extends ContentImplRuntimeException {

		public ContentImplRuntimeException_InvalidArgument(String argument) {
			super("Argument '" + argument + "' is invalid.");
		}
	}

	public static class ContentImplRuntimeException_NoSuchVersion extends ContentImplRuntimeException {

		public ContentImplRuntimeException_NoSuchVersion(String version) {
			super("No such version '" + version + "'");
		}
	}

	public static class ContentImplRuntimeException_VersionMustBeHigherThanPreviousVersion extends ContentImplRuntimeException {

		public ContentImplRuntimeException_VersionMustBeHigherThanPreviousVersion(String givenVersion, String previousVersion) {
			super("Bad version '" + givenVersion + "'. Must be higher than previous version '" + previousVersion + "'");
		}
	}

	public static class ContentImplRuntimeException_UserHasNoDeleteVersionPermission extends ContentImplRuntimeException {

		public ContentImplRuntimeException_UserHasNoDeleteVersionPermission(User user) {
			super("User '" + user.getUsername() + "' has no delete version permission");
		}
	}

	public static class ContentImplRuntimeException_CannotDeleteLastVersion extends ContentImplRuntimeException {

		public ContentImplRuntimeException_CannotDeleteLastVersion() {
			super("Cannot delete last version of content");
		}
	}

}
