package com.constellio.app.utils;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.factories.ConstellioFactoriesDecorator;
import com.constellio.app.services.factories.ConstellioFactoriesRuntimeException.ConstellioFactoriesRuntimeException_TenantOffline;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.services.tenant.TenantService;
import com.constellio.data.utils.TenantUtils;
import com.constellio.model.conf.ModelLayerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class ScriptsUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptsUtils.class);

	public static void forEachAvailableTenant(BiConsumer<String, AppLayerFactory> script) {

		String currentTenant = TenantUtils.getTenantId();

		TenantService.getInstance().getTenants().forEach((properties) -> {
			String tenantId = "" + properties.getId();
			TenantUtils.setTenant(tenantId);

			try {
				AppLayerFactory appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
				script.accept(tenantId, appLayerFactory);
			} catch (ConstellioFactoriesRuntimeException_TenantOffline e) {
				LOGGER.warn("Task not executed on offline tenant '" + tenantId + "'", new Exception("Task stacktrace (no error)"));
			}
		});

		TenantUtils.setTenant(currentTenant);
	}

	public static void forEachAvailableAndFailedTenants(BiConsumer<String, AppLayerFactory> script) {

		String currentTenant = TenantUtils.getTenantId();

		TenantService.getInstance().getTenants().forEach((properties) -> {
			String tenantId = "" + properties.getId();
			TenantUtils.setTenant(tenantId);

			AppLayerFactory appLayerFactory = ConstellioFactories.getInstance(true).getAppLayerFactory();
			script.accept(tenantId, appLayerFactory);
		});

		TenantUtils.setTenant(currentTenant);
	}

	public static AppLayerFactory startLayerFactoriesWithoutBackgroundThreads() {
		ConstellioFactoriesDecorator constellioFactoriesDecorator = new ConstellioFactoriesDecorator() {
			@Override
			public AppLayerConfiguration decorateAppLayerConfiguration(AppLayerConfiguration appLayerConfiguration) {
				return super.decorateAppLayerConfiguration(appLayerConfiguration);
			}

			@Override
			public ModelLayerConfiguration decorateModelLayerConfiguration(
					ModelLayerConfiguration modelLayerConfiguration) {
				modelLayerConfiguration.setBatchProcessesEnabled(false);
				return super.decorateModelLayerConfiguration(modelLayerConfiguration);
			}

			@Override
			public DataLayerConfiguration decorateDataLayerConfiguration(
					DataLayerConfiguration dataLayerConfiguration) {
				dataLayerConfiguration.setBackgroundThreadsEnabled(false);
				return super.decorateDataLayerConfiguration(dataLayerConfiguration);
			}
		};

		return ConstellioFactories.getInstance(constellioFactoriesDecorator).getAppLayerFactory();
	}

}
