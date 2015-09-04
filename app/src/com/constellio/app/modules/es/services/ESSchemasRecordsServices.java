/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class ESSchemasRecordsServices extends ESGeneratedSchemasRecordsServices {

	AppLayerFactory appLayerFactory;

	public ESSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
		this.appLayerFactory = appLayerFactory;
	}

	private String httpConnectorTypeId;

	public ConnectorType getHttpConnectorType() {
		return getConnectorTypeWithCode(ConnectorType.CODE_HTTP);
	}

	public String getHttpConnectorTypeId() {
		if (httpConnectorTypeId == null) {
			httpConnectorTypeId = getHttpConnectorType().getId();
		}
		return httpConnectorTypeId;
	}

	public ConnectorManager getConnectorManager() {
		return appLayerFactory.getRegisteredManager(collection, ConstellioESModule.ID, ConnectorManager.ID);
	}

	public ContentManager getContentManager() {
		return appLayerFactory.getModelLayerFactory().getContentManager();
	}

	public SearchServices getSearchServices() {
		return appLayerFactory.getModelLayerFactory().newSearchServices();
	}

	public RecordServices getRecordServices() {
		return appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	public ModelLayerFactory getModelLayerFactory() {
		return appLayerFactory.getModelLayerFactory();
	}

	public IOServices getIOServices() {
		return appLayerFactory.getModelLayerFactory().getDataLayerFactory().getIOServicesFactory().newIOServices();
	}

	public ConnectorHttpInstance newConnectorHttpInstance() {
		return wrapConnectorHttpInstance(create(connectorInstance_http.schema())).setConnectorType(getHttpConnectorTypeId());
	}

	@Deprecated
	public ConnectorHttpDocument newConnectorHttpDocument() {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorHttpDocument(connectorInstance) instead");
	}

	@Deprecated
	public ConnectorHttpDocument newConnectorHttpDocumentWithId(String id) {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorHttpDocument(id, connectorInstance) instead");
	}

	public ConnectorHttpDocument newConnectorHttpDocumentWithId(String id, ConnectorInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorHttpDocument connectorHttpDocumentRecord = wrapConnectorHttpDocument(
				create(connectorHttpDocument.schemaType().getCustomSchema(schema), id));
		connectorHttpDocumentRecord.setConnector(connectorInstance);
		connectorHttpDocumentRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorHttpDocumentRecord.setTraversalCode(connectorInstance.getTraversalCode());
		return connectorHttpDocumentRecord;
	}

	public ConnectorHttpDocument newConnectorHttpDocument(ConnectorInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorHttpDocument connectorHttpDocumentRecord = wrapConnectorHttpDocument(
				create(connectorHttpDocument.schemaType().getCustomSchema(schema)));
		connectorHttpDocumentRecord.setConnector(connectorInstance);
		connectorHttpDocumentRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorHttpDocumentRecord.setTraversalCode(connectorInstance.getTraversalCode());
		return connectorHttpDocumentRecord;
	}

	public ConnectorHttpInstance newConnectorHttpInstanceWithId(String id) {
		return wrapConnectorHttpInstance(create(connectorInstance_http.schema(), id)).setConnectorType(getHttpConnectorTypeId());
	}

	public LogicalSearchCondition fromConnectorHttpDocumentWhereConnectorIs(ConnectorHttpInstance instance) {
		return from(connectorHttpDocument.schemaType()).where(connectorHttpDocument.connector()).isEqualTo(instance);
	}

	// SMB

	private String smbConnectorTypeId;

	public ConnectorSmbInstance newConnectorSmbInstance() {
		return wrapConnectorSmbInstance(create(connectorInstance_smb.schema())).setConnectorType(getSmbConnectorTypeId());
	}

	@Deprecated
	public ConnectorSmbDocument newConnectorSmbDocument() {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorHttpDocument(connectorInstance) instead");
	}

	@Deprecated
	public ConnectorSmbDocument newConnectorSmbDocumentWithId(String id) {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorHttpDocument(id, connectorInstance) instead");
	}

	@Deprecated
	public ConnectorSmbFolder newConnectorSmbFolder() {
		throw new UnsupportedOperationException("Unsupported operation - Use newConnectorHttpFolder(connectorInstance) instead");
	}

	@Deprecated
	public ConnectorSmbFolder newConnectorSmbFolderWithId(String id) {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorHttpFolder(id, connectorInstance) instead");
	}

	public String getSmbConnectorTypeId() {
		if (smbConnectorTypeId == null) {
			smbConnectorTypeId = getSmbConnectorType().getId();
		}
		return smbConnectorTypeId;
	}

	public ConnectorType getSmbConnectorType() {
		return getConnectorTypeWithCode(ConnectorType.CODE_SMB);
	}

	public ConnectorSmbDocument newConnectorSmbDocument(ConnectorInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorSmbDocument connectorSmbDocumentRecord = wrapConnectorSmbDocument(
				create(connectorSmbDocument.schemaType().getCustomSchema(schema)));
		connectorSmbDocumentRecord.setConnector(connectorInstance);
		connectorSmbDocumentRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorSmbDocumentRecord.setTraversalCode(connectorInstance.getTraversalCode());
		return connectorSmbDocumentRecord;
	}

	public ConnectorSmbDocument newConnectorSmbDocumentWithId(String id, ConnectorInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorSmbDocument connectorSmbDocumentRecord = wrapConnectorSmbDocument(
				create(connectorSmbDocument.schemaType().getCustomSchema(schema), id));
		connectorSmbDocumentRecord.setConnector(connectorInstance);
		connectorSmbDocumentRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorSmbDocumentRecord.setTraversalCode(connectorInstance.getTraversalCode());
		return connectorSmbDocumentRecord;
	}

	public ConnectorSmbFolder newConnectorSmbFolder(ConnectorInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorSmbFolder connectorSmbFolderRecord = wrapConnectorSmbFolder(
				create(connectorSmbFolder.schemaType().getCustomSchema(schema)));
		connectorSmbFolderRecord.setConnector(connectorInstance);
		connectorSmbFolderRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorSmbFolderRecord.setTraversalCode(connectorInstance.getTraversalCode());
		return connectorSmbFolderRecord;
	}

	public ConnectorSmbFolder newConnectorSmbFolderWithId(String id, ConnectorInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorSmbFolder connectorSmbFolderRecord = wrapConnectorSmbFolder(
				create(connectorSmbFolder.schemaType().getCustomSchema(schema), id));
		connectorSmbFolderRecord.setConnector(connectorInstance);
		connectorSmbFolderRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorSmbFolderRecord.setTraversalCode(connectorInstance.getTraversalCode());
		return connectorSmbFolderRecord;
	}

	public LogicalSearchCondition fromConnectorSmbDocumentWhereConnectorIs(ConnectorSmbInstance instance) {
		return from(connectorSmbDocument.schemaType()).where(connectorSmbDocument.connector()).isEqualTo(instance);
	}
	
	public LogicalSearchCondition fromConnectorSmbFolderWhereConnectorIs(ConnectorSmbInstance instance) {
		return from(connectorSmbFolder.schemaType()).where(connectorSmbFolder.connector()).isEqualTo(instance);
	}

	public List<ConnectorDocument<?>> wrapConnectorDocuments(List<Record> records) {
		List<ConnectorDocument<?>> documents = new ArrayList<>();

		for (Record record : records) {
			documents.add(wrapConnectorDocument(record));

		}

		return documents;
	}

	public ConnectorDocument<?> wrapConnectorDocument(Record record) {
		if (record == null) {
			return null;

		} else if (record.getSchemaCode().startsWith(ConnectorHttpDocument.SCHEMA_TYPE)) {
			return wrapConnectorHttpDocument(record);

		} else if (record.getSchemaCode().startsWith(ConnectorSmbFolder.SCHEMA_TYPE)) {
			return wrapConnectorSmbFolder(record);

		} else if (record.getSchemaCode().startsWith(ConnectorSmbDocument.SCHEMA_TYPE)) {
			return wrapConnectorSmbDocument(record);

		}

		throw new ImpossibleRuntimeException("wrapConnectorDocument does not support schema '" + record.getSchemaCode() + "'");
	}

	public List<MetadataSchemaType> getConnectorDocumentsSchemaTypes(String connectorId) {
		Connector connector = instanciate(connectorId);
		List<MetadataSchemaType> types = new ArrayList<>();

		for (String typeCode : connector.getConnectorDocumentTypes()) {
			types.add(getTypes().getSchemaType(typeCode));
		}

		return types;
	}

	public final SchemaTypeShortcuts_connectorDocument_common connectorDocument
			= new SchemaTypeShortcuts_connectorDocument_common("connectorHttpDocument_default");

	public LogicalSearchCondition fromAllFetchedDocumentsOf(String connectorId) {
		List<MetadataSchemaType> schemaTypes = getConnectorDocumentsSchemaTypes(connectorId);
		return from(schemaTypes)
				.where(connectorDocument.fetched()).isTrue()
				.andWhere(connectorDocument.connector()).isEqualTo(connectorId);
	}
	
	public LogicalSearchCondition fromAllDocumentsOf(String connectorId) {
		List<MetadataSchemaType> schemaTypes = getConnectorDocumentsSchemaTypes(connectorId);
		return from(schemaTypes)
				.where(connectorDocument.connector()).isEqualTo(connectorId);
	}

	public String getCollection() {
		return collection;
	}

	public LogicalSearchQuery connectorDocumentsToFetchQuery(ConnectorInstance<?> connectorInstance) {
		List<String> typeCodes = instanciate(connectorInstance).getConnectorDocumentTypes();
		List<MetadataSchemaType> types = getTypes().getSchemaTypesWithCode(typeCodes);
		LogicalSearchQuery query = new LogicalSearchQuery();
		String currentTraversalCode = connectorInstance.getTraversalCode();
		query.setCondition(from(types).whereAllConditions(
				where(connectorDocument.connector()).isEqualTo(connectorInstance),
				anyConditions(
						where(connectorDocument.fetched()).isFalse(),
						where(connectorDocument.traversalCode()).isNotEqual(currentTraversalCode)
				)));
		query.sortAsc(connectorDocument.fetched());
		query.sortAsc(Schemas.MODIFIED_ON);
		return query;
	}

	public class SchemaTypeShortcuts_connectorDocument_common extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_connectorDocument_common(String schemaCode) {
			super(schemaCode);
		}

		public Metadata connector() {
			return Metadata.newGlobalMetadata("connector_s", MetadataValueType.REFERENCE, false);
		}

		public Metadata connectorType() {
			return Metadata.newGlobalMetadata("connectorType_s", MetadataValueType.REFERENCE, false);
		}

		public Metadata fetched() {
			return Metadata.newGlobalMetadata("fetched_s", MetadataValueType.BOOLEAN, false);
		}

		public Metadata traversalCode() {
			return Metadata.newGlobalMetadata("traversalCode_s", MetadataValueType.STRING, false);
		}

	}

	public Connector instanciate(String connectorId) {
		ConnectorInstance connectorInstance = getConnectorInstance(connectorId);
		return instanciate(connectorInstance);
	}

	public Connector instanciate(ConnectorInstance instance) {
		String connectorTypeId = instance.getConnectorType();
		String connectorClassName = getConnectorType(connectorTypeId).getConnectorClassName();

		try {
			Class<?> connectorClass = Class.forName(connectorClassName);
			return (Connector) connectorClass.newInstance();

		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public List<ConnectorDocument<?>> searchConnectorDocuments(LogicalSearchQuery query) {
		return wrapConnectorDocuments(appLayerFactory.getModelLayerFactory().newSearchServices().search(query));
	}
	
	public SchemasDisplayManager getMetadataSchemasDisplayManager() {
		return appLayerFactory.getMetadataSchemasDisplayManager();
	}

	public AppLayerFactory getAppLayerFactory() {
		return appLayerFactory;
	}
	
	public String getConnectorSmbDocumentSchemaCode(ConnectorInstance connectorInstance) {
		String result = connectorSmbDocument.schemaType().getDefaultSchema().getCode();
		return result;
	}
}
