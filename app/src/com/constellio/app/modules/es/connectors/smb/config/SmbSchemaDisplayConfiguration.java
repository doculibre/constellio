package com.constellio.app.modules.es.connectors.smb.config;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemaDisplayManagerTransaction;
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;

public class SmbSchemaDisplayConfiguration {
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;

	public SmbSchemaDisplayConfiguration(ESSchemasRecordsServices es, ConnectorSmbInstance connectorInstance) {
		this.es = es;
		this.connectorInstance = connectorInstance;
	}

	private ESSchemasRecordsServices getEs() {
		return es;
	}

	public void setupMetadatasDisplay() {
		// TODO Benoit. Update only once / if needed
		SchemasDisplayManager manager = getEs().getMetadataSchemasDisplayManager();

		SchemaDisplayManagerTransaction transaction = new SchemaDisplayManagerTransaction();

		// Connector SMB Config/Instance
		SchemaDisplayConfig schemaFormFolderTypeConfig = order(connectorInstance.getCollection(), getEs().getAppLayerFactory(), "form",
				manager.getSchema(getEs().getCollection(), getEs().getConnectorSmbDocumentSchemaCode(connectorInstance)), ConnectorDocument.TITLE,
				ConnectorSmbDocument.URL, ConnectorSmbDocument.PARENT);

		SchemaDisplayConfig schemaDisplayFolderTypeConfig = order(connectorInstance.getCollection(), getEs().getAppLayerFactory(), "display",
				manager.getSchema(getEs().getCollection(), getEs().getConnectorSmbDocumentSchemaCode(connectorInstance)), ConnectorDocument.TITLE,
				ConnectorSmbDocument.URL, ConnectorSmbDocument.PARENT);

		transaction.add(schemaDisplayFolderTypeConfig.withFormMetadataCodes(schemaFormFolderTypeConfig.getFormMetadataCodes()));

		manager.execute(transaction);

		SchemaTypesDisplayTransactionBuilder transactionBuilder = manager.newTransactionBuilderFor(connectorInstance.getCollection());
		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.URL)
		//				.afterMetadata(ConnectorDocument.TITLE);

		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.PARENT)
		//				.afterMetadata(ConnectorSmbDocument.TITLE);

		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.EXTENSION)
		//				.afterMetadata(ConnectorSmbDocument.PARENT);

		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.LANGUAGE)
		//				.afterMetadata(ConnectorSmbDocument.EXTENSION);
		//
		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.SIZE)
		//				.afterMetadata(ConnectorSmbDocument.LANGUAGE);

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
				.addToSearchResult(ConnectorSmbDocument.LAST_MODIFIED)
				.afterMetadata(ConnectorSmbDocument.TITLE);

		String modifiedOn = Schemas.MODIFIED_ON.getLocalCode();

		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE).removeFromSearchResult(modifiedOn);

		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.LAST_FETCH_ATTEMPT)
		//				.afterMetadata(ConnectorSmbDocument.LAST_MODIFIED);
		//
		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS)
		//				.afterMetadata(ConnectorSmbDocument.LAST_FETCH_ATTEMPT);
		//
		//		transactionBuilder.in(ConnectorSmbDocument.SCHEMA_TYPE)
		//				.addToSearchResult(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_DETAILS)
		//				.afterMetadata(ConnectorSmbDocument.LAST_FETCH_ATTEMPT_STATUS);

		manager.execute(transactionBuilder.build());
	}

	protected SchemaDisplayConfig order(String collection, AppLayerFactory appLayerFactory, String type, SchemaDisplayConfig schema, String... localCodes) {

		MetadataSchemaTypes schemaTypes = appLayerFactory.getModelLayerFactory()
				.getMetadataSchemasManager()
				.getSchemaTypes(collection);

		List<String> visibleMetadataCodes = new ArrayList<>();
		for (String localCode : localCodes) {
			visibleMetadataCodes.add(schema.getSchemaCode() + "_" + localCode);
		}
		List<String> metadataCodes = new ArrayList<>();
		metadataCodes.addAll(visibleMetadataCodes);
		List<String> otherMetadatas = new ArrayList<>();
		List<String> retrievedMetadataCodes;
		if ("form".equals(type)) {
			retrievedMetadataCodes = schema.getFormMetadataCodes();
		} else {
			retrievedMetadataCodes = schema.getDisplayMetadataCodes();
		}
		for (String retrievedMetadataCode : retrievedMetadataCodes) {
			int index = visibleMetadataCodes.indexOf(retrievedMetadataCode);
			if (index != -1) {
				metadataCodes.set(index, retrievedMetadataCode);
			} else if (!schemaTypes.getMetadata(retrievedMetadataCode)
					.isSystemReserved()) {
				otherMetadatas.add(retrievedMetadataCode);
			}
		}
		SchemaDisplayConfig newSchema;
		if ("form".equals(type)) {
			metadataCodes.addAll(otherMetadatas);
			newSchema = schema.withFormMetadataCodes(metadataCodes);

			SchemasDisplayManager manager = appLayerFactory.getMetadataSchemasDisplayManager();
			for (String invisible : otherMetadatas) {
				manager.saveMetadata(manager.getMetadata(collection, invisible)
						.withInputType(MetadataInputType.HIDDEN));
			}
		} else {
			newSchema = schema.withDisplayMetadataCodes(metadataCodes);
		}

		return newSchema;

	}
}
