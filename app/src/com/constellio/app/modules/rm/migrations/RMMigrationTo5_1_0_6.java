package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo5_1_0_6 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.0.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {

		new SchemaAlterationFor5_1_0_6(collection, migrationResourcesProvider, appLayerFactory).migrate();

	}

	class SchemaAlterationFor5_1_0_6 extends MetadataSchemasAlterationHelper {
		MetadataSchemaTypes types;

		protected SchemaAlterationFor5_1_0_6(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
			types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		}

		public String getVersion() {
			return "5.1.0.6";
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaBuilder retentionRuleSchemaBuilder = typesBuilder.getSchema(RetentionRule.DEFAULT_SCHEMA);

			retentionRuleSchemaBuilder.getMetadata(RetentionRule.TITLE).setUniqueValue(false);
		}
	}
}
