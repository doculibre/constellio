package com.constellio.app.modules.es;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;

import static com.constellio.app.modules.rm.ConstellioRMModule.ID;

public class ESConfigs {

	static List<SystemConfiguration> configurations = new ArrayList<>();

	public static final SystemConfiguration CONNECTOR_NUMBER_OF_RECORDS_PER_BATCH,
			CONNECTOR_NUMBER_OF_THREADS;

	static {
		SystemConfigurationGroup others = new SystemConfigurationGroup(ConstellioESModule.ID, "others");

		add(CONNECTOR_NUMBER_OF_RECORDS_PER_BATCH = others.createInteger("connectorNumberOfRecordsPerBatch").withDefaultValue(50).whichIsHidden());

		add(CONNECTOR_NUMBER_OF_THREADS = others.createInteger("connectorNumberOfThreads").withDefaultValue(-1).whichIsHidden());
	}

	static void add(SystemConfiguration configuration) {
		configurations.add(configuration);
	}

	SystemConfigurationsManager manager;

	public ESConfigs(AppLayerFactory appLayerFactory) {
		this.manager = appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager();
	}

	public ESConfigs(ModelLayerFactory modelLayerFactory) {
		this.manager = modelLayerFactory.getSystemConfigurationsManager();
	}

	public ESConfigs(SystemConfigurationsManager manager) {
		this.manager = manager;
	}

	public int getConnectorNumberOfRecordsPerBatch() {
		return manager.getValue(CONNECTOR_NUMBER_OF_RECORDS_PER_BATCH);
	}

	public int getConnectorNumberOfThreads() {
		int configValue = manager.getValue(CONNECTOR_NUMBER_OF_THREADS);
		return configValue != -1 ? configValue: Runtime.getRuntime().availableProcessors();
	}

}
