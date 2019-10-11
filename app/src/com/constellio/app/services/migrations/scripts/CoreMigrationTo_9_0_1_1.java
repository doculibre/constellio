package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_0_1_1 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.1.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreMigrationTo_9_0_1_1.SchemaAlterationFor9_0_1_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_1_1 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_1_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder savedSearchSchema = typesBuilder.getSchemaType(SavedSearch.SCHEMA_TYPE).getDefaultSchema();
			savedSearchSchema.createUndeletable(SavedSearch.SCHEMA_CODE_FILTER).setType(MetadataValueType.STRING);
		}
	}
}
