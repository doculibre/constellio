package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Event;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static org.apache.ignite.internal.util.lang.GridFunc.asList;

public class CoreMigrationTo_9_0_42_2 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.0.42.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor9_0_42_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor9_0_42_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor9_0_42_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			for (String schemaTypeCode : asList(Event.SCHEMA_TYPE, SearchEvent.SCHEMA_TYPE, SavedSearch.SCHEMA_TYPE)) {
				MetadataSchemaBuilder schema = typesBuilder.getDefaultSchema(schemaTypeCode);

				for (MetadataBuilder metadataBuilder : schema.getMetadatas()) {
					if (metadataBuilder.getDataEntry().getType() != DataEntryType.MANUAL) {
						metadataBuilder.defineDataEntry().asManual();
					}
				}
			}
		}
	}
}
