package com.constellio.app.modules.es.extensions.api;

import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;

public class OnHttpDocumentFetchedParams {
    ConnectorHttpDocument connectorHttpDocument;

    public ConnectorHttpDocument getConnectorHttpDocument() {
        return connectorHttpDocument;
    }

    public OnHttpDocumentFetchedParams setConnectorHttpDocument(ConnectorHttpDocument connectorHttpDocument) {
        this.connectorHttpDocument = connectorHttpDocument;
        return this;
    }
}
