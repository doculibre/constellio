package com.constellio.app.ui.application;

import com.vaadin.server.ClientConnector;
import com.vaadin.ui.ConnectorTracker;
import com.vaadin.ui.UI;

public class ConstellioConnectorTracker extends ConnectorTracker {

	private final String prefix = "connectorTracker";
	private int sequence = 0; 
	
	public ConstellioConnectorTracker(UI ui) {
		super(ui);
	}

	@Override
	public void registerConnector(ClientConnector connector) {
		try {
			super.registerConnector(connector);
		} catch (RuntimeException exception) {
			
		}
	}
}
