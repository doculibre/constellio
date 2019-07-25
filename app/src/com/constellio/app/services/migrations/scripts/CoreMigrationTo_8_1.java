package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.security.global.UserCredential.HAS_AGREED_TO_PRIVACY_POLICY;

public class CoreMigrationTo_8_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {

		if (Collection.SYSTEM_COLLECTION.equals(collection)) {
			new CoreSchemaAlterationFor8_1(collection, provider, appLayerFactory).migrate();
		}
	}

	private class CoreSchemaAlterationFor8_1 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor8_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			if (!builder.getDefaultSchema(UserCredential.SCHEMA_TYPE).hasMetadata(HAS_AGREED_TO_PRIVACY_POLICY)) {
				builder.getDefaultSchema(UserCredential.SCHEMA_TYPE)
						.createUndeletable(HAS_AGREED_TO_PRIVACY_POLICY).setType(MetadataValueType.BOOLEAN);
			}
		}
	}
}
