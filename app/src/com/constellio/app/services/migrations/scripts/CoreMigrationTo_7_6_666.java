package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.SolrAuthorizationDetails;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_6_666 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.666";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreSchemaAlterationFor_7_6_666(collection, migrationResourcesProvider, appLayerFactory).migrate();
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
	}

	class CoreSchemaAlterationFor_7_6_666 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_6_666(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder authorizationSchema = typesBuilder.getSchema(SolrAuthorizationDetails.DEFAULT_SCHEMA);
			authorizationSchema.createUndeletable(SolrAuthorizationDetails.TARGET_SCHEMA_TYPE).setType(MetadataValueType.STRING);

		}
	}
}
