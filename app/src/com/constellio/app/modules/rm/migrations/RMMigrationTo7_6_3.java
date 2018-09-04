package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.Schemas;

import static com.constellio.app.modules.rm.RMTypes.rmSchemas;

public class RMMigrationTo7_6_3 extends MigrationHelper implements MigrationScript {
	@Override
	public String getVersion() {
		return "7.6.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		SchemasDisplayManager schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();

		//Transaction 1
		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchema schema : rmSchemas(appLayerFactory, collection)) {
			if (schema.hasMetadataWithCode(Schemas.PATH.getLocalCode())) {
				transaction.add(schemasDisplayManager.getMetadata(collection, schema.get(Schemas.PATH.getLocalCode()).getCode())
						.withVisibleInAdvancedSearchStatus(true));
			}
		}
		schemasDisplayManager.execute(transaction);

		//Transaction 2
		transaction = new SchemaDisplayManagerTransaction();
		for (MetadataSchema schema : rmSchemas(appLayerFactory, collection)) {
			for (Metadata metadata : schema.getMetadatas().onlySearchable()) {
				transaction.add(schemasDisplayManager.getMetadata(collection, metadata.getCode()).withHighlightStatus(true));
			}
		}
		schemasDisplayManager.execute(transaction);
	}
}
