package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_5_22 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.22";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) {
		new CoreSchemaAlterationFor6_5_22(collection, migrationResourcesProvider, appLayerFactory).migrate();

		SchemasDisplayManager displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		MetadataDisplayConfig displayConfig;

		displayConfig = displayManager.getMetadata(collection, User.DEFAULT_SCHEMA + "_" + User.PERSONAL_EMAILS)
									  .withInputType(MetadataInputType.TEXTAREA);

		displayManager.saveMetadata(displayConfig);
	}

	private class CoreSchemaAlterationFor6_5_22 extends MetadataSchemasAlterationHelper {
		public CoreSchemaAlterationFor6_5_22(String collection, MigrationResourcesProvider migrationResourcesProvider,
											 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {

			builder.getDefaultSchema(User.SCHEMA_TYPE)
				   .create(User.PERSONAL_EMAILS).
						   setType(MetadataValueType.STRING).
						   setMultivalue(true).
						   setEnabled(true).
						   setEssential(false);
		}

	}
}
