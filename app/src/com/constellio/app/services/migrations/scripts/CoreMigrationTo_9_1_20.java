package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_1_20 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.20";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_1_20(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_1_20 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_1_20(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (typesBuilder.getCollection().equals(Collection.SYSTEM_COLLECTION)) {
				MetadataSchemaBuilder credentialSchema =
						typesBuilder.getSchemaType(UserCredential.SCHEMA_TYPE).getDefaultSchema();

				credentialSchema.createUndeletable(UserCredential.TEAMS_FAVORITES_DISPLAY_ORDER)
						.setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);

				credentialSchema.createUndeletable(UserCredential.TEAMS_HIDDEN_FAVORITES)
						.setType(MetadataValueType.STRING).setMultivalue(true).setSystemReserved(true);
			}
		}
	}
}
