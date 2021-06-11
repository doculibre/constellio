package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;

public class CoreMigrationFrom9_4_AddMenuDisplayManagerToAppLayerFactory extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		appLayerFactory.getMenusDisplayManager().createEmptyCollectionMenusDisplay(collection);
	}
}
