package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.Capsule;

public class CoreMigrationTo_8_2_1_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		manager.saveMetadata(manager.getMetadata(collection, Capsule.DEFAULT_SCHEMA + "_" + Capsule.HTML)
				.withInputType(MetadataInputType.RICHTEXT));
	}
}