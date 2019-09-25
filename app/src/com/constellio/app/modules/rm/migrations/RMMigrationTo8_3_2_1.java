package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class RMMigrationTo8_3_2_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.2.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor8_3_2_1(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_3_2_1 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_3_2_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										  AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaBuilder cartSchema = types().getSchema(Cart.DEFAULT_SCHEMA);
			cartSchema.get(Cart.TITLE).required();

			MetadataSchemaBuilder folderSchema = types().getSchema(Folder.DEFAULT_SCHEMA);
			folderSchema.get(Folder.MAIN_COPY_RULE_ID_ENTERED).setEssential(true);
		}
	}
}
