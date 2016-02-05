package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;

public interface MigrationScript {

	public String getVersion();

	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception;
}
