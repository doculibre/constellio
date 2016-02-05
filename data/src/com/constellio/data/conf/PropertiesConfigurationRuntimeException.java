package com.constellio.data.conf;

public class PropertiesConfigurationRuntimeException extends RuntimeException {

	public PropertiesConfigurationRuntimeException(String message) {
		super(message);
	}

	public PropertiesConfigurationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public PropertiesConfigurationRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class PropertiesConfigurationRuntimeException_ConfigNotDefined extends PropertiesConfigurationRuntimeException {

		public PropertiesConfigurationRuntimeException_ConfigNotDefined(String key) {
			super("Property '" + key + "' must be defined");
		}
	}

	public static class PropertiesConfigurationRuntimeException_InvalidConfigValue
			extends PropertiesConfigurationRuntimeException {

		public PropertiesConfigurationRuntimeException_InvalidConfigValue(String key, String value) {
			super("Invalid property '" + key + "' value '" + value + "' is invalid");
		}

		public PropertiesConfigurationRuntimeException_InvalidConfigValue(String key, String value,
				String supportedValuesString) {
			super("Invalid property '" + key + "' value '" + value + "' is invalid. Supported values are : "
					+ supportedValuesString);
		}
	}

}
