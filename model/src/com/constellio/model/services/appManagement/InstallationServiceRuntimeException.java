package com.constellio.model.services.appManagement;

@SuppressWarnings("serial")
public class InstallationServiceRuntimeException extends RuntimeException {

	public InstallationServiceRuntimeException() {
	}

	public InstallationServiceRuntimeException(String message) {
		super(message);
	}

	public InstallationServiceRuntimeException(Throwable cause) {
		super(cause);
	}

	public InstallationServiceRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class ConfigurationFileNotCreated extends InstallationServiceRuntimeException {

		public ConfigurationFileNotCreated() {
			super("Configuration file was not created");
		}
	}
}
