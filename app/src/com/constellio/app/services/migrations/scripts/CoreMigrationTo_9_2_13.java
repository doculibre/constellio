package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_2_13 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.2.13";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_2_13(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_2_13 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_2_13(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder externalAccessUrlSchema = typesBuilder.getSchemaType(ExternalAccessUrl.SCHEMA_TYPE)
					.getDefaultSchema();
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();

			if (!externalAccessUrlSchema.hasMetadata(ExternalAccessUrl.USER)) {
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.USER)
						.setType(MetadataValueType.REFERENCE)
						.defineReferencesTo(userSchema);
			}

			if (!externalAccessUrlSchema.hasMetadata(ExternalAccessUrl.ROLES)) {
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.ROLES)
						.setType(MetadataValueType.STRING)
						.setMultivalue(true);
			}
		}
	}
}
