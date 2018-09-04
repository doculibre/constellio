package com.constellio.app.modules.es.extensions.api;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.model.services.factories.ModelLayerFactory;

public class OnHttpDocumentFetchedParams {
	ConnectorHttpDocument connectorHttpDocument;
	ModelLayerFactory modelLayerFactory;

	public ModelLayerFactory getModelLayerFactory() {
		return modelLayerFactory;
	}

	public OnHttpDocumentFetchedParams setModelLayerFactory(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		return this;
	}

	public ConnectorHttpDocument getConnectorHttpDocument() {
		return connectorHttpDocument;
	}

	public OnHttpDocumentFetchedParams setConnectorHttpDocument(ConnectorHttpDocument connectorHttpDocument) {
		this.connectorHttpDocument = connectorHttpDocument;
		return this;
	}
}
