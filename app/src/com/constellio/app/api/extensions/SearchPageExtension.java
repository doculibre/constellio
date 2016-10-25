package com.constellio.app.api.extensions;


import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.api.extensions.taxonomies.QueryAndResponseInfoParam;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchPageExtension {

	protected AppLayerFactory appLayerFactory;

	public SearchPageExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		return null;
	}

	public void writeQueryAndResponseInfoToCSV(QueryAndResponseInfoParam param) {}

	protected AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
}
