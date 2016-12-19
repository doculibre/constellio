package com.constellio.app.modules.es.extensions;

import com.constellio.app.api.extensions.SearchPageExtension;
import com.constellio.app.api.extensions.taxonomies.GetCustomResultDisplayParam;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.ui.components.SmbSearchResultDisplay;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.framework.components.SearchResultDisplay;

public class ESSearchPageExtension extends SearchPageExtension {

	private AppLayerFactory appLayerFactory;

	public ESSearchPageExtension(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public SearchResultDisplay getCustomResultDisplayFor(GetCustomResultDisplayParam param) {
		if (param.getSchemaType().equals(ConnectorSmbDocument.SCHEMA_TYPE)) {

			return new SmbSearchResultDisplay(param.getSearchResultVO(), param.getComponentFactory(), appLayerFactory);
		}
		return super.getCustomResultDisplayFor(param);
	}
}
