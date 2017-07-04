package com.constellio.app.services.factories;

import com.constellio.data.utils.Factory;

public class SingletonConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	ConstellioFactories instance;

	@Override
	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory) {

		if (instance == null) {
			boolean createdByThisThread = false;
			synchronized (this) {
				if (instance == null) {
					ConstellioFactories instanceBeingInitialized = constellioFactoriesFactory.get();
					instanceBeingInitialized.getAppLayerFactory().initialize();
					instance = instanceBeingInitialized;
					createdByThisThread = true;
				}
			}
			if (createdByThisThread) {
				instance.getAppLayerFactory().postInitialization();
			}
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
