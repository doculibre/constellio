package com.constellio.sdk.tests;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.factories.ConstellioFactoriesInstanceProvider;
import com.constellio.app.services.factories.SingletonConstellioFactoriesInstanceProvider;
import com.constellio.data.utils.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SDKConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SDKConstellioFactoriesInstanceProvider.class);

	public static final String DEFAULT_NAME = "default";

	public static boolean firstTest;

	ThreadLocal<String> nameThreadLocal = new ThreadLocal<>();
	Map<String, ConstellioFactories> instances = new HashMap<>();

	@Override
	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory) {
		String name = nameThreadLocal.get();
		if (name == null) {
			name = DEFAULT_NAME;
		}
		return getInstance(constellioFactoriesFactory, name);
	}

	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory, String name) {
		ConstellioFactories constellioFactories = instances.get(name);

		if (constellioFactories == null && constellioFactoriesFactory != null) {

			boolean createdByThisThread = false;
			synchronized (SingletonConstellioFactoriesInstanceProvider.class) {
				if (constellioFactories == null) {
					constellioFactories = constellioFactoriesFactory.get();
					instances.put(name, constellioFactories);
					if (firstTest) {
						firstTest = false;
					} else {
						constellioFactories.getAppLayerFactory().initialize();
					}
					createdByThisThread = true;
				}
			}
			if (createdByThisThread) {
				constellioFactories.getAppLayerFactory().postInitialization();
			}
			//instance.getAppLayerFactory().initialize();
			//			((PluginManagerImpl) instance.getAppLayerFactory().getPluginManager()).getPluginConfiguration()
			//					.setConfiguration(ConstellioPlugin.class, "singletonInitializeMode", "true");
		}

		//		if (constellioFactories == null) {
		//			constellioFactories = constellioFactoriesFactory.get();
		//			instances.put(name, constellioFactories);
		//			//constellioFactories.getAppLayerFactory().getPluginManager().configure();
		//			constellioFactories.getAppLayerFactory().initialize();
		//		}
		return constellioFactories;

	}

	@Override
	public boolean isInitialized() {
		return !instances.isEmpty();
	}

	@Override
	public void clear() {
		for (String instanceName : instances.keySet()) {
			ConstellioFactories instance = instances.get(instanceName);

			if (instances.size() > 1) {
				LOGGER.info("Closing instance '" + instanceName + "'");
			}

			instance.getAppLayerFactory().close();
		}
		instances.clear();
	}
}
