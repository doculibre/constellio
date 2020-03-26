package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_9_1_2 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "9.1.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new CoreMigrationTo_9_1_2.SchemaAlterationFor_9_1_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class SchemaAlterationFor_9_1_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor_9_1_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
											AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {

			if (Collection.SYSTEM_COLLECTION.equals(typesBuilder.getCollection())) {
				typesBuilder.getDefaultSchema(UserCredential.SCHEMA_TYPE).createUndeletable(UserCredential.AZURE_USERNAME)
						.setUniqueValue(true).setType(MetadataValueType.STRING).setDefaultValue("");
			}

			typesBuilder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.AZURE_USER)
					.setUniqueValue(true).setType(MetadataValueType.STRING).setDefaultValue("");
		}
	}
}
