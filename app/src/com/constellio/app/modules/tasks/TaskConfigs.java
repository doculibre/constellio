package com.constellio.app.modules.tasks;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.modules.tasks.TaskModule.ID;

public class TaskConfigs {

	static List<SystemConfiguration> configurations = new ArrayList<>();

	SystemConfigurationsManager manager;

	public TaskConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	public static final SystemConfiguration DEFAULT_DUE_DATE;
	public static final SystemConfiguration SHOW_COMMENTS;

	static {
		SystemConfigurationGroup task = new SystemConfigurationGroup(ID, "tasks");
		add(DEFAULT_DUE_DATE = task.createInteger("defaultDueDate").withDefaultValue(0));
		add(SHOW_COMMENTS = task.createBooleanFalseByDefault("showComments"));
	}

	public int getDefaultDueDate() {
		return manager.getValue(DEFAULT_DUE_DATE);
	}
}
