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
