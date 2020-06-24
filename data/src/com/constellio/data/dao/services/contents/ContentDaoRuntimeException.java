package com.constellio.data.dao.services.contents;

public class ContentDaoRuntimeException extends RuntimeException {

	public ContentDaoRuntimeException(String message) {
		super(message);
	}

	public ContentDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ContentDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ContentDaoRuntimeException_CannotDeleteFolder extends ContentDaoRuntimeException {
		public ContentDaoRuntimeException_CannotDeleteFolder(String id, Throwable cause) {
			super("Cannot delete folder '" + id + "'", cause);
		}
	}

	public static class ContentDaoRuntimeException_NoSuchFolder extends ContentDaoRuntimeException {
		public ContentDaoRuntimeException_NoSuchFolder(String id) {
			super("Cannot delete folder '" + id + "'");
		}
	}

	public static class ContentDaoRuntimeException_CannotMoveFolderTo extends ContentDaoRuntimeException {
		public ContentDaoRuntimeException_CannotMoveFolderTo(String folderId, String newFolderId, Throwable cause) {
			super("Cannot move folder '" + folderId + "' to '" + newFolderId + "'", cause);
		}
	}

	public static class ContentDaoRuntimeException_WriteCancelled extends ContentDaoRuntimeException {
		public ContentDaoRuntimeException_WriteCancelled(String id) {
			super("Write of '" + id + "' was cancelled");
		}
	}
}

