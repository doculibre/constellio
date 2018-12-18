package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Printable;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_1_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_8_1_3(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_8_1_3 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_1_3(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder printableSchema = typesBuilder.getDefaultSchema(Printable.SCHEMA_TYPE);
			printableSchema.get(Schemas.TITLE_CODE).setDefaultRequirement(true);
			printableSchema.get(Printable.JASPERFILE).setDefaultRequirement(true);
		}
	}
}
