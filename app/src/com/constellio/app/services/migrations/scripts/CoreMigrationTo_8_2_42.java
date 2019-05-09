package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.CommonMetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_8_2_42 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.2.42";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		appLayerFactory.getSystemGlobalConfigsManager().setReindexingRequired(true);
		new CoreSchemaAlterationFor_8_2_42(collection, migrationResourcesProvider, appLayerFactory).migrate();
	}

	class CoreSchemaAlterationFor_8_2_42 extends MetadataSchemasAlterationHelper {

		protected CoreSchemaAlterationFor_8_2_42(String collection,
												 MigrationResourcesProvider migrationResourcesProvider,
												 AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			new CommonMetadataBuilder().addCommonMetadataToAllExistingSchemas(typesBuilder);
			//			for (MetadataSchemaTypeBuilder typeBuilder : typesBuilder.getTypes()) {
			//				if (!typeBuilder.getDefaultSchema().hasMetadata(Schemas.HIDDEN.getLocalCode())) {
			//					CommonMetadataBuilder
			//				}
			//			}
		}
	}
}
