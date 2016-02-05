package com.constellio.app.modules.es.connectors;

import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.RegisteredConnector;
import com.constellio.app.modules.es.services.ConnectorManager;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;

public class ConnectorServicesFactory {

	public static ConnectorUtilsServices forConnectorInstance(AppLayerFactory appLayerFactory,
			ConnectorInstance connectorInstance) {
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(connectorInstance.getCollection(), appLayerFactory);
		ConnectorManager connectorManager = es.getConnectorManager();

		for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
			if (connector.getConnectorInstanceCode().equals(connectorInstance.getSchema().getCode())) {
				return connector.getServices();
			}
		}
		throw new ImpossibleRuntimeException("Unsupported schema '" + connectorInstance.getSchema().getCode() + "'");
	}

	public static ConnectorUtilsServices forConnectorDocument(AppLayerFactory appLayerFactory,
			ConnectorDocument connectorDocument) {
		return forRecord(appLayerFactory, connectorDocument.getWrappedRecord());
	}
	
	public ConnectorUtilsServices forConnectorDocumentNonStatic(AppLayerFactory appLayerFactory,
			ConnectorDocument connectorDocument) {
		return forConnectorDocument(appLayerFactory, connectorDocument);
	}

	public static ConnectorUtilsServices forRecord(AppLayerFactory appLayerFactory, Record record) {

		ESSchemasRecordsServices es = new ESSchemasRecordsServices(record.getCollection(), appLayerFactory);
		ConnectorManager connectorManager = es.getConnectorManager();

		for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
			ConnectorUtilsServices<?> services = connector.getServices();
			for (String type : services.getConnectorDocumentTypes()) {
				if (record.getSchemaCode().startsWith(type + "_")) {
					return services;
				}
			}
		}

		throw new ImpossibleRuntimeException("Unsupported schema '" + record.getSchemaCode() + "'");
	}

}
