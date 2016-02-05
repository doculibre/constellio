package com.constellio.data.io.streamFactories;

@SuppressWarnings("serial")
public class CopyInputStreamFactoryRuntimeException extends RuntimeException {

	public CopyInputStreamFactoryRuntimeException() {
	}

	public CopyInputStreamFactoryRuntimeException(String message) {
		super(message);
	}

	public CopyInputStreamFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public CopyInputStreamFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotWriteContentInTempFile extends CopyInputStreamFactoryRuntimeException {

		public CannotWriteContentInTempFile(String tempFilePath, Throwable cause) {
			super("Cannot write content in file : " + tempFilePath, cause);
		}

	}

}
