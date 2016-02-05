package com.constellio.app.services.migrations.scripts;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class CoreMigrationTo_5_0_6_6 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.0.6.6";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory) {

		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();

		MetadataSchemaTypes types = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			for (MetadataSchema schema : type.getCustomSchemas()) {
				SchemaDisplayConfig schemaConfig = manager.getSchema(collection, schema.getCode());
				if (schemaConfig.getDisplayMetadataCodes().contains(schema.getCode() + "_" + Schemas.TOKENS.getLocalCode())) {
					manager.resetSchema(collection, schema.getCode());
				}
			}
		}

	}

}


