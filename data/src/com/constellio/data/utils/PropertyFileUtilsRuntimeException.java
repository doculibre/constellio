package com.constellio.data.utils;

public class PropertyFileUtilsRuntimeException extends RuntimeException {
	public PropertyFileUtilsRuntimeException(String message) {
		super(message);
	}

	public PropertyFileUtilsRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertyFileUtilsRuntimeException(Throwable cause) {
		super(cause);
	}
}
