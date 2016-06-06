package com.constellio.app.modules.es.migrations;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class ESMigrationTo6_4 implements MigrationScript {
	@Override
	public String getVersion() {
		return "6.4";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider provider, AppLayerFactory factory)
			throws Exception {
		configureTableMetadatas(collection, factory);
	}

	private void configureTableMetadatas(String collection, AppLayerFactory factory) {
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		MetadataSchemasManager metadataSchemasManager = factory.getModelLayerFactory().getMetadataSchemasManager();
		List<MetadataSchemaType> schemaTypes = metadataSchemasManager
				.getSchemaTypes(collection).getSchemaTypes();
		SchemasDisplayManager manager = factory.getMetadataSchemasDisplayManager();
		for (MetadataSchemaType metadataSchemaType : schemaTypes) {

			for (MetadataSchema metadataSchema : metadataSchemaType.getCustomSchemas()) {
				SchemaDisplayConfig customConfig = manager.getSchema(collection, metadataSchema.getCode());
				SchemaDisplayConfig newCustomConfig = customConfig.withTableMetadataCodes(new ArrayList<String>());
				transaction.add(newCustomConfig);
			}

			SchemaDisplayConfig config = manager.getSchema(collection, metadataSchemaType.getDefaultSchema().getCode());
			SchemaDisplayConfig newConfig = config.withTableMetadataCodes(config.getSearchResultsMetadataCodes());
			transaction.add(newConfig);
		}
		manager.execute(transaction);
	}

}
