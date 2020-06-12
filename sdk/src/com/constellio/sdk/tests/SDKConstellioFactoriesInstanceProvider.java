package com.constellio.sdk.tests;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.factories.ConstellioFactoriesInstanceProvider;
import com.constellio.app.services.factories.SingletonConstellioFactoriesInstanceProvider;
import com.constellio.data.utils.Factory;
import com.constellio.model.utils.TenantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDKConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SDKConstellioFactoriesInstanceProvider.class);

	public static final String EMPTY_TENANT_ID = "-1";
	public static final String DEFAULT_TENANT_ID = "1";
	public static final String DEFAULT_NAME = "default";

	static boolean firstTest;

	private Map<String, Map<String, ConstellioFactories>> instances = new HashMap<>();

	@Override
	public ConstellioFactories getInstance(String tenantId, Factory<ConstellioFactories> constellioFactoriesFactory) {
		return getInstance(constellioFactoriesFactory, DEFAULT_NAME, tenantId);
	}

	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory, String name) {
		return getInstance(constellioFactoriesFactory, name, EMPTY_TENANT_ID);
	}

	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory, String name,
										   String tenantId) {
		String currentTenantId = tenantId != null ? tenantId : EMPTY_TENANT_ID;
		String currentName = name != null ? name : DEFAULT_NAME;

		ConstellioFactories constellioFactories =
				instances.containsKey(currentTenantId) ? instances.get(currentTenantId).get(currentName) : null;

		if (constellioFactories == null && constellioFactoriesFactory != null) {

			synchronized (SingletonConstellioFactoriesInstanceProvider.class) {
				constellioFactories = constellioFactoriesFactory.get();
				addInstanceToMap(currentTenantId, currentName, constellioFactories);
				if (firstTest) {
					firstTest = false;
				} else {
					constellioFactories.getAppLayerFactory().initialize();
				}
				constellioFactories.getAppLayerFactory().postInitialization();
			}
		}
		return constellioFactories;
	}

	public boolean isInitialized() {
		if (TenantUtils.isSupportingTenants()) {
			return isInitialized(DEFAULT_TENANT_ID);
		}
		return isInitialized(EMPTY_TENANT_ID);
	}

	@Override
	public boolean isInitialized(String tenantId) {
		return instances.containsKey(tenantId);
	}

	@Override
	public void clearAll() {
		getAllInstances().forEach(instance -> instance.getAppLayerFactory().close());
		instances.clear();
	}

	@Override
	public void clear(String tenantId) {
		String currentTenantId = tenantId != null ? tenantId : EMPTY_TENANT_ID;

		if (instances.containsKey(currentTenantId)) {
			for (String instanceName : instances.get(currentTenantId).keySet()) {
				ConstellioFactories instance = instances.get(currentTenantId).get(instanceName);

				if (instances.get(currentTenantId).size() > 1) {
					LOGGER.info("Closing instance '" + instanceName + "'");
				}

				instance.getAppLayerFactory().close();
			}
			instances.remove(currentTenantId);
		}
	}

	public List<ConstellioFactories> getAllInstances() {
		List<ConstellioFactories> constellioFactories = new ArrayList<>();
		instances.forEach((key, value) -> constellioFactories.addAll(value.values()));
		return constellioFactories;
	}

	private void addInstanceToMap(String tenantId, String name, ConstellioFactories constellioFactories) {
		instances.computeIfPresent(tenantId, (key, value) -> {
			value.put(name, constellioFactories);
			return value;
		});
		instances.computeIfAbsent(tenantId, key -> new HashMap<String, ConstellioFactories>() {{
			put(name, constellioFactories);
		}});

	}
}
