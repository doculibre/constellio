package com.constellio.sdk.tests;

import java.util.HashMap;
import java.util.Map;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.factories.ConstellioFactoriesInstanceProvider;
import com.constellio.app.services.factories.SingletonConstellioFactoriesInstanceProvider;
import com.constellio.data.utils.Factory;

public class SDKConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	public static final String DEFAULT_NAME = "default";

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

		if (constellioFactories == null) {

			boolean createdByThisThread = false;
			synchronized (SingletonConstellioFactoriesInstanceProvider.class) {
				if (constellioFactories == null) {
					constellioFactories = constellioFactoriesFactory.get();
					instances.put(name, constellioFactories);
					constellioFactories.getAppLayerFactory().initialize();
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
		for (ConstellioFactories instance : instances.values()) {
			instance.getAppLayerFactory().close();
		}
		instances.clear();
	}
}
