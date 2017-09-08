package com.constellio.data.conf;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Days;
import org.joda.time.Duration;

import com.constellio.data.conf.PropertiesConfigurationRuntimeException.PropertiesConfigurationRuntimeException_ConfigNotDefined;
import com.constellio.data.conf.PropertiesConfigurationRuntimeException.PropertiesConfigurationRuntimeException_InvalidConfigValue;

public abstract class PropertiesConfiguration {

	protected File propertyFile;

	protected Map<String, String> configs;

	protected PropertiesConfiguration(Map<String, String> configs, File propertyFile) {
		this.configs = configs;
		this.propertyFile = propertyFile;
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
		String value = configs.get(key);
		return value == null ? defaultValue : value;
	}

	protected void setBoolean(String key, boolean value) {
		setString(key, value ? "true" : "false");
	}

	protected void setString(String key, String value) {
		writeProperty(key, value);
	}

	protected void setInt(String key, int value) {
		writeProperty(key, "" + value);
	}

	protected void setLong(String key, long value) {
		writeProperty(key, "" + value);
	}

	protected void setFile(String key, File value) {
		writeProperty(key, value == null ? "" : value.getAbsolutePath());
	}

	public void writeProperty(String key, String value) {
		try {
			List<String> properties = FileUtils.readLines(propertyFile);
			String languageProperty = null;
			for (String property : properties) {
				if (property.startsWith(key)) {
					languageProperty = property;
					break;
				}
			}
			if (languageProperty != null) {
				properties.remove(languageProperty);
			}
			if (value != null) {
				properties.add(key + "=" + value);
			}
			propertyFile.delete();
			FileUtils.writeLines(propertyFile, properties);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		configs.put(key, value);
	}

	protected Boolean getBoolean(String key, boolean defaultValue) {
		String stringValue = getString(key, defaultValue ? "true" : "false");
		return Boolean.valueOf(stringValue);
	}

	protected File getFile(String key, File defaultValue) {
		String value = configs.get(key);

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
		String value = configs.get(key);
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

	protected int getInt(String key, int defaultValue) {
		try {
			return Integer.parseInt(getRequiredString(key));
		} catch (Exception e) {
			return defaultValue;
		}
	}

	protected long getLong(String key, long defaultValue) {
		try {
			return Long.parseLong(getRequiredString(key));
		} catch (Exception e) {
			return defaultValue;
		}
	}

	protected Duration getDuration(String key, Duration defaultDuration) {
		String durationString = getString(key, null);

		if (durationString == null) {
			return defaultDuration;
		} else {
			if (durationString.toUpperCase().endsWith("S")) {
				return Duration.standardSeconds(Long.valueOf(StringUtils.substringBefore(durationString.toUpperCase(), "S")));

			} else if (durationString.toUpperCase().endsWith("M")) {
				return Duration.standardMinutes(Long.valueOf(StringUtils.substringBefore(durationString.toUpperCase(), "M")));

			} else if (durationString.toUpperCase().endsWith("H")) {
				return Duration.standardHours(Long.valueOf(StringUtils.substringBefore(durationString.toUpperCase(), "H")));

			} else if (durationString.toUpperCase().endsWith("D")) {
				return Duration.standardDays(Long.valueOf(StringUtils.substringBefore(durationString.toUpperCase(), "D")));

			}
		}

		return null;
	}

	protected void setDuration(String key, Duration value) {
		String stringValue;
		if (value == null) {
			stringValue = null;
		} else {
			stringValue = value.getStandardSeconds() + "s";
		}
		setString(key, stringValue);
	}

}
