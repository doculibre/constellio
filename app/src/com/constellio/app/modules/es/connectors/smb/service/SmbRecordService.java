package com.constellio.app.modules.es.connectors.smb.service;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;

import java.util.*;

import com.constellio.app.modules.es.constants.ESTaxonomies;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.model.entities.records.Record;
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

public class SmbRecordService {
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private ConnectorSmbUtils smbUtils;

	public SmbRecordService(ESSchemasRecordsServices es, ConnectorSmbInstance connectorInstance) {
		this.es = es;
		this.connectorInstance = connectorInstance;
		this.smbUtils = new ConnectorSmbUtils();
	}

	public static String getSafeId(ConnectorSmbFolder folder) {
		String folderId = null;
		if (folder != null) {
			folderId = folder.getId();
		}
		return folderId;
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

	public synchronized ConnectorSmbDocument newConnectorSmbDocument(String url) {
		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		return document;
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

	public synchronized ConnectorSmbFolder newConnectorSmbFolder(String url) {
		ConnectorSmbFolder folder = es.newConnectorSmbFolder(connectorInstance);
		return folder;
	}

	public List<String> getRecordsWithDifferentTraversalCode() {
		List<String> recordsToDelete = new ArrayList<>();

		for (Iterator<String> documentIterator = getDocumentUrlsToDelete(); documentIterator.hasNext(); ) {
			recordsToDelete.add(documentIterator.next());
		}

		for (Iterator<String> folderIterator = getFolderUrlsToDelete(); folderIterator.hasNext(); ) {
			recordsToDelete.add(folderIterator.next());
		}

		return recordsToDelete;
	}

	private Iterator<String> getDocumentUrlsToDelete() {
		return es.getUrlsIterator(new LogicalSearchQuery(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorDocument.traversalCode())
				.isNotEqual(connectorInstance.getTraversalCode())));

	}

	private Iterator<String> getFolderUrlsToDelete() {
		return es.getUrlsIterator(new LogicalSearchQuery(from(es.connectorSmbDocument.schemaType())
				.where(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance)
						.andWhere(es.connectorDocument.traversalCode())
						.isNotEqual(connectorInstance.getTraversalCode()))));
	}

	public ConnectorSmbFolder getFolder(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		} else {
			List<ConnectorSmbFolder> folders = getFolders(url);
			if (folders.isEmpty()) {
				return null;
			}
			return folders.get(0);
		}
	}

	public ConnectorSmbDocument getDocument(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		} else {
			List<ConnectorSmbDocument> documents = getDocuments(url);
			if (documents.isEmpty()) {
				return null;
			}
			return documents.get(0);
		}
	}

	public void updateResumeUrl(String url) {
		connectorInstance.setResumeUrl(url);
	}

	public Iterator<ConnectorSmbDocument> getAllDocumentsInFolder(ConnectorDocument<?> folderToDelete) {
		if (folderToDelete.getPaths().isEmpty()) {
			return new ArrayList<ConnectorSmbDocument>().iterator();
		}
		String path = folderToDelete.getPaths().get(0);
		return es.iterateConnectorSmbDocuments(where(Schemas.PATH).isStartingWithText(path));
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