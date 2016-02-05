package com.constellio.app.extensions.impl;

import com.constellio.app.api.extensions.PagesComponentsExtension;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.services.factories.AppLayerFactory;

public class DefaultPagesComponentsExtension extends PagesComponentsExtension {

	AppLayerFactory appLayerFactory;

	public DefaultPagesComponentsExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void decorateView(PagesComponentsExtensionParams params) {

	}

}
