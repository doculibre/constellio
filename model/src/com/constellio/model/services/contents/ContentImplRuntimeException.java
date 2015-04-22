/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
