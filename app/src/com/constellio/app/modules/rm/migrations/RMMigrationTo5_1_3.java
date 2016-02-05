package com.constellio.app.modules.rm.migrations;

import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;

public class RMMigrationTo5_1_3 implements MigrationScript {

	@Override
	public String getVersion() {
		return "5.1.3";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
			AppLayerFactory appLayerFactory)
			throws Exception {
		setupDisplayConfig(collection, appLayerFactory);
	}

	private void setupDisplayConfig(String collection, AppLayerFactory appLayerFactory) {
		SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
		SchemaTypesDisplayTransactionBuilder builder = manager.newTransactionBuilderFor(collection);

		SchemaDisplayManagerTransaction transaction = builder
				.in(ContainerRecord.SCHEMA_TYPE)
				.addToForm(ContainerRecord.DECOMMISSIONING_TYPE, ContainerRecord.ADMINISTRATIVE_UNIT)
				.beforeMetadata(ContainerRecord.STORAGE_SPACE)
				.in(Document.SCHEMA_TYPE)
				.addToForm(Document.FOLDER).atFirstPosition()
				.in(DocumentType.SCHEMA_TYPE)
				.addToDisplay(DocumentType.TEMPLATES).atTheEnd()
				.build()
				.add(manager.getMetadata(collection, ContainerRecord.DEFAULT_SCHEMA, ContainerRecord.ADMINISTRATIVE_UNIT)
						.withInputType(MetadataInputType.LOOKUP))
				.add(manager.getMetadata(collection, ContainerRecord.DEFAULT_SCHEMA, ContainerRecord.DECOMMISSIONING_TYPE)
						.withInputType(MetadataInputType.DROPDOWN));

		manager.execute(transaction);
	}

}
