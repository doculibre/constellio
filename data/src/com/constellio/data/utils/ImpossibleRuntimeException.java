package com.constellio.data.utils;

@SuppressWarnings("serial")
public class ImpossibleRuntimeException extends RuntimeException {

	public ImpossibleRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImpossibleRuntimeException(String message) {
		super(message);
	}

	public ImpossibleRuntimeException(Throwable cause) {
		super(cause);
	}

}
