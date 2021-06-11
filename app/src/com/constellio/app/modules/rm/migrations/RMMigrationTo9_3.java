package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

public class RMMigrationTo9_3 implements MigrationScript {

	private String collection;
	private SchemasDisplayManager displayManager;

	@Override
	public String getVersion() {
		return "9.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory) throws Exception {
		new SchemaAlterationFor9_3(collection, migrationResourcesProvider, appLayerFactory).migrate();

		this.collection = collection;
		displayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		updateDecomListDisplayConfig();
	}

	private void updateDecomListDisplayConfig() {
		SchemaDisplayConfig config = displayManager.getSchema(collection, DecommissioningList.DEFAULT_SCHEMA);

		displayManager.saveSchema(config
				.withNewDisplayMetadatas(DecommissioningList.DEFAULT_SCHEMA + "_" + DecommissioningList.SUPER_USER));
	}

	private class SchemaAlterationFor9_3 extends MetadataSchemasAlterationHelper {
		SchemaAlterationFor9_3(String collection, MigrationResourcesProvider migrationResourcesProvider,
							   AppLayerFactory appLayerFactory) {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder userSchema = typesBuilder.getSchemaType(User.SCHEMA_TYPE).getDefaultSchema();
			MetadataSchemaBuilder decomListSchema =
					typesBuilder.getSchemaType(DecommissioningList.SCHEMA_TYPE).getDefaultSchema();

			decomListSchema.createUndeletable(DecommissioningList.SUPER_USER)
					.setType(REFERENCE)
					.defineReferencesTo(userSchema);
		}
	}
}
