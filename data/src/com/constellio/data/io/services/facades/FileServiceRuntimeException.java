package com.constellio.data.io.services.facades;

@SuppressWarnings("serial")
public class FileServiceRuntimeException extends RuntimeException {

	public FileServiceRuntimeException() {
	}

	public FileServiceRuntimeException(String message) {
		super(message);
	}

	public FileServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public FileServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCopyFile extends FileServiceRuntimeException {

		public CannotCopyFile(String src, String dest, Throwable t) {
			super("Cannot copy '" + src + "' to '" + dest + "'", t);
		}
	}

	public static class CannotMoveFile extends FileServiceRuntimeException {

		public CannotMoveFile(String src, String dest, Throwable t) {
			super("Cannot move '" + src + "' to '" + dest + "'", t);
		}
	}

	public static class CannotDeleteFile extends FileServiceRuntimeException {

		public CannotDeleteFile(String filePath, Throwable t) {
			super("Cannot delete '" + filePath + "'", t);
		}
	}

	public static class CannotCreateTemporaryFolder extends FileServiceRuntimeException {

		public CannotCreateTemporaryFolder(Throwable t) {
			super("Cannot create temporary folder", t);
		}

		public CannotCreateTemporaryFolder() {
			super("Cannot create temporary folder");
		}
	}

	public static class CannotReadStreamToString extends FileServiceRuntimeException {

		public CannotReadStreamToString(Throwable t) {
			super("Cannot read stream to string", t);
		}
	}

	public static class FileServiceRuntimeException_CannotReadFile extends FileServiceRuntimeException {

		public FileServiceRuntimeException_CannotReadFile(String filePath, Throwable t) {
			super("Cannot read file '" + filePath + "'", t);
		}
	}

}
