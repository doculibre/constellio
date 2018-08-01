package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreMigrationTo_6_5_14 implements MigrationScript {
	private final static Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_6_5_14.class);

	@Override
	public String getVersion() {
		return "6.5.14";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		SystemConfigurationsManager systemConfigurationsManager = appLayerFactory.getModelLayerFactory()
				.getSystemConfigurationsManager();
		systemConfigurationsManager.setValue(ConstellioEIMConfigs.TRASH_PURGE_DELAI, 90);
	}

}
