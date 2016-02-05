package com.constellio.app.modules.es.services;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.schemas.MetadataSchemasManager;

public class ConnectorDeleteService {

	private AppLayerFactory appLayerFactory;
	private ESSchemasRecordsServices es;
	private String collection;

	public ConnectorDeleteService(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);
	}

	public void deleteConnector(ConnectorInstance connectorInstance) {
		es.getConnectorManager().totallyDeleteConnectorRecordsSkippingValidation(
				appLayerFactory.getModelLayerFactory().getDataLayerFactory().newRecordDao(), connectorInstance);
		List<MetadataSchema> schemasToDelete = new ArrayList<>();
		Connector connector = es.getConnectorManager().instanciate(connectorInstance);
		MetadataSchemasManager schemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		for (String documentType : connector.getConnectorDocumentTypes()) {
			schemasToDelete.add(
					schemasManager.getSchemaTypes(collection).getSchema(documentType + "_" + connectorInstance.getId()));
		}
		schemasManager.deleteCustomSchemas(schemasToDelete);
		String configFolderPath =
				"connectors/" + es.getConnectorType(connectorInstance.getConnectorType()).getCode() + "/" + connectorInstance
						.getId() + "/";
		ConfigManager configManager = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getConfigManager();
		if (configManager.folderExist(configFolderPath)) {
			configManager.deleteFolder(configFolderPath);
		}
		es.getConnectorManager().delete(connectorInstance);
	}

}
