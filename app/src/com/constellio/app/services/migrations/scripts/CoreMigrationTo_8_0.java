package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_0 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		if (!Collection.SYSTEM_COLLECTION.equals(collection)) {
			new CoreSchemaAlterationFor_8_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}
	}

	class CoreSchemaAlterationFor_8_0 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA).create(SolrAuthorizationDetails.NEGATIVE)
					.setType(BOOLEAN);
		}
	}
}
