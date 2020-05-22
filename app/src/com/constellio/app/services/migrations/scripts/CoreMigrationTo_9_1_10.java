package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.ExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.SignatureExternalAccessUrl;
import com.constellio.model.entities.records.wrappers.structure.ExternalAccessUrlStatus;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_1_10 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.10";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_1_10(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_1_10 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_1_10(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			if (!typesBuilder.hasSchemaType(ExternalAccessUrl.SCHEMA_TYPE)) {
				MetadataSchemaTypeBuilder externalAccessUrlSchemaType =
						typesBuilder.createNewSchemaType(ExternalAccessUrl.SCHEMA_TYPE).setSecurity(false);
				MetadataSchemaBuilder externalAccessUrlSchema = externalAccessUrlSchemaType.getDefaultSchema();

				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.TOKEN)
						.setType(MetadataValueType.STRING);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.EXPIRATION_DATE)
						.setType(MetadataValueType.DATE);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.STATUS)
						.setType(MetadataValueType.ENUM)
						.defineAsEnum(ExternalAccessUrlStatus.class);
				externalAccessUrlSchema.createUndeletable(ExternalAccessUrl.ACCESS_RECORD)
						.setType(MetadataValueType.STRING);


				MetadataSchemaBuilder externalSignatureUrlSchema =
						typesBuilder.getSchemaType(SignatureExternalAccessUrl.SCHEMA_TYPE)
								.createCustomSchema(SignatureExternalAccessUrl.SCHEMA);
			}
		}
	}
}
