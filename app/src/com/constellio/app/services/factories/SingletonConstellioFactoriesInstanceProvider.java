package com.constellio.app.services.factories;

import com.constellio.data.dao.services.CurrentTenantSingleton;
import com.constellio.data.utils.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SingletonConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonConstellioFactoriesInstanceProvider.class);

	ConstellioFactories[] instances = new ConstellioFactories[256];
	static ThreadLocal<ConstellioFactories> instancesThreadLocal = new ThreadLocal<>();

	@Override
	public ConstellioFactories getInstance(Factory<ConstellioFactories> constellioFactoriesFactory) {

		if (!CurrentTenantSingleton.isInitialized() || CurrentTenantSingleton.hasCurrentTenant()) {
			boolean createdByThisThread = false;

			//Only one thread can create the factories, other threads are waiting for factories to be initialized
			//Current thread call to getInstance will return between the first initialize will return the not yet fully
			// initialized factory, we don't want to block the main thread or create a second factory

			int currentTenantIndex = CurrentTenantSingleton.getCurrentTenant().getIndex();
			synchronized (this) {
				ConstellioFactories factoriesInInitilization = instancesThreadLocal.get();
				if (instances[currentTenantIndex] == null) {
					if (factoriesInInitilization != null) {
						LOGGER.info("Reentring getInstance, returning uninitialized instance " + factoriesInInitilization + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
						return factoriesInInitilization;
					} else {
						ConstellioFactories instanceBeingInitialized = constellioFactoriesFactory.get();
						instancesThreadLocal.set(instanceBeingInitialized);
						LOGGER.info("Initializing instance " + instanceBeingInitialized + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
						instanceBeingInitialized.getAppLayerFactory().initialize();

						instances[currentTenantIndex] = instanceBeingInitialized;
						instancesThreadLocal.set(null);
						createdByThisThread = true;
					}
				}

			}
			if (createdByThisThread) {
				LOGGER.info("Post-intializing instance " + instances[currentTenantIndex] + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
				instances[currentTenantIndex].getAppLayerFactory().postInitialization();
			}

			return instances[currentTenantIndex];
			//instance.getAppLayerFactory().initialize();
			//			((PluginManagerImpl) instance.getAppLayerFactory().getPluginManager()).getPluginConfiguration()
			//					.setConfiguration(ConstellioPlugin.class, "singletonInitializeMode", "true");
		} else {
			throw new IllegalStateException("Could not get ConstellioFactories instance : no current tenant");

		}


	}

	@Override
	public synchronized boolean isInitialized() {
		if (!CurrentTenantSingleton.isInitialized() || CurrentTenantSingleton.hasCurrentTenant()) {
			int currentTenantIndex = CurrentTenantSingleton.getCurrentTenant().getIndex();
			return instances[currentTenantIndex] != null;
		} else {
			return false;
		}
	}

	@Override
	public void clear() {
		for (int i = 0; i < 256; i++) {
			if (instances[i] != null) {
				instances[i].getAppLayerFactory().close();
				LOGGER.info("SingletonConstellioFactoriesInstanceProvider:clear");
				instances[i] = null;
			}
		}
	}

}
