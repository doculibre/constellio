package com.constellio.app.services.factories;

import com.constellio.app.services.factories.AppLayerFactoryRuntineException.AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldRetry;
import com.constellio.data.utils.Factory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.constellio.data.utils.TenantUtils.EMPTY_TENANT_ID;

public class SingletonConstellioFactoriesInstanceProvider implements ConstellioFactoriesInstanceProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SingletonConstellioFactoriesInstanceProvider.class);

	private Map<String, ConstellioFactories> instanceByTenantId;

	SingletonConstellioFactoriesInstanceProvider() {
		instanceByTenantId = new ConcurrentHashMap<>();
	}

	@Override
	public ConstellioFactories getInstance(String tenantId, Factory<ConstellioFactories> constellioFactoriesFactory) {
		String currentTenantId = getCurrentTenantId(tenantId);

		if (constellioFactoriesFactory == null) {
			return instanceByTenantId.get(currentTenantId);
		}

		return instanceByTenantId.computeIfAbsent(currentTenantId, key -> {
			ConstellioFactories instanceBeingInitialized = constellioFactoriesFactory.get();

			CompletableFuture.runAsync(() -> {

				boolean tryInitialize = true;
				while (tryInitialize) {
					try {
						initializeInstance(key, instanceBeingInitialized);
						tryInitialize = false;

					} catch (AppLayerFactoryRuntineException_ErrorsDuringInitializeShouldRetry ignored) {
						clear(currentTenantId);
						getInstance(currentTenantId, constellioFactoriesFactory);
						//Nothing, just re-entering the while loop for an other attempt

					} catch (Exception e) {
						LOGGER.error("Error while initializing for tenant " + tenantId, e);
					}
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
