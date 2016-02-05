package com.constellio.data.io.streamFactories.impl;

@SuppressWarnings("serial")
public class CopyInputStreamFactoryRuntimeException extends RuntimeException {

	private CopyInputStreamFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	private CopyInputStreamFactoryRuntimeException(String message) {
		super(message);
	}

	public static class CannotGetNewInputStreamRuntime extends CopyInputStreamFactoryRuntimeException {

		public CannotGetNewInputStreamRuntime(Exception e) {
			super("Cannot get new InputStream", e);
		}

	}

	public static class CannotWriteInputContentInAFileRuntime extends CopyInputStreamFactoryRuntimeException {

		public CannotWriteInputContentInAFileRuntime(Exception e) {
			super("Cannot write input stream content in a file", e);
		}

	}

	public static class CannotReadInputStreamRuntime extends CopyInputStreamFactoryRuntimeException {

		public CannotReadInputStreamRuntime(Exception e) {
			super("Cannot read input stream content in a file", e);
		}

	}

	public static class InputStreamIsNull extends CopyInputStreamFactoryRuntimeException {

		public InputStreamIsNull() {
			super("Input stream is null");
		}

	}
}
