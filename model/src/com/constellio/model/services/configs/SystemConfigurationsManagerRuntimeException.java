package com.constellio.model.services.configs;

public class SystemConfigurationsManagerRuntimeException extends RuntimeException {

	public SystemConfigurationsManagerRuntimeException(String message) {
		super(message);
	}

	public SystemConfigurationsManagerRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public SystemConfigurationsManagerRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class SystemConfigurationsManagerRuntimeException_InvalidConfigValue
			extends SystemConfigurationsManagerRuntimeException {

		public SystemConfigurationsManagerRuntimeException_InvalidConfigValue(String config, Object value) {
			super("Value '" + value + "' for config '" + config + "' is invalid.");
		}
	}

	public static class SystemConfigurationsManagerRuntimeException_UpdateScriptFailed
			extends SystemConfigurationsManagerRuntimeException {

		public SystemConfigurationsManagerRuntimeException_UpdateScriptFailed(String config, Object value, Exception e) {
			super("Update script failed when changing value '" + value + "' for config '" + config, e);
		}
	}
}
