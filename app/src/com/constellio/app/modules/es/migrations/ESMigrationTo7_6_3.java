package com.constellio.app.modules.es.migrations;

import java.util.List;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataList;

public class ESMigrationTo7_6_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider, AppLayerFactory appLayerFactory)
			throws Exception {
		List<MetadataSchemaType> schemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes(collection).getSchemaTypes();
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchemaType type : schemaTypes) {
			List<MetadataSchema> allSchemas = type.getAllSchemas();
			for (MetadataSchema schema : allSchemas) {
				MetadataList metadataList = schema.getMetadatas().onlySearchable();
				for (Metadata metadata : metadataList) {
					transaction.add(metadataSchemasDisplayManager.getMetadata(collection, metadata.getCode())
							.withHighlightStatus(true));
				}
			}
		}
		metadataSchemasDisplayManager.execute(transaction);
	}
}
