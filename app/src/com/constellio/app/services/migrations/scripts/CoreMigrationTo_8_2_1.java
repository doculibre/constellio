package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_2_1 implements MigrationScript {
	@Override
	public String getVersion() {
		return "8.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		//for i18ns
		new SchemaAlterationsFor8_2_1(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	private static class SchemaAlterationsFor8_2_1 extends MetadataSchemasAlterationHelper {
		protected SchemaAlterationsFor8_2_1(String collection, MigrationResourcesProvider provider,
											AppLayerFactory factory) {
			super(collection, provider, factory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			builder.getDefaultSchema(User.SCHEMA_TYPE).createUndeletable(User.TAXONOMY_DISPLAY_ORDER)
					.setType(MetadataValueType.STRING).setMultivalue(true);
		}
	}
}