package com.constellio.model.services.configs;

import com.constellio.model.entities.structures.TableProperties;

import java.util.HashMap;
import java.util.Map;

// TODO::JOLA --> Build the system
public class UserConfigurationsManager {
	private Map<String, TableProperties> tableProperties;

	public UserConfigurationsManager() {
		tableProperties = new HashMap<>();
	}

	public TableProperties getTableProperties(String tableId) {
		return tableProperties.get(tableId);
	}
}
