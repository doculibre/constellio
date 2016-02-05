package com.constellio.app.ui.tools;

public class ApplicationRuntimeException extends RuntimeException {

	public ApplicationRuntimeException(String message) {
		super(message);
	}

	public ApplicationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicationRuntimeException(Throwable cause) {
		super(cause);
	}

}
