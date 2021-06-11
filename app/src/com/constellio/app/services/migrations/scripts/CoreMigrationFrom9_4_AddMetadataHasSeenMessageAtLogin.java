package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationFrom9_4_AddMetadataHasSeenMessageAtLogin extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "9.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			new CoreSchemaAlterationFrom9_4_AddMetadataHasSeenMessageAtLogin(collection, provider, appLayerFactory).migrate();
		}
	}

	private class CoreSchemaAlterationFrom9_4_AddMetadataHasSeenMessageAtLogin extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFrom9_4_AddMetadataHasSeenMessageAtLogin(String collection,
																			MigrationResourcesProvider migrationResourcesProvider,
																			AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			if (!builder.getDefaultSchema(UserCredential.SCHEMA_TYPE).hasMetadata(UserCredential.HAS_SEEN_LATEST_MESSAGE_AT_LOGIN)) {
				builder.getDefaultSchema(UserCredential.SCHEMA_TYPE)
						.createUndeletable(UserCredential.HAS_SEEN_LATEST_MESSAGE_AT_LOGIN).setType(MetadataValueType.BOOLEAN);
			}
		}
	}
}
