package com.constellio.app.services.factories;

import com.constellio.data.utils.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SingletonConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonConstellioFactoriesInstanceProvider.class);

	ConstellioFactories instance;
	static ThreadLocal<ConstellioFactories> instancesThreadLocal = new ThreadLocal<>();

	@Override
	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory) {

		if (instance == null) {
			boolean createdByThisThread = false;

			//Only one thread can create the factories, other threads are waiting for factories to be initialized
			//Current thread call to getInstance will return between the first initialize will return the not yet fully
			// initialized factory, we don't want to block the main thread or create a second factory


			synchronized (this) {
				ConstellioFactories factoriesInInitilization = instancesThreadLocal.get();
				if (instance == null) {
					if (factoriesInInitilization != null) {
						LOGGER.info("Reentring getInstance, reurning uninotialized instance " + factoriesInInitilization + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
						return factoriesInInitilization;
					} else {
						ConstellioFactories instanceBeingInitialized = constellioFactoriesFactory.get();
						instancesThreadLocal.set(instanceBeingInitialized);
						LOGGER.info("Initializing instance " + instanceBeingInitialized + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
						instanceBeingInitialized.getAppLayerFactory().initialize();

						instance = instanceBeingInitialized;
						instancesThreadLocal.set(null);
						createdByThisThread = true;
					}
				}
			}
			if (createdByThisThread) {
				LOGGER.info("Post-intializing instance " + instance + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
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
		if (instance != null) {
			instance.getAppLayerFactory().close();
			LOGGER.info("SingletonConstellioFactoriesInstanceProvider:clear");
			instance = null;
		}
	}

}
