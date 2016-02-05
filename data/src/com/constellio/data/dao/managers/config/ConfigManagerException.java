package com.constellio.data.dao.managers.config;

@SuppressWarnings("serial")
public class ConfigManagerException extends Exception {

	public ConfigManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigManagerException(String message) {
		super(message);
	}

	public ConfigManagerException(Throwable cause) {
		super(cause);
	}

	public static class OptimisticLockingConfiguration extends ConfigManagerException {

		public OptimisticLockingConfiguration(String path, String expectedHash, String wasHash) {
			super("Could not update config '" + path + "' since it has been modified by an other thread. Expected hash '"
					+ expectedHash + "' but was '" + wasHash + "'");
		}
	}

}
