package com.constellio.app.entities.modules;

import com.constellio.app.services.factories.AppLayerFactory;

import java.util.List;

public interface ComboMigrationScript extends MigrationScript {

	public List<MigrationScript> getVersions();

	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception;
}
