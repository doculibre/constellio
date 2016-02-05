package com.constellio.app.services.migrations;

@SuppressWarnings("serial")
public class MigrationServicesException extends Exception {
	public MigrationServicesException(String string) {
		super(string);
	}

	public MigrationServicesException(String string, Exception e) {
		super(string, e);
	}

}
