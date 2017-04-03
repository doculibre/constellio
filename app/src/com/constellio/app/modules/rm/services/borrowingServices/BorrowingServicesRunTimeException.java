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

	public static class BorrowingServicesRunTimeException_UserWithoutReadAccessToContainer
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_UserWithoutReadAccessToContainer(String username, String containerId) {
			super("User: " + username + " cannot read container :" + containerId);
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

	public static class BorrowingServicesRunTimeException_ContainerIsNotBorrowed
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_ContainerIsNotBorrowed(String containerId) {
			super("Container is not borrowed :" + containerId);
		}
	}

	public static class BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_UserNotAllowedToReturnFolder(String username) {
			super("User not allowed to return folder :" + username);
		}
	}

	public static class BorrowingServicesRunTimeException_UserNotAllowedToReturnContainer
			extends BorrowingServicesRunTimeException {

		public BorrowingServicesRunTimeException_UserNotAllowedToReturnContainer(String username) {
			super("User not allowed to return container :" + username);
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
