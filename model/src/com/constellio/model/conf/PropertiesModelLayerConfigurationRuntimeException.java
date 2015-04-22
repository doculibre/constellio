/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
