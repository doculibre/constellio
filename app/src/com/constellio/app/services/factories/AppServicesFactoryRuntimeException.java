package com.constellio.app.services.factories;

public class AppServicesFactoryRuntimeException extends RuntimeException {

	public AppServicesFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public AppServicesFactoryRuntimeException(String message) {
		super(message);
	}

	public AppServicesFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotStartApplication extends AppServicesFactoryRuntimeException {

		public CannotStartApplication(Exception e) {
			super("Cannot start application", e);
		}
	}
}
