package com.constellio.app.services.factories;

import com.constellio.data.utils.Factory;

public class SingletonConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	ConstellioFactories instance;

	@Override
	public synchronized ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory) {

		if (instance == null) {
			instance = constellioFactoriesFactory.get();
			instance.getAppLayerFactory().initialize();
			//instance.getAppLayerFactory().initialize();
			//			((PluginManagerImpl) instance.getAppLayerFactory().getPluginManager()).getPluginConfiguration()
			//					.setConfiguration(ConstellioPlugin.class, "singletonInitializeMode", "true");
		}

		return instance;
	}

	@Override
	public synchronized boolean isInitialized() {
		return instance != null;
	}

	@Override
	public void clear() {
		instance.getAppLayerFactory().close();
		instance = null;
	}

}
