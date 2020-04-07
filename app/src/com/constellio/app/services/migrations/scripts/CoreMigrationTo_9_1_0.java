package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.records.wrappers.User.AZURE_USER;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;

public class CoreMigrationTo_9_1_0 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.0";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor_9_1_0(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_1_0 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_1_0(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (Collection.SYSTEM_COLLECTION.equals(typesBuilder.getCollection())) {
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).createIfInexisting(UserCredential.AZURE_USERNAME,
						(m) -> m.setUniqueValue(true).setType(STRING));
			}

			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE)
					.createIfInexisting(AZURE_USER, (m) -> m.setUniqueValue(true).setType(STRING));

			if (typesBuilder.getCollection().equals(Collection.SYSTEM_COLLECTION)) {
				MetadataSchemaBuilder credentialsSchema = typesBuilder.getSchemaType(UserCredential.SCHEMA_TYPE).getDefaultSchema();
				credentialsSchema.createIfInexisting(UserCredential.ELECTRONIC_SIGNATURE, (m) -> m.setType(CONTENT));
				credentialsSchema.createIfInexisting(UserCredential.ELECTRONIC_INITIALS, (m) -> m.setType(CONTENT));
			}
		}
	}
}
