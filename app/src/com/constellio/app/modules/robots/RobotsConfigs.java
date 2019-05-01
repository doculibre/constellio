package com.constellio.app.modules.robots;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.util.ArrayList;
import java.util.List;

public class RobotsConfigs {

	private SystemConfigurationsManager manager;

	static List<SystemConfiguration> configurations = new ArrayList<>();

	public static final SystemConfiguration ROBOTS_AUTOMATIC_EXECUTION_DELAY;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(ConstellioESModule.ID, "others");

		add(ROBOTS_AUTOMATIC_EXECUTION_DELAY = others.createInteger("robotsAutomaticExecutionDelay")
				.withDefaultValue(1440).whichIsHidden().whichRequiresReboot());
	}

	public RobotsConfigs(AppLayerFactory appLayerFactory) {
		this.manager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
	}

	public RobotsConfigs(ModelLayerFactory modelLayerFactory) {
		this.manager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public RobotsConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	public int getRobotsAutomaticExecutionDelay() {
		return manager.getValue(ROBOTS_AUTOMATIC_EXECUTION_DELAY);
	}

	private static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

}
