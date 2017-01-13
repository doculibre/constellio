package com.constellio.app.modules.es.connectors.ldap;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.services.ESGeneratedSchemasRecordsServices.SchemaTypeShortcuts_connectorInstance_default;
import com.constellio.app.modules.es.services.ESSchemaRecordsServicesRuntimeException.ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ConnectorLDAPUtilsServices implements ConnectorUtilsServices<ConnectorLDAP> {

	ESSchemasRecordsServices es;
	AppLayerFactory appLayerFactory;
	String collection;

	public ConnectorLDAPUtilsServices(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.es = new ESSchemasRecordsServices(collection, appLayerFactory);
	}

	@Override
	public void addExcludedUrlsTo(List<String> newUrls, ConnectorInstance instance) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void deleteDocumentOnRemoteComponent(ConnectorDocument<?> connectorDocument) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public InputStream newContentInputStream(ConnectorDocument<?> connectorDocument, String resourceName) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public List<ConnectorDocument<?>> getChildren(ConnectorDocument<?> connectorDocument) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public boolean isExcludable(ConnectorDocument<?> connectorDocument) {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public ConnectorLDAP instantiateConnector(ConnectorInstance<?> instance) {
		ConnectorLDAP connectorLDAP = (ConnectorLDAP) es.getConnectorManager().instanciate(instance);
		connectorLDAP.setEs(es);
		return connectorLDAP;
	}

	@Override
	public List<String> getConnectorDocumentTypes() {
		return asList(ConnectorLDAPUserDocument.SCHEMA_TYPE);
	}

	@Override
	public ConnectorDocument<?> wrapConnectorDocument(Record record) {
		if (record.getSchemaCode().startsWith(ConnectorLDAPUserDocument.SCHEMA_TYPE)) {
			return es.wrapConnectorLDAPUserDocument(record);

		}
		throw new ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument(record.getSchemaCode());
	}

	@Override
	public ConnectorInstance wrapConnectorInstance(Record record) {
		return es.wrapConnectorLDAPInstance(record);
	}

	@Override
	public ConnectorInstance<?> newConnectorInstance() {
		return es.newConnectorLDAPInstance();
	}

	@Override
	public String getRecordExternalUrl(RecordVO recordVO) {
		return null;
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
