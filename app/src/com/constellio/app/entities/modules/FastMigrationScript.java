package com.constellio.app.entities.modules;

import java.util.List;

import com.constellio.app.services.factories.AppLayerFactory;

public interface FastMigrationScript {

	public List<MigrationScript> getVersions();

	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception;
}
