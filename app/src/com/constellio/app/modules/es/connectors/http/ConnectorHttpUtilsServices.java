package com.constellio.app.modules.es.connectors.http;

import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;

import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;

public class ConnectorHttpUtilsServices implements ConnectorUtilsServices<ConnectorHttp> {

	ESSchemasRecordsServices es;
	AppLayerFactory appLayerFactory;
	String collection;
	private String httpConnectorTypeId;

	public ConnectorHttpUtilsServices(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public void addExcludedUrlsTo(List<String> newUrls, ConnectorInstance instance) {

	}

	@Override
	public void deleteDocumentOnRemoteComponent(ConnectorDocument<?> connectorDocument) {

	}

	@Override
	public InputStream newContentInputStream(ConnectorDocument<?> connectorDocument, String resourceName) {
		return null;
	}

	@Override
	public List<ConnectorDocument<?>> getChildren(ConnectorDocument<?> connectorDocument) {
		return null;
	}

	@Override
	public boolean isExcludable(ConnectorDocument<?> connectorDocument) {
		return false;
	}

	@Override
	public ConnectorHttp instantiateConnector(ConnectorInstance<?> instance) {
		ConnectorHttp connectorHttp = (ConnectorHttp) es.getConnectorManager().instanciate(instance);
		connectorHttp.setEs(es);
		return connectorHttp;
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorHttpDocument.SCHEMA_TYPE);
	}

	@Override
	public ConnectorDocument<?> wrapConnectorDocument(Record record) {
		return es.wrapConnectorHttpDocument(record);
	}

	@Override
	public ConnectorInstance wrapConnectorInstance(Record record) {
		return es.wrapConnectorHttpInstance(record);
	}

	@Override
	public ConnectorInstance<?> newConnectorInstance() {
		return es.newConnectorHttpInstance();
	}

	@Override
	public String getRecordExternalUrl(RecordVO recordVO) {
		String schemaCode = recordVO.getSchema().getCode();
		return recordVO.get(schemaCode + "_url");
	}

	@Override
	public InputStream newContentInputStream(ConnectorDocument connectorDocument, String classifyDocument,
											 String availableVersion) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public List<String> getAvailableVersions(String connectorID, ConnectorDocument document) {
		throw new UnsupportedOperationException("TODO");
	}
}
