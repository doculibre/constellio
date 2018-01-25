package com.constellio.app.services.migrations.scripts;

import static com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails.OVERRIDE_INHERITED;
import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_6_1218 implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.6.1218";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {

		new CoreSchemaAlterationFor_7_6_1218(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_7_6_1218 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_1218(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder authorizationSchema = typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA);
			if (!authorizationSchema.hasMetadata(OVERRIDE_INHERITED)) {
				authorizationSchema.createUndeletable(OVERRIDE_INHERITED).setType(BOOLEAN);
			}
		}
	}
}
