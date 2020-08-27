package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.SignatureExternalAccessUrl;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;


public class RMMigrationTo9_2_11 implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.2.11";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_2_11(collection, migrationResourcesProvider, appLayerFactory).migrate();

		appLayerFactory.getSystemGlobalConfigsManager().markLocalCachesAsRequiringRebuild();
	}

	private class SchemaAlterationFor9_2_11 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationFor9_2_11(String collection,
											MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			MetadataSchemaTypeBuilder externalAccessUrl = typesBuilder.getSchemaType(ExternalAccessUrl.SCHEMA_TYPE);
			if (!externalAccessUrl.hasSchema(SignatureExternalAccessUrl.SCHEMA)) {
				externalAccessUrl.createCustomSchema(SignatureExternalAccessUrl.SCHEMA);
			}
		}
	}

}
