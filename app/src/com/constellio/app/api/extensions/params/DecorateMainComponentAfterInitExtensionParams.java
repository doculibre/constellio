package com.constellio.app.api.extensions.params;

import com.vaadin.ui.Component;

public class DecorateMainComponentAfterInitExtensionParams {

	Component mainComponent;

	public DecorateMainComponentAfterInitExtensionParams(Component mainComponent) {
		this.mainComponent = mainComponent;
	}

	public Component getMainComponent() {
		return mainComponent;
	}
}
