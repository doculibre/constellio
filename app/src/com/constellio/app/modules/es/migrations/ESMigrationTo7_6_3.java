package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.ESTypes;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataList;

public class ESMigrationTo7_6_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		//Transaction 1
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchema schema : ESTypes.esSchemas(appLayerFactory, collection)) {
			if (schema.hasMetadataWithCode(Schemas.PATH.getLocalCode())) {
				transaction.add(metadataSchemasDisplayManager.getMetadata(collection,
						schema.get(Schemas.PATH.getLocalCode()).getCode()).withVisibleInAdvancedSearchStatus(true));
			}
		}
		metadataSchemasDisplayManager.execute(transaction);

		//Transaction 2
		transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchema schema : ESTypes.esSchemas(appLayerFactory, collection)) {
			MetadataList metadataList = schema.getMetadatas().onlySearchable();
			for (Metadata metadata : metadataList) {
				transaction.add(metadataSchemasDisplayManager.getMetadata(collection, metadata.getCode())
						.withHighlightStatus(true));
			}
		}
		metadataSchemasDisplayManager.execute(transaction);

	}
}
