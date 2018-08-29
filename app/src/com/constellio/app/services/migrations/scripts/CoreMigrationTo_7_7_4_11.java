package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_7_7_4_11 implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.7.4.11";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		if (collection.equals(Collection.SYSTEM_COLLECTION)) {
			new CoreSchemaAlterationFor_7_7_4_11(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}
	}

	class CoreSchemaAlterationFor_7_7_4_11 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_7_7_4_11(String collection,
												   MigrationResourcesProvider migrationResourcesProvider,
												   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder defaultSchema = typesBuilder.getDefaultSchema(SolrUserCredential.SCHEMA_TYPE);
			defaultSchema.get(SolrUserCredential.SERVICE_KEY).setUniqueValue(true);
		}
	}
}
