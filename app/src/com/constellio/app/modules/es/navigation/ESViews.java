package com.constellio.app.modules.es.navigation;

import com.constellio.app.ui.application.CoreViews;
import com.vaadin.navigator.Navigator;

public class ESViews extends CoreViews {
	public ESViews(Navigator navigator) {
		super(navigator);
	}

	// CONNECTOR INSTANCES

	public void listConnectorInstances() {
        navigator.navigateTo(ESNavigationConfiguration.LIST_CONNECTOR_INSTANCES);
	}

    public void displayConnectorInstance(String params) {
        navigator.navigateTo(ESNavigationConfiguration.DISPLAY_CONNECTOR_INSTANCE + "/" + params);
    }

    public void editConnectorInstance(String params) {
        navigator.navigateTo(ESNavigationConfiguration.EDIT_CONNECTOR_INSTANCE + "/" + params);
    }

    public void wizardConnectorInstance() {
        navigator.navigateTo(ESNavigationConfiguration.WIZARD_CONNECTOR_INSTANCE);
    }

    //MAPPING

    public void addConnectorMapping(String connectorInstance, String documentType) {
        navigator.navigateTo(ESNavigationConfiguration.ADD_CONNECTOR_MAPPING +
                "/" + connectorInstance + "/" + documentType);
    }

    public void editConnectorMapping(String connectorInstance, String documentType, String metadata) {
        navigator.navigateTo(ESNavigationConfiguration.ADD_CONNECTOR_MAPPING +
                "/" + connectorInstance + "/" + documentType + "/" + metadata);
    }

    public void displayConnectorMappings(String entityId) {
        navigator.navigateTo(ESNavigationConfiguration.DISPLAY_CONNECTOR_MAPPINGS + "/" + entityId);
    }

}
