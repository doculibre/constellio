package com.constellio.app.modules.es.services;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.connectors.ConnectorUtilsServices;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorInstanciator;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.ConnectorType;
import com.constellio.app.modules.es.model.connectors.RegisteredConnector;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPInstance;
import com.constellio.app.modules.es.model.connectors.ldap.ConnectorLDAPUserDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemaRecordsServicesRuntimeException.ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.AppLayerFactoryWithRequestCacheImpl;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.Factory;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.constellio.model.entities.schemas.MetadataValueType.BOOLEAN;
import static com.constellio.model.entities.schemas.MetadataValueType.DATE_TIME;
import static com.constellio.model.entities.schemas.MetadataValueType.ENUM;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static java.util.Arrays.asList;

public class ESSchemasRecordsServices extends ESGeneratedSchemasRecordsServices implements ConnectorInstanciator {

	private static final Logger LOGGER = LoggerFactory.getLogger(ESSchemasRecordsServices.class);

	protected transient AppLayerFactory appLayerFactory;
	protected Factory<AppLayerFactory> appLayerFactoryFactory;

	public ESSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory);
		this.appLayerFactory = appLayerFactory;
		this.appLayerFactoryFactory = toAppLayerFactoryFactory(appLayerFactory);
	}

	private static Factory<AppLayerFactory> toAppLayerFactoryFactory(final AppLayerFactory appLayerFactory) {
		final boolean requestAppLayerFactory = appLayerFactory instanceof AppLayerFactoryWithRequestCacheImpl;
		return new Factory<AppLayerFactory>() {
			@Override
			public AppLayerFactory get() {
				return requestAppLayerFactory ?
					   ConstellioFactories.getInstance().getAppLayerFactory() :
					   ConstellioFactories.getInstance().getUncachedAppLayerFactory();
			}
		};
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		appLayerFactory = appLayerFactoryFactory.get();
	}

	private String sharepointConnectorTypeId;

	private String httpConnectorTypeId;

	public ConnectorType getHttpConnectorType() {
		return getConnectorTypeWithCode(ConnectorType.CODE_HTTP);
	}

	public ConnectorType getSharepointConnectorType() {
		return getConnectorTypeWithCode(ConnectorType.CODE_SHAREPOINT);
	}

	public String getHttpConnectorTypeId() {
		if (httpConnectorTypeId == null) {
			httpConnectorTypeId = getHttpConnectorType().getId();
		}
		return httpConnectorTypeId;
	}

	public String getSharepointConnectorTypeId() {
		if (sharepointConnectorTypeId == null) {
			sharepointConnectorTypeId = getSharepointConnectorType().getId();
		}
		return sharepointConnectorTypeId;
	}

	public ConnectorManager getConnectorManager() {
		return appLayerFactory.getRegisteredManager(getCollection(), ConstellioESModule.ID, ConnectorManager.ID);
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

	public ConnectorHttpDocument getConnectorHttpDocumentByUrl(String url) {
		Metadata metadata = connectorHttpDocument.schema().getMetadata(Schemas.URL.getLocalCode());
		LogicalSearchCondition condition = from(connectorHttpDocument.schemaType()).where(metadata).isEqualTo(url);
		return wrapConnectorHttpDocument(modelLayerFactory.newSearchServices().searchSingleResult(condition));
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

	public ConnectorHttpDocument newConnectorHttpDocument(ConnectorHttpInstance connectorInstance) {
		String schema = connectorInstance.getDocumentsCustomSchemaCode();
		ConnectorHttpDocument connectorHttpDocumentRecord = wrapConnectorHttpDocument(
				create(connectorHttpDocument.schemaType().getCustomSchema(schema)));
		connectorHttpDocumentRecord.setConnector(connectorInstance);
		connectorHttpDocumentRecord.setConnectorType(connectorInstance.getConnectorType());
		connectorHttpDocumentRecord.setTraversalCode(connectorInstance.getTraversalCode());
		connectorHttpDocumentRecord.setFetchDelay(connectorInstance.getDaysBeforeRefetching());
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
				"Unsupported operation - Use newConnectorSmbDocument(connectorInstance) instead");
	}

	@Deprecated
	public ConnectorSmbDocument newConnectorSmbDocumentWithId(String id) {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorSmbDocument(id, connectorInstance) instead");
	}

	@Deprecated
	public ConnectorSmbFolder newConnectorSmbFolder() {
		throw new UnsupportedOperationException("Unsupported operation - Use newConnectorSmbFolder(connectorInstance) instead");
	}

	@Deprecated
	public ConnectorSmbFolder newConnectorSmbFolderWithId(String id) {
		throw new UnsupportedOperationException(
				"Unsupported operation - Use newConnectorSmbFolder(id, connectorInstance) instead");
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

	public ConnectorDocument<?> getConnectorDocument(String id) {
		Record record = getRecordServices().getDocumentById(id);
		return record == null ? null : wrapConnectorDocument(record);
	}

	public ConnectorSmbFolder getConnectorSmbFolder(String id) {
		Record record = getRecordServices().getDocumentById(id);
		return record == null ? null : wrapConnectorSmbFolder(record);
	}

	@Override
	public ConnectorInstance wrapConnectorInstance(Record record) {
		if (record == null) {
			return null;

		} else {
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(record.getCollection(), appLayerFactory);
			ConnectorManager connectorManager = es.getConnectorManager();

			for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
				ConnectorUtilsServices services;
				if (record.getSchemaCode().equals(connector.getConnectorInstanceCode())) {
					services = connector.getServices();
					return services.wrapConnectorInstance(record);
				}
			}
			LOGGER.warn("Unsupported connector instance '" + record.getSchemaCode() + "'");
			return new ConnectorInstance(record, getTypes());
		}
	}

	public ConnectorDocument<?> wrapConnectorDocument(Record record) {
		if (record == null) {
			return null;

		} else {
			ESSchemasRecordsServices es = new ESSchemasRecordsServices(record.getCollection(), appLayerFactory);
			ConnectorManager connectorManager = es.getConnectorManager();

			for (RegisteredConnector connector : connectorManager.getRegisteredConnectors()) {
				ConnectorUtilsServices<?> services = connector.getServices();
				for (String type : services.getConnectorDocumentTypes()) {
					if (record.getSchemaCode().startsWith(type + "_")) {
						return services.wrapConnectorDocument(record);
					}
				}
			}
			throw new ESSchemaRecordsServicesRuntimeException_RecordIsNotAConnectorDocument(record.getSchemaCode());

		}
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


	public LogicalSearchQuery connectorDocumentsToFetchQuery(ConnectorInstance<?> connectorInstance) {
		LogicalSearchQuery query = connectorDocumentsToFetchQueryUnsorted(connectorInstance);
		query.sortAsc(connectorDocument.fetched());
		query.sortAsc(Schemas.MODIFIED_ON);
		return query;
	}

	public LogicalSearchQuery connectorDocumentsToFetchQueryUnsorted(ConnectorInstance<?> connectorInstance) {
		List<String> typeCodes = instanciate(connectorInstance).getConnectorDocumentTypes();
		List<MetadataSchemaType> types = getTypes().getSchemaTypesWithCode(typeCodes);
		LogicalSearchQuery query = new LogicalSearchQuery();
		String currentTraversalCode = connectorInstance.getTraversalCode();
		query.setCondition(from(types).whereAllConditions(
				where(connectorDocument.connector()).isEqualTo(connectorInstance),
				anyConditions(
						where(connectorDocument.fetched()).isFalse(),
						where(connectorDocument.traversalCode()).isNotEqual(currentTraversalCode),
						where(connectorDocument.nextFetch()).isLessOrEqualThan(TimeProvider.getLocalDateTime())
				)));
		return query;
	}

	public LogicalSearchQuery connectorDocumentsToFetchWithNoDelayAndNoDeletedQuery(
			ConnectorInstance<?> connectorInstance) {
		LogicalSearchQuery query = connectorDocumentsToFetchWithNoDelayAndNoDeletedQueryUnsorted(connectorInstance);
		query.sortAsc(connectorDocument.fetched());
		query.sortAsc(Schemas.MODIFIED_ON);
		return query;
	}

	public LogicalSearchQuery connectorDocumentsToFetchWithNoDelayAndNoDeletedQueryUnsorted(
			ConnectorInstance<?> connectorInstance) {
		List<String> typeCodes = instanciate(connectorInstance).getConnectorDocumentTypes();
		List<MetadataSchemaType> types = getTypes().getSchemaTypesWithCode(typeCodes);
		LogicalSearchQuery query = new LogicalSearchQuery();
		String currentTraversalCode = connectorInstance.getTraversalCode();
		query.setCondition(from(types).whereAllConditions(
				where(connectorDocument.connector()).isEqualTo(connectorInstance),
				where(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull(),
				anyConditions(
						where(connectorDocument.fetched()).isFalse(),
						where(connectorDocument.traversalCode()).isNotEqual(currentTraversalCode),
						where(connectorDocument.nextFetch()).isLessOrEqualThan(TimeProvider.getLocalDateTime())
				)

		));
		return query;
	}

	public Iterator<String> getUrlsIterator(LogicalSearchQuery query) {
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.URL));
		final Iterator<Record> recordIterator = getSearchServices().recordsIterator(query, 1000);
		return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return recordIterator.hasNext();
			}

			@Override
			public String next() {
				return recordIterator.next().get(Schemas.URL);
			}

			@Override
			public void remove() {
				recordIterator.remove();
			}
		};
	}

	public class SchemaTypeShortcuts_connectorDocument_common extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_connectorDocument_common(String schemaCode) {
			super(schemaCode);
		}

		public Metadata url() {
			return Metadata.newGlobalMetadata((short) 0, "url_s", STRING, false, false);
		}

		public Metadata connectorUrl() {
			return Metadata.newGlobalMetadata((short) 0, "connectorUrl_s", STRING, false, false);
		}

		public Metadata mimetype() {
			return Metadata.newGlobalMetadata((short) 0, "mimetype_s", STRING, false, false);
		}

		public Metadata traversalCode() {
			return Metadata.newGlobalMetadata((short) 0, "traversalCode_s", STRING, false, false);
		}

		public Metadata connector() {
			return Metadata.newGlobalMetadata((short) 0, "connector_s", REFERENCE, false, false);
		}

		public Metadata connectorType() {
			return Metadata.newGlobalMetadata((short) 0, "connectorType_s", REFERENCE, false, false);
		}

		public Metadata fetched() {
			return Metadata.newGlobalMetadata((short) 0, "fetched_s", BOOLEAN, false, false);
		}

		public Metadata fetchedDateTime() {
			return Metadata.newGlobalMetadata((short) 0, "fetchedDateTime_dt", DATE_TIME, false, false);
		}

		public Metadata status() {
			return Metadata.newGlobalMetadata((short) 0, "status_s", ENUM, false, false);
		}

		public Metadata fetchFrequency() {
			return Metadata.newGlobalMetadata((short) 0, "fetchFrequency_s", ENUM, false, false);
		}

		public Metadata fetchDelay() {
			return Metadata.newGlobalMetadata((short) 0, "fetchDelay_d", NUMBER, false, false);
		}

		public Metadata nextFetch() {
			return Metadata.newGlobalMetadata((short) 0, "nextFetch_dt", DATE_TIME, false, false);
		}

		public Metadata searchable() {
			return Metadata.newGlobalMetadata((short) 0, "searchable_s", BOOLEAN, false, false);
		}

		public Metadata neverFetch() {
			return Metadata.newGlobalMetadata((short) 0, "neverFetch_s", BOOLEAN, false, false);
		}

		public Metadata errorCode() {
			return Metadata.newGlobalMetadata((short) 0, "errorCode_s", STRING, false, false);
		}

		public Metadata errorsCount() {
			return Metadata.newGlobalMetadata((short) 0, "errorsCount_d", NUMBER, false, false);
		}

		public Metadata errorMessage() {
			return Metadata.newGlobalMetadata((short) 0, "errorMessage_s", STRING, false, false);
		}

		public Metadata errorStackTrace() {
			return Metadata.newGlobalMetadata((short) 0, "errorStackTrace_s", TEXT, false, false);
		}

		public final Metadata lastModified() {
			return Metadata.newGlobalMetadata((short) 0, "lastModified_dt", DATE_TIME, false, false);
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

	//ldap

	public ConnectorLDAPInstance newConnectorLDAPInstance() {
		return super.newConnectorLDAPInstance().setConnectorType(getLDAPConnectorTypeId());
	}

	public ConnectorType getLDAPConnectorTypeId() {
		return getConnectorTypeWithCode(ConnectorType.CODE_LDAP);
	}

	@Deprecated
	public ConnectorLDAPUserDocument newConnectorLDAPUserDocument() {
		return super.newConnectorLDAPUserDocument();
	}

	public ConnectorLDAPUserDocument newConnectorLDAPUserDocument(ConnectorLDAPInstance connectorInstance) {
		ConnectorLDAPUserDocument document = super.newConnectorLDAPUserDocument();
		document.setConnector(connectorInstance);
		document.setConnectorType(connectorInstance.getConnectorType());
		document.setTraversalCode(connectorInstance.getTraversalCode());
		return document;
	}

	@Deprecated
	public ConnectorLDAPUserDocument newConnectorLDAPUserDocumentWithId(String id) {
		return wrapConnectorLDAPUserDocument(create(connectorLdapUserDocument.schema(), id));
	}

	public ConnectorLDAPUserDocument newConnectorLDAPUserDocumentWithId
			(String id, ConnectorInstance connectorInstance) {
		ConnectorLDAPUserDocument document = super.newConnectorLDAPUserDocumentWithId(id);
		document.setConnector(connectorInstance);
		document.setConnectorType(connectorInstance.getConnectorType());
		document.setTraversalCode(connectorInstance.getTraversalCode());
		return document;
	}

	public Iterator<ConnectorSmbDocument> iterateConnectorSmbDocuments(LogicalSearchCondition condition) {
		MetadataSchemaType type = connectorSmbDocument.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return iterateConnectorSmbDocuments(query);
	}

	public Iterator<ConnectorSmbDocument> iterateConnectorSmbDocuments(LogicalSearchQuery query) {
		return wrapRecordIteratorToSmbDocument(
				appLayerFactory.getModelLayerFactory().newSearchServices().recordsIterator(query, 100));
	}

	public Iterator<ConnectorSmbDocument> wrapRecordIteratorToSmbDocument(final Iterator<Record> recordsIterator) {
		return new Iterator<ConnectorSmbDocument>() {
			@Override
			public boolean hasNext() {
				return recordsIterator.hasNext();
			}

			@Override
			public ConnectorSmbDocument next() {
				return wrapConnectorSmbDocument(recordsIterator.next());
			}

			@Override
			public void remove() {
				recordsIterator.remove();
			}
		};
	}


	public Iterator<ConnectorSmbFolder> iterateConnectorSmbFolders(LogicalSearchCondition condition) {
		MetadataSchemaType type = connectorSmbDocument.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return iterateConnectorSmbFolders(query);
	}

	public Iterator<ConnectorSmbFolder> iterateConnectorSmbFolders(LogicalSearchQuery query) {
		return wrapRecordIteratorToSmbFolder(
				appLayerFactory.getModelLayerFactory().newSearchServices().recordsIterator(query, 100));
	}

	public Iterator<ConnectorSmbFolder> wrapRecordIteratorToSmbFolder(final Iterator<Record> recordsIterator) {
		return new Iterator<ConnectorSmbFolder>() {
			@Override
			public boolean hasNext() {
				return recordsIterator.hasNext();
			}

			@Override
			public ConnectorSmbFolder next() {
				return wrapConnectorSmbFolder(recordsIterator.next());
			}

			@Override
			public void remove() {
				recordsIterator.remove();
			}
		};
	}
}
