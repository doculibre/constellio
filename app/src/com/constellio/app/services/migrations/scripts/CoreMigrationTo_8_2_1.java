package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

public class CoreMigrationTo_8_2_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreMigrationTo_8_2_1.SchemaAlterationFor8_2_1(collection, migrationResourcesProvider, appLayerFactory)
				.migrate();
	}

	class SchemaAlterationFor8_2_1 extends MetadataSchemasAlterationHelper {

		private SchemaAlterationFor8_2_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder type = typesBuilder.getSchemaType(Event.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
			defaultSchema.createUndeletable(Event.NEGATIVE_AUTHORIZATION).setType(BOOLEAN);
		}
	}
}
