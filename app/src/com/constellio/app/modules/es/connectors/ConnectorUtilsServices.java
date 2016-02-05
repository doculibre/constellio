package com.constellio.app.modules.es.connectors;

import java.io.InputStream;
import java.util.List;

import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;

public interface ConnectorUtilsServices<T extends Connector> {

	void addExcludedUrlsTo(List<String> newUrls, ConnectorInstance instance);

	void deleteDocumentOnRemoteComponent(ConnectorDocument<?> connectorDocument);

	InputStream newContentInputStream(ConnectorDocument<?> connectorDocument, String resourceName);

	List<ConnectorDocument<?>> getChildren(ConnectorDocument<?> connectorDocument);

	boolean isExcludable(ConnectorDocument<?> connectorDocument);

	T instantiateConnector(ConnectorInstance<?> instance);

	List<String> getConnectorDocumentTypes();

	ConnectorDocument<?> wrapConnectorDocument(Record record);

	ConnectorInstance wrapConnectorInstance(Record record);

	ConnectorInstance<?> newConnectorInstance();

	String getRecordExternalUrl(RecordVO recordVO);

	InputStream newContentInputStream(ConnectorDocument connectorDocument, String classifyDocument, String availableVersion);

	List<String> getAvailableVersions(String connectorID, ConnectorDocument document);
}
