package com.constellio.app.ui.framework.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

public class BaseCustomComponent extends CustomComponent {

	public BaseCustomComponent() {
	}

	public BaseCustomComponent(Component compositionRoot) {
		super(compositionRoot);
	}

	@Override
	public void setCompositionRoot(Component compositionRoot) {
		super.setCompositionRoot(compositionRoot);
	}

}
