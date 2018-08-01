package com.constellio.app.modules.es.migrations;

import com.constellio.app.entities.modules.MetadataSchemasAlterationHelper;
import com.constellio.app.entities.modules.MigrationHelper;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.modules.es.model.connectors.DocumentSmbConnectorUrlCalculator;
import com.constellio.app.modules.es.model.connectors.DocumentSmbParentConnectorUrlCalculator;
import com.constellio.app.modules.es.model.connectors.FolderSmbConnectorUrlCalculator;
import com.constellio.app.modules.es.model.connectors.FolderSmbParentConnectorUrlCalculator;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;

public class ESMigrationTo7_4_2 extends MigrationHelper implements MigrationScript {

	@Override
	public String getVersion() {
		return "7.4.2";
	}

	@Override
	public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
						AppLayerFactory appLayerFactory)
			throws Exception {
		new SchemaAlterationFor7_4_2(collection, migrationResourcesProvider, appLayerFactory).migrate();
		SchemasDisplayManager metadataSchemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbDocument.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.CONNECTOR_URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbDocument.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.PARENT_CONNECTOR_URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbDocument.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.PARENT_URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbDocument.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.LAST_MODIFIED));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbDocument.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbDocument.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.SIZE));


		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbFolder.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.CONNECTOR_URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbFolder.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.PARENT_CONNECTOR_URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbFolder.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.PARENT_URL));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbFolder.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.LAST_MODIFIED));
		metadataSchemasDisplayManager.saveSchema(metadataSchemasDisplayManager.getSchema(collection, ConnectorSmbFolder.DEFAULT_SCHEMA)
																			  .withNewSearchResultMetadataCode(ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.URL));

		metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + ConnectorSmbFolder.PARENT_CONNECTOR_URL)
																				.withVisibleInAdvancedSearchStatus(true));
		metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, ConnectorSmbFolder.DEFAULT_SCHEMA + "_" + Schemas.PATH.getLocalCode())
																				.withVisibleInAdvancedSearchStatus(false));
		metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + ConnectorSmbDocument.PARENT_CONNECTOR_URL)
																				.withVisibleInAdvancedSearchStatus(true));
		metadataSchemasDisplayManager.saveMetadata(metadataSchemasDisplayManager.getMetadata(collection, ConnectorSmbDocument.DEFAULT_SCHEMA + "_" + Schemas.PATH.getLocalCode())
																				.withVisibleInAdvancedSearchStatus(false));
	}

	static class SchemaAlterationFor7_4_2 extends MetadataSchemasAlterationHelper {

		protected SchemaAlterationFor7_4_2(String collection, MigrationResourcesProvider migrationResourcesProvider,
										   AppLayerFactory appLayerFactory)
				throws RecordServicesException {
			super(collection, migrationResourcesProvider, appLayerFactory);
		}

		@Override
		protected void migrate(MetadataSchemaTypesBuilder typesBuilder) {
			MetadataSchemaBuilder documentDefaultSchema = typesBuilder.getSchemaType(ConnectorSmbDocument.SCHEMA_TYPE).getDefaultSchema();
			documentDefaultSchema.create(ConnectorSmbDocument.CONNECTOR_URL).setType(MetadataValueType.STRING).setUniqueValue(true)
								 .setSystemReserved(true).setEssentialInSummary(true).defineDataEntry().asCalculated(DocumentSmbConnectorUrlCalculator.class)
								 .setDefaultRequirement(true);
			documentDefaultSchema.create(ConnectorSmbDocument.PARENT_URL).setType(MetadataValueType.STRING)
								 .setSystemReserved(true).setEssentialInSummary(true);
			documentDefaultSchema.create(ConnectorSmbDocument.PARENT_CONNECTOR_URL).setType(MetadataValueType.STRING)
								 .setSystemReserved(true).setEssentialInSummary(true)
								 .setSearchable(true).defineDataEntry().asCalculated(DocumentSmbParentConnectorUrlCalculator.class);
			documentDefaultSchema.get(ConnectorSmbDocument.LAST_MODIFIED).setEssentialInSummary(true);
			documentDefaultSchema.get(ConnectorSmbDocument.URL).setEssentialInSummary(true);
			documentDefaultSchema.get(ConnectorSmbDocument.PERMISSIONS_HASH).setEssentialInSummary(true);
			documentDefaultSchema.get(ConnectorSmbDocument.SIZE).setEssentialInSummary(true);
			documentDefaultSchema.deleteMetadataWithoutValidation("parent");

			MetadataSchemaBuilder folderDefaultSchema = typesBuilder.getSchemaType(ConnectorSmbFolder.SCHEMA_TYPE).getDefaultSchema();
			folderDefaultSchema.create(ConnectorSmbFolder.CONNECTOR_URL).setType(MetadataValueType.STRING).setUniqueValue(true)
							   .setSystemReserved(true).setEssentialInSummary(true).defineDataEntry().asCalculated(FolderSmbConnectorUrlCalculator.class)
							   .setDefaultRequirement(true);
			folderDefaultSchema.create(ConnectorSmbFolder.PARENT_URL).setType(MetadataValueType.STRING)
							   .setSystemReserved(true).setEssentialInSummary(true).setSearchable(true);
			folderDefaultSchema.create(ConnectorSmbFolder.PARENT_CONNECTOR_URL).setType(MetadataValueType.STRING)
							   .setSystemReserved(true).setEssentialInSummary(true).defineDataEntry().asCalculated(FolderSmbParentConnectorUrlCalculator.class);
			folderDefaultSchema.deleteMetadataWithoutValidation("parent");
		}
	}
}
