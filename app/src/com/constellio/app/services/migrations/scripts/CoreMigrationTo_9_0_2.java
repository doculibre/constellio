package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.RecordCacheType.NOT_CACHED;

public class CoreMigrationTo_9_0_2 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreMigrationTo_9_0_2.SchemaAlterationFor9_0_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchemaType(Event.SCHEMA_TYPE).setRecordCacheType(NOT_CACHED);
			typesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE).setRecordCacheType(NOT_CACHED);
			typesBuilder.getSchemaType(SavedSearch.SCHEMA_TYPE).setRecordCacheType(NOT_CACHED);
		}
	}
}
