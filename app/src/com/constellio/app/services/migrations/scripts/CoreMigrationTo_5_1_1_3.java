package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;

public class CoreMigrationTo_5_1_1_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		appLayerFactory.getModelLayerFactory().getEmailQueueManager().clearQueue();

	}
}
