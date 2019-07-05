package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;

public class RMMigrationTo8_3_1 implements MigrationScript {

	@Override
	public String getVersion() {
		return "8.3.1";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory appLayerFactory)
			throws Exception {
		new RMMigrationTo8_3_1.SchemaAlterationFor8_3_1(collection, provider, appLayerFactory).migrate();
	}

	private class SchemaAlterationFor8_3_1 extends MetadataSchemasAlterationHelper {
		public SchemaAlterationFor8_3_1(String collection, MigrationResourcesProvider migrationResourcesProvider,
										AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder builder) {
			MetadataSchemaTypeBuilder schemaType = types().getSchemaType(DecommissioningList.SCHEMA_TYPE);
			MetadataSchemaBuilder defaultSchema = schemaType.getDefaultSchema();
			if (!defaultSchema.hasMetadata(DecommissioningList.CONTENTS)) {
				defaultSchema.createUndeletable(DecommissioningList.CONTENTS).setType(CONTENT).setMultivalue(true);

				SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
				SchemaDisplayConfig schemaDisplayConfig = manager.getSchema(collection, DecommissioningList.DEFAULT_SCHEMA);
				schemaDisplayConfig = schemaDisplayConfig.withNewFormMetadata(DecommissioningList.DEFAULT_SCHEMA + "_" + DecommissioningList.CONTENTS);
				manager.saveSchema(schemaDisplayConfig);
			}
		}
	}
}
