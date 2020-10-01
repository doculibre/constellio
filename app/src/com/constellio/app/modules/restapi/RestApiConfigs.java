package com.constellio.app.modules.restapi;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestApiConfigs {

	private SystemConfigurationsManager manager;

	static List<SystemConfiguration> configurations = new ArrayList<>();

	public static final SystemConfiguration REST_API_URLS;
	public static final SystemConfiguration CORS_ALLOWED_ORIGINS;
	public static final SystemConfiguration EXPERIMENTAL_SERVICES;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(ConstellioRestApiModule.ID, "others");

		add(REST_API_URLS = others.createString("restApiUrls"));
		add(CORS_ALLOWED_ORIGINS = others.createString("corsAllowedOrigins").whichRequiresReboot());
		add(EXPERIMENTAL_SERVICES = others.createBooleanFalseByDefault("experimentalServices").whichIsHidden());
	}

	public RestApiConfigs(AppLayerFactory appLayerFactory) {
		this.manager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
	}

	public RestApiConfigs(ModelLayerFactory modelLayerFactory) {
		this.manager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public List<String> getRestApiUrls() {
		String value = manager.getValue(RestApiConfigs.REST_API_URLS);
		if (Strings.isNullOrEmpty(value)) {
			return Collections.emptyList();
		}

		List<String> restApiUrls = new ArrayList<>();
		String[] urls = value.split(";");
		for (String url : urls) {
			restApiUrls.add((url.trim()));
		}
		return restApiUrls;
	}

	public String getCorsAllowedOrigins() {
		return manager.getValue(RestApiConfigs.CORS_ALLOWED_ORIGINS);
	}

	public boolean areExperimentalServicesEnabled() {
		return manager.getValue(RestApiConfigs.EXPERIMENTAL_SERVICES);
	}

	private static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

}
