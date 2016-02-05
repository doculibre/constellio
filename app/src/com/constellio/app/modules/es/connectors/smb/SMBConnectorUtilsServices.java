package com.constellio.app.modules.es.connectors.smb;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.constellio.app.modules.es.connectors.ConnectorServicesRuntimeException.ConnectorServicesRuntimeException_CannotDownloadDocument;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESGeneratedSchemasRecordsServices.SchemaTypeShortcuts_connectorInstance_default;
import com.constellio.app.modules.es.services.ESSchemaRecordsServicesRuntimeException.ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.SchemasRecordsServices.AbstractSchemaTypeShortcuts;
import com.constellio.model.services.records.SchemasRecordsServices.SchemaTypeShortcuts;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SMBConnectorUtilsServices implements ConnectorUtilsServices<ConnectorSmb> {

	SearchServices searchServices;
	ESSchemasRecordsServices es;
	AppLayerFactory appLayerFactory;
	String collection;

	public SMBConnectorUtilsServices(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public void addExcludedUrlsTo(List<String> newUrls, ConnectorInstance instance) {
		ConnectorSmbInstance smbInstance = es.wrapConnectorSmbInstance(instance.getWrappedRecord());
		Set<String> urls = new HashSet<>(smbInstance.getExclusions());
		urls.addAll(newUrls);
		smbInstance.setExclusions(new ArrayList<>(urls));
	}

	@Override
	public void
	deleteDocumentOnRemoteComponent(ConnectorDocument<?> connectorDocument) {
		ConnectorSmbInstance instance = es.getConnectorSmbInstance(connectorDocument.getConnector());
		instantiateConnector(instance).deleteFile(connectorDocument);
	}

	@Override
	public InputStream newContentInputStream(ConnectorDocument<?> connectorDocument, String resourceName) {
		try {
			ConnectorSmbInstance instance = es.getConnectorSmbInstance(connectorDocument.getConnector());
			return instantiateConnector(instance).getInputStream((ConnectorSmbDocument) connectorDocument, resourceName);

		} catch (ConnectorSmbRuntimeException e) {
			throw new ConnectorServicesRuntimeException_CannotDownloadDocument(connectorDocument, e);
		}
	}

	@Override
	public List<ConnectorDocument<?>> getChildren(ConnectorDocument<?> connectorDocument) {
		LogicalSearchCondition condition = from(es.connectorSmbDocument.schemaType())
				.where(es.connectorSmbDocument.parent()).isEqualTo(connectorDocument.getId());
		return es.searchConnectorDocuments(new LogicalSearchQuery(condition));
	}

	@Override
	public boolean isExcludable(ConnectorDocument<?> connectorDocument) {
		return connectorDocument.getSchemaCode().startsWith(es.connectorSmbDocument.schemaType() + "_");
	}

	@Override
	public ConnectorSmb instantiateConnector(ConnectorInstance instance) {
		ConnectorSmb connectorSmb = (ConnectorSmb) es.getConnectorManager().instanciate(instance);
		connectorSmb.setEs(es);
		return connectorSmb;
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorSmbDocument.SCHEMA_TYPE, ConnectorSmbFolder.SCHEMA_TYPE);
	}

	@Override
	public ConnectorDocument<?> wrapConnectorDocument(Record record) {
		if (record.getSchemaCode().startsWith(ConnectorSmbFolder.SCHEMA_TYPE)) {
			return es.wrapConnectorSmbFolder(record);

		} else if (record.getSchemaCode().startsWith(ConnectorSmbDocument.SCHEMA_TYPE)) {
			return es.wrapConnectorSmbDocument(record);

		}
		throw new ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument(record.getSchemaCode());
	}

	@Override
	public ConnectorInstance wrapConnectorInstance(Record record) {
		return es.wrapConnectorSmbInstance(record);
	}

	@Override
	public ConnectorInstance<?> newConnectorInstance() {
		return es.newConnectorSmbInstance();
	}

	@Override
	public String getRecordExternalUrl(RecordVO recordVO) {
		return null;
	}
	@Override
	public InputStream newContentInputStream(ConnectorDocument connectorDocument, String classifyDocument, String availableVersion) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public List<String> getAvailableVersions(String connectorID, ConnectorDocument document) {
		throw new UnsupportedOperationException("TODO");
	}

}
