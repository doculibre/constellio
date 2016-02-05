package com.constellio.data.io.services.zip;

@SuppressWarnings("serial")
public class ZipServiceRuntimeException extends RuntimeException {

	public ZipServiceRuntimeException() {
	}

	public ZipServiceRuntimeException(String message) {
		super(message);
	}

	public ZipServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public ZipServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotUnzip extends ZipServiceRuntimeException {

		public CannotUnzip(String zipFilePath, String zipFileContentDestinationDirPath, Throwable t) {
			super("Cannot file: '" + zipFilePath + "' in '" + zipFileContentDestinationDirPath + "'", t);
		}
	}

}
