package com.constellio.data.io.streams.factories;

@SuppressWarnings({ "serial" })
public class StreamsServicesRuntimeException extends RuntimeException {

	public StreamsServicesRuntimeException() {
	}

	public StreamsServicesRuntimeException(String message) {
		super(message);
	}

	public StreamsServicesRuntimeException(Throwable cause) {
		super(cause);
	}

	public StreamsServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCreateTempFile extends StreamsServicesRuntimeException {

		public CannotCreateTempFile(String filePath, Exception e) {
			super("Cannot create temp file " + filePath, e);
		}
	}

	public static class StreamsServicesRuntimeException_FileNotFound extends StreamsServicesRuntimeException {

		public StreamsServicesRuntimeException_FileNotFound(Exception e) {
			super(e);
		}
	}

}
