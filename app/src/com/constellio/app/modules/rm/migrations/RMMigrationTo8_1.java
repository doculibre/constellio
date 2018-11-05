package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_1 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		// Changement de libélé
		new RMMigrationTo8_1.SchemaAlterationFor8_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor8_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor8_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		public String getVersion() {
			return "8.1";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(Printable.DEFAULT_SCHEMA).get(Printable.TITLE).setMultiLingual(true);
			//For i18n purposes
		}
	}
}