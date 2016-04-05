package com.constellio.app.modules.es.navigation;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.NavigatorConfigurationService;
import com.vaadin.navigator.Navigator;

public class ESViews extends CoreViews {
	public ESViews(Navigator navigator) {
		super(navigator);
	}

	// CONNECTOR INSTANCES

	public void listConnectorInstances() {
		navigator.navigateTo(NavigatorConfigurationService.LIST_CONNECTOR_INSTANCES);
	}
}
