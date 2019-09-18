package com.constellio.app.modules.tasks;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.ArrayList;
import java.util.List;

public class TaskConfigs {
	public static final SystemConfiguration ADD_COMMENTS_WHEN_READ_AUTHORIZATION;

	static List<SystemConfiguration> configurations = new ArrayList<>();


	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public TaskConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(TaskModule.ID, "others");
		add(ADD_COMMENTS_WHEN_READ_AUTHORIZATION = others.createBooleanTrueByDefault("addCommentsWhenReadAuthorization").whichIsHidden());
	}

	public boolean isAddCommentsWhenReadAuthorization() {
		return manager.getValue(ADD_COMMENTS_WHEN_READ_AUTHORIZATION);
	}
}
