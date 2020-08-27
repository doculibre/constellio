package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.security.global.SystemWideGroup;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class CoreMigrationTo_6_5_19 implements MigrationScript {

	@Override
	public String getVersion() {
		return "6.5.19";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		//
		new AddGlobalGroupLocallyCreatedMetadata(collection, provider, appLayerFactory).migrate();

	}

	private class AddGlobalGroupLocallyCreatedMetadata extends MetadataSchemasAlterationHelper {
		public AddGlobalGroupLocallyCreatedMetadata(String collection, MigrationResourcesProvider provider,
													AppLayerFactory appLayerFactory) {
			super(collection, provider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder metadataSchemaTypesBuilder) {
			if (metadataSchemaTypesBuilder.hasSchemaType(SystemWideGroup.SCHEMA_TYPE)) {
				// Add metadata to schema
				final MetadataSchemaBuilder metadataSchemaBuilder = metadataSchemaTypesBuilder
						.getSchema(SystemWideGroup.DEFAULT_SCHEMA);
				if (!metadataSchemaBuilder.hasMetadata(SystemWideGroup.LOCALLY_CREATED)) {
					metadataSchemaBuilder.createUndeletable(SystemWideGroup.LOCALLY_CREATED).setType(MetadataValueType.BOOLEAN);
				}
			}
		}
	}

}
