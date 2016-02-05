package com.constellio.data.dao.managers.config;

@SuppressWarnings("serial")
public class ConfigManagerRuntimeException extends RuntimeException {

	public ConfigManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigManagerRuntimeException(String message) {
		super(message);
	}

	public static class NoSuchConfiguration extends ConfigManagerRuntimeException {

		public NoSuchConfiguration(String path) {
			super("No such configuration '" + path + "'");
		}
	}

	public static class ConfigurationAlreadyExists extends ConfigManagerRuntimeException {

		public ConfigurationAlreadyExists(String path) {
			super("Could not add configuration '" + path + "' since it already exists");
		}
	}

	public static class CannotCompleteOperation extends ConfigManagerRuntimeException {
		public CannotCompleteOperation(String operation, Exception e) {
			super("Cannot complete operation '" + operation + "'", e);
		}
	}

	public static class CannotHashTheFile extends ConfigManagerRuntimeException {
		public CannotHashTheFile(String file, Exception e) {
			super("Could not hash the file '" + file + "'", e);
		}
	}

	public static class WrongVersion extends ConfigManagerRuntimeException {
		public WrongVersion(String version) {
			super("Cannot update a file with the same version as previously : " + version);
		}
	}

}
