package com.constellio.app.modules.es.model.connectors;

import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.connectors.spi.Connector;

public class RegisteredConnector {

	//http
	final String connectorCode;

	final ConnectorUtilsServices services;
	final String connectorInstanceCode;

	public String getConnectorCode() {
		return connectorCode;
	}

	public RegisteredConnector(String connectorCode, String connectorInstanceCode, ConnectorUtilsServices services) {
		this.connectorCode = connectorCode;
		this.services = services;
		this.connectorInstanceCode = connectorInstanceCode;
	}

	public ConnectorUtilsServices getServices() {
		return services;
	}

	public String getConnectorInstanceCode() {
		return connectorInstanceCode;
	}
}
