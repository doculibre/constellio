package com.constellio.model.services.configs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.ConfigManagerException.OptimisticLockingConfiguration;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.BinaryConfiguration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperation;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.configs.SystemConfigurationsManagerRuntimeException.SystemConfigurationsManagerRuntimeException_InvalidConfigValue;

public class SystemConfigurationIOServices {

	static final String CONFIG_FILE_PATH = "/systemConfigs.properties";
	ConfigManager configManager;
	IOServices ioServices;

	public SystemConfigurationIOServices(ConfigManager configManager, IOServices ioServices) {
		this.configManager = configManager;
		this.ioServices = ioServices;
	}

	public SystemConfigurationIOServices(DataLayerFactory dataLayerFactory) {
		this.configManager = dataLayerFactory.getConfigManager();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
	}

	public void setValue(final SystemConfiguration config, final Object newValue) {
		configManager.updateProperties(CONFIG_FILE_PATH, updateConfigValueAlteration(config, newValue));
	}

	private PropertiesAlteration updateConfigValueAlteration(final SystemConfiguration config, final Object newValue) {
		return new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				if (LangUtils.isEqual(newValue, config.getDefaultValue())) {
					properties.remove(config.getPropertyKey());
				} else {
					properties.put(config.getPropertyKey(), SystemConfigurationIOServices.this.toString(config, newValue));
				}
			}
		};
	}

	private String toString(SystemConfiguration config, Object value) {
		if (value == null) {
			return null;
		}
		switch (config.getType()) {

		case STRING:
			return value.toString();
		case BOOLEAN:
			return ((Boolean) value) ? "true" : "false";
		case INTEGER:
			return "" + value;
		case ENUM:
			return ((Enum<?>) value).name();
		}
		throw new ImpossibleRuntimeException("Unsupported config type : " + config.getType());
	}

	Object toObject(SystemConfiguration config, String value) {
		if (value == null) {
			return null;
		}
		switch (config.getType()) {

		case STRING:
			return value;
		case BOOLEAN:
			return "true".equals(value);
		case INTEGER:
			return Integer.valueOf(value);
		case ENUM:
			return EnumUtils.getEnum((Class) config.getEnumClass(), value);
		}
		throw new ImpossibleRuntimeException("Unsupported config type : " + config.getType());
	}

	public void createEmptyFileIfInexistent() {
		configManager.createPropertiesDocumentIfInexistent(CONFIG_FILE_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
			}
		});
	}

	public StreamFactory<InputStream> getBinaryValue(SystemConfiguration config) {
		BinaryConfiguration binaryConfiguration = configManager.getBinary(config.getPropertyKey());
		return binaryConfiguration == null ? null : binaryConfiguration.getInputStreamFactory();
	}

	public void reset(final SystemConfiguration config) {
		configManager.updateProperties(CONFIG_FILE_PATH, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.remove(config.getPropertyKey());
			}
		});
	}

	public Object getConvertedValue(SystemConfiguration config) {
		return toObject(config, getValue(config));
	}

	public String getValue(SystemConfiguration config) {
		return configManager.getProperties(CONFIG_FILE_PATH).getProperties().get(config.getPropertyKey());
	}

	public void setBinaryValue(SystemConfiguration config, StreamFactory<InputStream> streamFactory) {
		final String configPath = "/systemConfigs/" + config.getCode();
		if (configManager.exist(configPath)) {
			if (streamFactory == null) {
				configManager.delete(configPath);
			} else {

				try {
					ioServices.execute(new StreamOperation<InputStream>() {
						@Override
						public void execute(InputStream stream) {
							String hash = configManager.getBinary(configPath).getHash();
							try {
								configManager.update(configPath, hash, stream);
							} catch (OptimisticLockingConfiguration e) {
								throw new ImpossibleRuntimeException(e);
							}
						}
					}, streamFactory);
				} catch (IOException e) {
					throw new SystemConfigurationsManagerRuntimeException_InvalidConfigValue(config.getCode(), "");
				}

			}
		} else {
			if (streamFactory != null) {
				try {
					ioServices.execute(new StreamOperation<InputStream>() {
						@Override
						public void execute(InputStream stream) {
							configManager.add(configPath, stream);
						}
					}, streamFactory);
				} catch (IOException e) {
					throw new SystemConfigurationsManagerRuntimeException_InvalidConfigValue(config.getCode(), "");
				}

			}
		}
	}

	public Map<String, String> getRawProperties() {
		PropertiesConfiguration propertiesConfig = configManager.getProperties(CONFIG_FILE_PATH);
		if (propertiesConfig != null) {
			return propertiesConfig.getProperties();
		} else {
			return Collections.emptyMap();
		}
	}
}
