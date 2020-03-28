package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo9_2_0_2 implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.0.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new SchemaAlterationFor9_2_0_2(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor9_2_0_2 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor9_2_0_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			typesBuilder.getDefaultSchema(ExternalLink.SCHEMA_TYPE).createUndeletable(ExternalLink.IMPORTED_ON).setType(MetadataValueType.DATE_TIME);
		}
	}
}
