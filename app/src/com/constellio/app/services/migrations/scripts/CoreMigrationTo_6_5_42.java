package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.SolrUserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_5_42 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			MetadataSchema userCredentialSchema = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
					.getSchemaTypes(collection).getSchema(SolrUserCredential.DEFAULT_SCHEMA);

			new CoreSchemaAlterationFor6_5_42(collection, migrationResourcesProvider, appLayerFactory).migrate();
		}

	}

	private class CoreSchemaAlterationFor6_5_42 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_5_42(String collection, MigrationResourcesProvider migrationResourcesProvider,
				AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder credentialsSchemaBuilder = builder.getSchema(SolrUserCredential.DEFAULT_SCHEMA);
			if (!credentialsSchemaBuilder.hasMetadata(SolrUserCredential.PERSONAL_EMAILS)) {
				credentialsSchemaBuilder.createUndeletable(SolrUserCredential.PERSONAL_EMAILS).setType(MetadataValueType.STRING)
						.setMultivalue(true);
			}
		}

	}
}
