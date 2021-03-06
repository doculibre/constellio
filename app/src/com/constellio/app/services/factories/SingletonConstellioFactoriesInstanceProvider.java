package com.constellio.app.services.factories;

import com.constellio.app.services.factories.AppLayerFactoryRuntineException.AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldNotRetry;
import com.constellio.app.services.factories.AppLayerFactoryRuntineException.AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldRetry;
import com.constellio.app.services.factories.ConstellioFactoriesRuntimeException.ConstellioFactoriesRuntimeException_TenantOffline;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TenantUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.constellio.data.utils.TenantUtils.EMPTY_TENANT_ID;

public class SingletonConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonConstellioFactoriesInstanceProvider.class);

	private Map<String, ConstellioFactories> instanceByTenantId;

	private Map<String, Throwable> brokenFactoriesMap;

	private Map<String, Thread> initThread;

	SingletonConstellioFactoriesInstanceProvider() {
		instanceByTenantId = new ConcurrentHashMap<>();
		brokenFactoriesMap = new ConcurrentHashMap<>();
	}


	@Override
	public ConstellioFactories getInstance(String tenantId, Factory<ConstellioFactories> constellioFactoriesFactory,
										   boolean acceptingFailedFactories) {
		String currentTenantId = getCurrentTenantId(tenantId);

		if (constellioFactoriesFactory == null) {
			return instanceByTenantId.get(currentTenantId);
		}

		if (!acceptingFailedFactories && tenantId != null) {
			Throwable throwedException = brokenFactoriesMap.get(tenantId);
			if (throwedException != null) {
				throw new ConstellioFactoriesRuntimeException_TenantOffline(tenantId, throwedException);
			}
		}

		return instanceByTenantId.computeIfAbsent(currentTenantId, key -> {
			ConstellioFactories instanceBeingInitialized = constellioFactoriesFactory.get();

			CompletableFuture.runAsync(() -> {
				TenantUtils.setTenant(tenantId);
				try {
					initializeInstance(key, instanceBeingInitialized);

				} catch (AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldRetry ignored) {
					LOGGER.info("Factories of tenant '" + tenantId + "' failed to initialize. Removing it from the map and retrying a new creation");
					clear(currentTenantId);

					getInstance(currentTenantId, constellioFactoriesFactory, acceptingFailedFactories);
					//The retry is not working well, since these factories have been returned
					//getInstance(currentTenantId, constellioFactoriesFactory);
					//Nothing, just re-entering the while loop for an other attempt

				} catch (AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldNotRetry t) {
					LOGGER.info("Factories of tenant '" + tenantId + "' failed to initialize. This tenant is now offline", t);
					brokenFactoriesMap.put(tenantId, t.getCause());
					clear(currentTenantId);

				} catch (Throwable t) {
					LOGGER.info("Factories of tenant '" + tenantId + "' failed to initialize. This tenant is now offline", t);
					brokenFactoriesMap.put(tenantId, t);
					clear(currentTenantId);

				}
			});

			return instanceBeingInitialized;
		});
	}

	private void initializeInstance(String tenantId, ConstellioFactories instanceBeingInitialized) {
		LOGGER.info("Initializing instance " + instanceBeingInitialized + " for tenant id " + tenantId + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
		instanceBeingInitialized.getAppLayerFactory().initialize();

		LOGGER.info("Post-intializing instance " + instanceBeingInitialized + " for tenant id " + tenantId + " from provider " + SingletonConstellioFactoriesInstanceProvider.this.toString());
		instanceBeingInitialized.getAppLayerFactory().postInitialization();
	}

	@Override
	public boolean isInitialized(String tenantId) {
		return instanceByTenantId.containsKey(getCurrentTenantId(tenantId));
	}

	@Override
	public void clear(String tenantId) {
		String currentTenantId = getCurrentTenantId(tenantId);
		if (instanceByTenantId.containsKey(currentTenantId)) {
			instanceByTenantId.get(currentTenantId).getAppLayerFactory().close();
			LOGGER.info("SingletonConstellioFactoriesInstanceProvider:clear for tenant id : " + currentTenantId);
			instanceByTenantId.remove(currentTenantId);
		}
	}

	@Override
	public void clearAll() {
		instanceByTenantId.values().forEach(instance -> instance.getAppLayerFactory().close());
		instanceByTenantId.clear();
	}

	private String getCurrentTenantId(String tenantId) {
		if (tenantId == null) {
			return EMPTY_TENANT_ID;
		}
		return tenantId;
	}

}
