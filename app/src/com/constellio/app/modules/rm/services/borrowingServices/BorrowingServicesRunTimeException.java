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
package com.constellio.app.modules.rm.services.borrowingServices;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

public class BorrowingServicesRunTimeException extends RuntimeException {

	public BorrowingServicesRunTimeException(String message) {
		super(message);
	}

	public BorrowingServicesRunTimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_FolderIsAlreadyBorrowed(String folderId) {
			super("Folder already borrowed :" + folderId);
		}
	}

	public static class BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_UserWithoutReadAccessToFolder(String username, String folderId) {
			super("User: " + username + " cannot read folder :" + folderId);
		}
	}

	public static class BorrowingServicesRunTimeException_InvalidPreviewReturnDate
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_InvalidPreviewReturnDate(LocalDateTime previewReturnDate) {
			super("Invalid preview return date :" + previewReturnDate);
		}
	}

	public static class BorrowingServicesRunTimeException_InvalidBorrowingDate
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_InvalidBorrowingDate(LocalDate date) {
			super("Borrowing date cannot be in the future :" + date);
		}
	}

	public static class BorrowingServicesRunTimeException_CannotBorrowActiveFolder
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_CannotBorrowActiveFolder(String folderId) {
			super("Cannot borrow active folder :" + folderId);
		}
	}

	public static class BorrowingServicesRunTimeException_FolderIsNotBorrowed
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_FolderIsNotBorrowed(String folderId) {
			super("Folder is not borrowed :" + folderId);
		}
	}

	public static class BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder(String username) {
			super("User not allowed to return folder :" + username);
		}
	}

	public static class BorrowingServicesRunTimeException_ContainerIsAlreadyBorrowed
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_ContainerIsAlreadyBorrowed(String containerId) {
			super("Container already borrowed :" + containerId);
		}
	}

	public static class BorrowingServicesRunTimeException_RecordServicesException
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_RecordServicesException(Exception e) {
			super("Cannot execute transaction", e);
		}
	}
}
