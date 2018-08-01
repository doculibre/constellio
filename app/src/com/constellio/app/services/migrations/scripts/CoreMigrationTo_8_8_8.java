package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_8_8 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.8.8";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreMigrationTo_8_8_8.CoreSchemaAlterationFor_8_8_8(collection, migrationResourcesProvider, appLayerFactory)
				.migrate();
	}

	class CoreSchemaAlterationFor_8_8_8 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_8_8(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
				typeBuilder.getDefaultSchema().get(Schemas.SCHEMA_AUTOCOMPLETE_FIELD).setMultiLingual(true);
			}
		}
	}
}
