package com.constellio.app.api.extensions.params;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Component;

public class DecorateMainComponentAfterInitExtensionParams {

	Component mainComponent;
	ViewChangeEvent viewChangeEvent;

	public DecorateMainComponentAfterInitExtensionParams(Component mainComponent) {
		this(mainComponent, null);
	}

	public DecorateMainComponentAfterInitExtensionParams(Component mainComponent, ViewChangeEvent event) {
		this.mainComponent = mainComponent;
		this.viewChangeEvent = event;
	}

	public Component getMainComponent() {
		return mainComponent;
	}

	public ViewChangeEvent getViewChangeEvent() {
		return viewChangeEvent;
	}
}
