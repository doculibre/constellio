package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_1_2 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_1_2 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_1_2(String collection,
				MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder searchEvent = typesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE);
			searchEvent.setDataStore(DataStore.EVENTS);
			searchEvent.createMetadata(SearchEvent.LAST_PAGE_NAVIGATION).setType(MetadataValueType.NUMBER);
		}
	}
}
