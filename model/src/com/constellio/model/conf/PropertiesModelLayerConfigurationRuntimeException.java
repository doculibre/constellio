package com.constellio.model.conf;

public class PropertiesModelLayerConfigurationRuntimeException extends RuntimeException {

	public PropertiesModelLayerConfigurationRuntimeException(String message) {
		super(message);
	}

	public PropertiesModelLayerConfigurationRuntimeException() {
		super();
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidEmail
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidEmail(String email) {
			super("Invalid email: " + email);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidRegex
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidRegex(String email) {
			super("Invalid regex: " + email);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidUser
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidUser(String user) {
			super("Invalid user: " + user);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidPassword
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidPassword(String password) {
			super("Invalid password: " + password);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_NotABooleanValue(String property, String value) {
			super(property + " is not a boolean value: " + value);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidHost
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidHost(String host) {
			super("Invalid host: " + host);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidPort
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidPort(String port) {
			super("Invalid port: " + port);
		}
	}

	public static class PropertiesModelLayerConfigurationRuntimeException_InvalidLdapType
			extends PropertiesModelLayerConfigurationRuntimeException {
		public PropertiesModelLayerConfigurationRuntimeException_InvalidLdapType(String type) {
			super("Invalid ldap directory type: " + type);
		}
	}
}
