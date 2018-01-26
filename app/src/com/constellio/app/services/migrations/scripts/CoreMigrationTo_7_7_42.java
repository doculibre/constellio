package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.records.DataStore;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_42 implements MigrationScript {

	private static final Logger LOGGER = LoggerFactory.getLogger(CoreMigrationTo_7_7_42.class);

	@Override
	public String getVersion() {
		return "7.7.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_7_42(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	class CoreSchemaAlterationFor_7_7_42 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_42(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaTypeBuilder searchEvent = typesBuilder.createNewSchemaType(SearchEvent.SCHEMA_TYPE);
			searchEvent.setDataStore(DataStore.EVENTS);
			searchEvent.createMetadata(SearchEvent.USERNAME).setType(STRING);
			searchEvent.createMetadata(SearchEvent.QUERY).setType(STRING);
			searchEvent.createMetadata(SearchEvent.PAGE_NAVIGATION_COUNT).setType(NUMBER);
			searchEvent.createMetadata(SearchEvent.CLICK_COUNT).setType(NUMBER);
			searchEvent.createMetadata(SearchEvent.PARAMS).setType(STRING).setMultivalue(true);
			searchEvent.createMetadata(SearchEvent.ORIGINAL_QUERY).setType(STRING);
			searchEvent.createMetadata(SearchEvent.NUM_FOUND).setType(NUMBER);
			searchEvent.createMetadata(SearchEvent.Q_TIME).setType(NUMBER);
		}
	}
}
