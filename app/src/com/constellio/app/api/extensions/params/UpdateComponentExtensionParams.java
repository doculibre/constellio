package com.constellio.app.api.extensions.params;

import com.vaadin.ui.Component;

public class UpdateComponentExtensionParams {

	Component mainComponent;

	Component componentToUpdate;

	public UpdateComponentExtensionParams(Component mainComponent, Component componentToUpdate) {
		this.mainComponent = mainComponent;
		this.componentToUpdate = componentToUpdate;
	}

	public Component getMainComponent() {
		return mainComponent;
	}

	public Component getComponentToUpdate() {
		return componentToUpdate;
	}
}
