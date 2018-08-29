package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class CoreMigrationTo_7_7_7 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.7";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new CoreMigrationTo_7_7_7.CoreSchemaAlterationFor_7_7_7(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_7_7 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_7(String collection,
												MigrationResourcesProvider migrationResourcesProvider,
												AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder searchEvent = typesBuilder.getSchemaType(SearchEvent.SCHEMA_TYPE).getDefaultSchema();
			if (!searchEvent.hasMetadata(SearchEvent.CLICKS)) {
				searchEvent.create(SearchEvent.CLICKS).setType(STRING).setMultivalue(true);
			}
		}
	}
}
