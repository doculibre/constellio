package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_3 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor6_3(collection, provider, appLayerFactory).migrate();
	}

	private class CoreSchemaAlterationFor6_3 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder type = typesBuilder.getSchemaType(SavedSearch.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = type.getDefaultSchema();
			defaultSchema.createUndeletable(SavedSearch.TEMPORARY).setType(BOOLEAN);
			defaultSchema.createUndeletable(SavedSearch.PAGE_NUMBER).setType(NUMBER);
		}
	}
}
