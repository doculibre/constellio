package com.constellio.app.modules.restapi;

import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;

import java.util.ArrayList;
import java.util.List;

public class RestApiConfigs {

	private SystemConfigurationsManager manager;

	static List<SystemConfiguration> configurations = new ArrayList<>();

	public static final SystemConfiguration REST_API_URLS;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(ConstellioRestApiModule.ID, "others");

		add(REST_API_URLS = others.createString("restApiUrls"));
	}

	private static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

}
