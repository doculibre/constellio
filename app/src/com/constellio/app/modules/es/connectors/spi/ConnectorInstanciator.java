package com.constellio.app.modules.es.connectors.spi;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;

public interface ConnectorInstanciator {

	Connector instanciate(ConnectorInstance connectorInstance);
}
