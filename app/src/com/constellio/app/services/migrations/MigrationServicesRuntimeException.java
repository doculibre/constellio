package com.constellio.app.services.migrations;

@SuppressWarnings("serial")
public class MigrationServicesRuntimeException extends RuntimeException {

	public MigrationServicesRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public MigrationServicesRuntimeException(String message) {
		super(message);
	}

	public MigrationServicesRuntimeException(Throwable cause) {
		super(cause);
	}
}
