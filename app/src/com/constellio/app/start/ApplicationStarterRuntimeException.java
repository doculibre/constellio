package com.constellio.app.start;

@SuppressWarnings("serial")
public class ApplicationStarterRuntimeException extends RuntimeException {

	public ApplicationStarterRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationStarterRuntimeException(String message) {
		super(message);
	}

	public ApplicationStarterRuntimeException(Throwable cause) {
		super(cause);
	}

}
