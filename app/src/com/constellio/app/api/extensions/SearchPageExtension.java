package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;

public class SearchPageExtension {

	protected AppLayerFactory appLayerFactory;

	public SearchPageExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		return null;
	}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
}
