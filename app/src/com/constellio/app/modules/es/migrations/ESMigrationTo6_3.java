package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;

public class ESMigrationTo6_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
	}

}
