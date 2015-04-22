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

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;

import com.constellio.data.conf.PropertiesConfigurationRuntimeException.PropertiesConfigurationRuntimeException_ConfigNotDefined;
import com.constellio.data.conf.PropertiesConfigurationRuntimeException.PropertiesConfigurationRuntimeException_InvalidConfigValue;

public abstract class PropertiesConfiguration {

	private Map<String, String> configs;

	protected PropertiesConfiguration(Map<String, String> configs) {
		this.configs = configs;
	}

	protected Object getEnum(String key, Enum<?> defaultValue) {
		String value = getString(key, null);

		if (value == null) {
			return defaultValue;

		} else {
			Object enumValue = EnumUtils.getEnum(defaultValue.getDeclaringClass(), value.toUpperCase());
			if (enumValue == null) {
				List supportedValues = EnumUtils.getEnumList(defaultValue.getDeclaringClass());
				String supportedValuesString = supportedValues.toString().toLowerCase();
				throw new PropertiesConfigurationRuntimeException_InvalidConfigValue(key, value, supportedValuesString);
			}
			return enumValue;
		}
	}

	protected String getString(String key, String defaultValue) {
		String value = (String) configs.get(key);
		return value == null ? defaultValue : value;
	}

	protected Boolean getBoolean(String key, boolean defaultValue) {
		String stringValue = getString(key, defaultValue ? "true" : "false");
		return Boolean.valueOf(stringValue);
	}

	protected File getFile(String key, File defaultValue) {
		String value = (String) configs.get(key);

		if (value == null) {
			return defaultValue;
		} else {
			return new File(value);
		}
	}

	protected Object getRequiredEnum(String key, Class enumClass) {
		String value = getRequiredString(key);
		Object enumValue = EnumUtils.getEnum(enumClass, value.toUpperCase());

		if (enumValue == null) {
			List supportedValues = EnumUtils.getEnumList(enumClass);
			String supportedValuesString = supportedValues.toString().toLowerCase();
			throw new PropertiesConfigurationRuntimeException_InvalidConfigValue(key, value, supportedValuesString);
		}
		return enumValue;
	}

	protected String getRequiredString(String key) {
		String value = (String) configs.get(key);
		if (value == null) {
			throw new PropertiesConfigurationRuntimeException_ConfigNotDefined(key);
		}
		return value;
	}

	protected Boolean getRequiredBoolean(String key) {
		return Boolean.valueOf(getRequiredString(key));
	}

	protected File getRequiredFile(String key) {
		return new File(getRequiredString(key));
	}

	protected int getRequiredInt(String key) {
		return Integer.parseInt(getRequiredString(key));
	}
}
