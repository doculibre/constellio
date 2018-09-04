package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo7_4_49 extends MigrationHelper implements MigrationScript {
	private String collection;

	private MigrationResourcesProvider migrationResourcesProvider;

	private AppLayerFactory appLayerFactory;

	@Override
	public String getVersion() {
		return "7.4.49";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		this.collection = collection;
		this.migrationResourcesProvider = migrationResourcesProvider;
		this.appLayerFactory = appLayerFactory;

		new RMMigrationTo7_4_49.SchemaAlterationFor7_4_49(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor7_4_49 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor7_4_49(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);

		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

		}
	}
}
