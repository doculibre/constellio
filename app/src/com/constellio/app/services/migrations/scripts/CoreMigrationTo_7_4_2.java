package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.enums.SearchPageLength;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_4_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.4.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor7_4_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private class CoreSchemaAlterationFor7_4_2 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor7_4_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			migrateTemporaryRecord(typesBuilder);
		}

		private void migrateTemporaryRecord(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.DEFAULT_PAGE_LENGTH).setType(MetadataValueType.ENUM)
						.defineAsEnum(SearchPageLength.class).setDefaultValue(SearchPageLength.TEN);
		}
	}
}