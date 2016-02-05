package com.constellio.data.dao.services.contents;

public class FileSystemContentDaoRuntimeException extends ContentDaoRuntimeException {

	public FileSystemContentDaoRuntimeException(String message) {
		super(message);
	}

	public FileSystemContentDaoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileSystemContentDaoRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class FileSystemContentDaoRuntimeException_DatastoreFailure extends FileSystemContentDaoRuntimeException {

		public FileSystemContentDaoRuntimeException_DatastoreFailure(Throwable cause) {
			super("Filesystem failure", cause);
		}
	}
}
