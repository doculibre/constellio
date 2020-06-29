package com.constellio.app.services.factories;

@SuppressWarnings("serial")
public class ConstellioFactoriesRuntimeException extends RuntimeException {

	public ConstellioFactoriesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConstellioFactoriesRuntimeException(String message) {
		super(message);
	}

	public ConstellioFactoriesRuntimeException(Throwable cause) {
		super(cause);
	}


}