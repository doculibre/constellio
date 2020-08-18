package com.constellio.model.services.configs;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.PropertiesAlteration;
import com.constellio.data.dao.managers.config.values.PropertiesConfiguration;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.configs.UserConfiguration;
import com.constellio.model.entities.configs.UserConfigurationType;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.structures.TablePanelProperties;
import com.constellio.model.entities.structures.TablePanelPropertiesFactory;
import com.constellio.model.entities.structures.TableProperties;
import com.constellio.model.entities.structures.TablePropertiesFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class UserConfigurationsManager implements StatefulService {

	private static final String COLLECTION_TOKEN = "[COLLECTION]";
	private static final String USERNAME_TOKEN = "[USERNAME]";
	private static final String CONFIG_FILE_PATH = "/" + COLLECTION_TOKEN + "/userConfigs/" + USERNAME_TOKEN + ".properties";

	private ConfigManager configManager;

	public UserConfigurationsManager(ConfigManager configManager) {
		this.configManager = configManager;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {

	}

	@SuppressWarnings("unchecked")
	public <T> T getValue(User user, String key, UserConfigurationType type) {
		PropertiesConfiguration propertiesConfig = configManager.getProperties(getFilePath(user));
		if (propertiesConfig != null) {
			Map<String, String> properties = propertiesConfig.getProperties();
			if (properties != null) {
				String value = properties.get(key);
				if (value != null) {
					return (T) toObject(type, value);
				}
			}
		}

		return null;
	}

	public void setValue(User user, String key, UserConfigurationType type, final Object newValue) {
		String path = getFilePath(user);

		this.configManager.keepInCache(path);
		configManager.createPropertiesDocumentIfInexistent(path, ConfigManager.EMPTY_PROPERTY_ALTERATION);
		configManager.updateProperties(path, new PropertiesAlteration() {
			@Override
			public void alter(Map<String, String> properties) {
				properties.put(key, UserConfigurationsManager.this.toString(type, newValue));
			}
		});
	}

	public void setTablePropertiesValue(User user, String tableId, final TableProperties newValue) {
		String key = UserConfiguration.TABLE_PROPERTIES + tableId;

		TablePropertiesFactory factory = new TablePropertiesFactory();
		setValue(user, key, UserConfigurationType.STRING, factory.toString(newValue));
	}

	public TableProperties getTablePropertiesValue(User user, String tableId) {
		String key = UserConfiguration.TABLE_PROPERTIES + tableId;
		String value = getValue(user, key, UserConfigurationType.STRING);

		TablePropertiesFactory factory = new TablePropertiesFactory();
		TableProperties properties = (TableProperties) factory.build(value);
		if (properties != null) {
			return properties;
		}

		return new TableProperties(tableId);
	}

	public void setTablePanelPropertiesValue(User user, String tablePanelId, final TablePanelProperties newValue) {
		String key = UserConfiguration.TABLE_PANEL_PROPERTIES + tablePanelId;

		TablePanelPropertiesFactory factory = new TablePanelPropertiesFactory();
		setValue(user, key, UserConfigurationType.STRING, factory.toString(newValue));
	}

	public TablePanelProperties getTablePanelPropertiesValue(User user, String tablePanelId) {
		String key = UserConfiguration.TABLE_PANEL_PROPERTIES + tablePanelId;
		String value = getValue(user, key, UserConfigurationType.STRING);

		TablePanelPropertiesFactory factory = new TablePanelPropertiesFactory();
		TablePanelProperties properties = (TablePanelProperties) factory.build(value);
		if (properties != null) {
			return properties;
		}

		return new TablePanelProperties(tablePanelId);
	}

	public void deleteConfigurations(User user) {
		configManager.delete(getFilePath(user));
	}

	public String getFilePath(User user) {
		String path = StringUtils.replace(CONFIG_FILE_PATH, COLLECTION_TOKEN, user.getCollection());
		path = StringUtils.replace(path, USERNAME_TOKEN, user.getUsername());
		return path;
	}

	private String toString(UserConfigurationType type, Object value) {
		if (value == null) {
			return null;
		}
		switch (type) {

			case STRING:
				return value.toString();
			case BOOLEAN:
				return ((Boolean) value) ? "true" : "false";
			case INTEGER:
				return "" + value;
		}
		throw new ImpossibleRuntimeException("Unsupported config type : " + type);
	}

	private Object toObject(UserConfigurationType type, String value) {
		if (value == null) {
			return null;
		}
		switch (type) {

			case STRING:
				return value;
			case BOOLEAN:
				return "true".equals(value);
			case INTEGER:
				return Integer.valueOf(value);
		}
		throw new ImpossibleRuntimeException("Unsupported config type : " + type);
	}
}
