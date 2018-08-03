package com.constellio.app.modules.tasks;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.ArrayList;
import java.util.List;

public class TaskConfigs {

	static List<SystemConfiguration> configurations = new ArrayList<>();


	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public TaskConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

}
