package com.constellio.app.modules.es.connectors.smb.service;

import java.util.*;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.model.connectors.DocumentSmbConnectorUrlCalculator;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import org.joda.time.LocalDateTime;

public class SmbRecordService {
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private ConnectorSmbUtils smbUtils;
	private RecordServices recordServices;

	public SmbRecordService(ESSchemasRecordsServices es, ConnectorSmbInstance connectorInstance) {
		this.es = es;
		this.connectorInstance = connectorInstance;
		this.smbUtils = new ConnectorSmbUtils();
		this.recordServices = es.getAppLayerFactory().getModelLayerFactory().newRecordServices();
	}

	public List<ConnectorSmbDocument> getDocuments(String url) {
		return es.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorSmbDocument.url())
				.isEqualTo(url));
	}

	public List<ConnectorSmbFolder> getFolders(String url) {
		return es.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorSmbFolder.url())
				.isEqualTo(url));
	}

	public ConnectorSmbDocument convertToSmbDocumentOrNull(ConnectorDocument document) {
		ConnectorSmbDocument result = null;

		String documentSchemaCode = document.getSchemaCode();
		String smbDocumentSchemaTypeCode = es.connectorSmbDocument.schemaType()
				.getCode();

		if (documentSchemaCode.contains(smbDocumentSchemaTypeCode)) {
			result = es.wrapConnectorSmbDocument(document.getWrappedRecord());
		}
		return result;
	}

	public ConnectorSmbFolder convertToSmbFolderOrNull(ConnectorDocument document) {
		ConnectorSmbFolder result = null;

		String documentSchemaCode = document.getSchemaCode();
		String smbFolderSchemaTypeCode = es.connectorSmbFolder.schemaType()
				.getCode();

		if (documentSchemaCode.contains(smbFolderSchemaTypeCode)) {
			result = es.wrapConnectorSmbFolder(document.getWrappedRecord());
		}
		return result;
	}

	public ConnectorDocument newConnectorDocument(String url) {
		if (smbUtils.isFolder(url)) {
			return es.newConnectorSmbFolder(connectorInstance);
		}
		return es.newConnectorSmbDocument(connectorInstance);
	}

	public ConnectorDocument getConnectorDocument(String url, ConnectorSmbInstance connectorInstance) {
		if (smbUtils.isFolder(url)) {
			return getFolder(url, connectorInstance);
		}
		return getDocument(url, connectorInstance);
	}

	public ConnectorSmbFolder getFolder(String url, ConnectorSmbInstance connectorInstance) {
		RecordServices recordServices = es.getModelLayerFactory().newRecordServices();
		String connectorUrl = DocumentSmbConnectorUrlCalculator.calculate(url, connectorInstance.getId());
		return es.wrapConnectorSmbFolder(recordServices.getRecordByMetadata(es.connectorSmbFolder.connectorUrl(), connectorUrl));
	}

	public ConnectorSmbDocument getDocument(String url, ConnectorSmbInstance connectorInstance) {
		RecordServices recordServices = es.getModelLayerFactory().newRecordServices();
		String connectorUrl = DocumentSmbConnectorUrlCalculator.calculate(url, connectorInstance.getId());
		return es.wrapConnectorSmbDocument(recordServices.getRecordsCaches().getCache(connectorInstance.getCollection())
				.getSummaryByMetadata(es.connectorSmbDocument.connectorUrl(), connectorUrl));
	}

	public ConnectorSmbDocument getDocumentFromCache(String url, ConnectorSmbInstance connectorInstance) {
		RecordServices recordServices = es.getModelLayerFactory().newRecordServices();
		String connectorUrl = DocumentSmbConnectorUrlCalculator.calculate(url, connectorInstance.getId());
		return es.wrapConnectorSmbDocument(recordServices.getRecordsCaches().getCache(connectorInstance.getCollection())
				.getSummaryByMetadata(es.connectorSmbDocument.connectorUrl(), connectorUrl));
	}

	public SmbModificationIndicator getSmbModificationIndicator(ConnectorSmbInstance connector, String url) {
		Metadata permissionHash = es.connectorSmbDocument.permissionsHash();
		Metadata size = es.connectorSmbDocument.size();
		Metadata lastModified = es.connectorSmbDocument.lastModified();
		Metadata documentConnectorUrlMetadata = es.connectorSmbDocument.connectorUrl();
		Metadata folderConnectorUrlMetadata = es.connectorSmbFolder.connectorUrl();

		String connectorUrlValue = DocumentSmbConnectorUrlCalculator.calculate(url, connector.getId());

		Record record = null;
		if(smbUtils.isFolder(url)) {
			record = recordServices.getRecordByMetadata(folderConnectorUrlMetadata, connectorUrlValue);
		} else {
			record = recordServices.getRecordsCaches().getCache(connector.getCollection()).getSummaryByMetadata(documentConnectorUrlMetadata, connectorUrlValue);
		}


		if(record == null) {
			return null;
		}

		String permissionHashValue = record.get(permissionHash);
		permissionHashValue = StringUtils.defaultString(permissionHashValue);
		Double sizeDouble = record.get(size);
		double sizeValue = 0;
		if (sizeDouble != null) {
			sizeValue = sizeDouble;
		}
		LocalDateTime lastModifiedDateTime = record.get(lastModified);
		long lastModifiedValue = -1;
		if (lastModifiedDateTime != null) {
			lastModifiedValue = lastModifiedDateTime.toDate().getTime();
		}

		SmbModificationIndicator databaseIndicator = new SmbModificationIndicator(permissionHashValue, sizeValue, lastModifiedValue);
		databaseIndicator.setParentId(record.getParentId());
		return databaseIndicator;
	}

	public void updateResumeUrl(String url) {
		connectorInstance.setResumeUrl(url);
	}


	public Set<String> duplicateDocuments() {
		LogicalSearchQuery query = new LogicalSearchQuery(es.fromAllDocumentsOf(connectorInstance.getId()))
			.addFieldFacet(Schemas.URL.getDataStoreCode())
			.setFieldFacetLimit(10_000)
			.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(Schemas.URL))
			.setNumberOfRows(0);
		Map<String, String[]> solrParams = new HashMap<>();
		solrParams.put("facet.mincount", new String[]{"2"});
		query.setOverridedQueryParams(solrParams);

		Set<String> urls = new HashSet<>();
		SPEQueryResponse response = es.getAppLayerFactory().getModelLayerFactory().newSearchServices().query(query);
		for (FacetValue facetValue : response.getFieldFacetValues(Schemas.URL.getDataStoreCode())) {
			urls.add(facetValue.getValue());
		}

		return urls;
	}
}