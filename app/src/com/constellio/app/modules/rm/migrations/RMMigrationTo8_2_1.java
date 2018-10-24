package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_2_1 implements MigrationScript {
	private static final String FOLDERS = "folders";
	private static final String DOCUMENTS = "documents";
	private static final String CONTAINERS = "containers";

	@Override
	public String getVersion() {
		return "8.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_2(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_2 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
									  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypes metadataSchemaTypes = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(collection);
			if (metadataSchemaTypes.hasMetadata(Cart.DEFAULT_SCHEMA + "_" + FOLDERS)) {
				builder.getDefaultSchema(Cart.SCHEMA_TYPE).deleteMetadataWithoutValidation(FOLDERS);
			}
			if (metadataSchemaTypes.hasMetadata(Cart.DEFAULT_SCHEMA + "_" + DOCUMENTS)) {
				builder.getDefaultSchema(Cart.SCHEMA_TYPE).deleteMetadataWithoutValidation(DOCUMENTS);
			}
			if (metadataSchemaTypes.hasMetadata(Cart.DEFAULT_SCHEMA + "_" + CONTAINERS)) {
				builder.getDefaultSchema(Cart.SCHEMA_TYPE).deleteMetadataWithoutValidation(CONTAINERS);
			}
		}
	}
}
