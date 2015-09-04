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
package com.constellio.app.modules.es.connectors.smb;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.connectors.smb.SmbService.SmbStatus;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.connectors.spi.ConnectorLogger;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class SmbFetchJob extends ConnectorJob {
	private static final String JOB_DELETED = "Job Deleted";
	private static final String JOB_UPDATED = "Job Updated";
	private static final String JOB_PARTIALLY_UPDATED = "Job Partially Updated";
	private static final String JOB_UNKNOWN = "Job Unknown Status";
	private static final String JOB_SKIPPED = "Job Skipped";
	private static final String JOB_ADDED_CHILD = "Job Added Child";
	private static final String JOB_UPDATED_CHILD = "Job Updated Child";
	private static final String JOB_NOT_ACCEPTED = "Job Not Accepted";

	private final List<ConnectorDocument<?>> documents;
	private final ConnectorEventObserver eventObserver;
	private final ConnectorSmbInstance connectorInstance;
	private final SmbService smbService;
	private final ESSchemasRecordsServices es;
	private String traversalCode = "toDetermine";
	private final ConnectorSmbUtils smbUtils;
	private ConnectorLogger logger;

	public SmbFetchJob(Connector connector, List<ConnectorDocument<?>> documents, ConnectorSmbInstance connectorInstance, ConnectorEventObserver eventObserver,
			ESSchemasRecordsServices es, ConnectorLogger logger) {

		this(connector, documents, connectorInstance, eventObserver, es,
				new SmbService(connectorInstance.getDomain(), connectorInstance.getUsername(), connectorInstance.getPassword(), connectorInstance.getSeeds(),
						connectorInstance.getInclusions(), connectorInstance.getExclusions(), es, logger), logger);

	}

	public SmbFetchJob(Connector connector, List<ConnectorDocument<?>> documents, ConnectorSmbInstance connectorInstance, ConnectorEventObserver eventObserver,
			ESSchemasRecordsServices es, SmbService smbService, ConnectorLogger logger) {

		super(connector, "fetch");
		this.documents = documents;
		this.eventObserver = eventObserver;
		this.connectorInstance = connectorInstance;
		this.es = es;
		smbUtils = new ConnectorSmbUtils(es);
		this.logger = logger;
		this.smbService = smbService;
	}

	@Override
	public void execute(Connector connector) {
		for (ConnectorDocument document : documents) {

			String url = getUrl(document);

			if (getSmbUtils().isAccepted(url, connectorInstance)) {
				SmbFileDTO startingSmbObject = newSmbDTO(url);

				switch (startingSmbObject.getStatus()) {
				case PARTIAL:
					processPartialSmbObject(document, startingSmbObject);

					List<SmbFileDTO> children = smbService.getChildrenIn(startingSmbObject);
					for (SmbFileDTO child : children) {
						processChild(connector, document, startingSmbObject, child);
					}

					flushRecords();
					break;

				case OK:
					processFullSmbObject(document, startingSmbObject);

					children = smbService.getChildrenIn(startingSmbObject);
					for (SmbFileDTO child : children) {
						processChild(connector, document, startingSmbObject, child);
					}

					flushRecords();
					break;

				case GONE:
					eventObserver.deleteEvents(document);
					logger.info(JOB_DELETED, url, new HashMap<String, String>());
					break;

				case UNKNOWN:
					processExistenceUnknownSmbObject(document, startingSmbObject);
					break;

				case FAIL:
					processFailedSmbObject(document, startingSmbObject);
					flushRecords();
					break;

				default:
					break;
				}
			} else {
				LogicalSearchCondition condition = es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance)
						.andWhere(es.connectorSmbDocument.url())
						.isEqualTo(url);
				List<ConnectorDocument<?>> doc = es.searchConnectorDocuments(new LogicalSearchQuery(condition));
				if (!doc.isEmpty()) {
					eventObserver.deleteEvents(document);
					logger.info(JOB_NOT_ACCEPTED, url, new HashMap<String, String>());
				}
			}
		}
	}

	private String getUrl(ConnectorDocument document) {
		String url = "";
		ConnectorSmbDocument documentForUrl = convertToSmbDocumentOrNull(document);
		if (documentForUrl != null) {
			url = documentForUrl.getUrl();
		} else {
			ConnectorSmbFolder folderForUrl = convertToSmbFolderOrNull(document);
			if (folderForUrl != null) {
				url = folderForUrl.getUrl();
			}
		}
		return url;
	}

	protected ConnectorSmbUtils getSmbUtils() {
		return smbUtils;
	}

	private SmbFileDTO newSmbDTO(String url) {
		SmbFileDTO startingSmbFileDTO = new SmbFileDTO();

		startingSmbFileDTO = smbService.getSmbFileDTO(url);

		return startingSmbFileDTO;
	}

	private void processPartialSmbObject(ConnectorDocument document, SmbFileDTO startingSmbObject) {
		eventObserver.addUpdateEvents(updateDocumentOrFolder(document, startingSmbObject, LastFetchedStatus.PARTIAL));
		connector.getLogger()
				.info(JOB_UPDATED, startingSmbObject.getUrl(), new HashMap<String, String>());
	}

	private void processFullSmbObject(ConnectorDocument document, SmbFileDTO startingSmbObject) {
		eventObserver.addUpdateEvents(updateDocumentOrFolder(document, startingSmbObject, LastFetchedStatus.OK));
		connector.getLogger()
				.info(JOB_UPDATED, startingSmbObject.getUrl(), new HashMap<String, String>());
	}

	private void processExistenceUnknownSmbObject(ConnectorDocument document, SmbFileDTO startingSmbObject) {
		eventObserver.addUpdateEvents(updateUnknownDocumentOrFolder(document, startingSmbObject, LastFetchedStatus.FAILED));
		connector.getLogger()
				.info(JOB_UNKNOWN, startingSmbObject.getUrl(), new HashMap<String, String>());
	}

	private void processFailedSmbObject(ConnectorDocument document, SmbFileDTO startingSmbObject) {
		eventObserver.addUpdateEvents(updateDocumentOrFolder(document, startingSmbObject, LastFetchedStatus.FAILED));
		connector.getLogger()
				.info(JOB_PARTIALLY_UPDATED, startingSmbObject.getUrl(), new HashMap<String, String>());
	}

	protected ConnectorDocument updateDocumentOrFolder(ConnectorDocument document, SmbFileDTO smbFileDTO, LastFetchedStatus lastFetchedStatus) {
		ConnectorSmbDocument smbDocument = convertToSmbDocumentOrNull(document);
		if (smbDocument != null) {

			smbDocument.setTitle(smbFileDTO.getName())
					.setTraversalCode(connectorInstance.getTraversalCode())
					.setConnector(connectorInstance)
					.setFetched(true)
					.setParsedContent(smbFileDTO.getParsedContent())
					.setSize(smbFileDTO.getLength())
					.setLastModified(new LocalDateTime(smbFileDTO.getLastModified()))
					.setPermissionsHash(smbFileDTO.getPermissionsHash())
					.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
					.setLastFetchAttemptStatus(lastFetchedStatus)
					.setLanguage(smbFileDTO.getLanguage())
					.setExtension(smbFileDTO.getExtension());

			String fetchAttemptDetails = buildFetchAttemptDetails(smbFileDTO);
			smbDocument.setLastFetchAttemptDetails(fetchAttemptDetails);
		} else {
			ConnectorSmbFolder smbFolder = convertToSmbFolderOrNull(document);

			if (smbFolder != null) {
				smbFolder.setTitle(smbFileDTO.getName())
						.setTraversalCode(connectorInstance.getTraversalCode())
						.setConnector(connectorInstance)
						.setFetched(true)
						.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
						.setLastFetchedStatus(lastFetchedStatus);
			}
		}

		return document;
	}

	private String buildFetchAttemptDetails(SmbFileDTO smbFileDTO) {
		String fetchAttemptDetails = "";
		if (!smbFileDTO.getMissingMetadatas()
				.isEmpty()) {
			fetchAttemptDetails += "Missing metadatas : ";

			for (String missingMetadata : smbFileDTO.getMissingMetadatas()) {
				fetchAttemptDetails += missingMetadata + ",";
			}
			fetchAttemptDetails = StringUtils.removeEnd(fetchAttemptDetails, ",");
		}
		if (!smbFileDTO.getErrorMessage()
				.equals("")) {
			fetchAttemptDetails += ". Error : " + smbFileDTO.getErrorMessage();
		}
		return fetchAttemptDetails;
	}

	protected ConnectorDocument updateUnknownDocumentOrFolder(ConnectorDocument document, SmbFileDTO smbFileDTO, LastFetchedStatus lastFetchedStatus) {
		ConnectorSmbDocument smbDocument = convertToSmbDocumentOrNull(document);
		if (smbDocument != null) {

			smbDocument.setConnector(connectorInstance)
					.setFetched(true)
					.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
					.setLastFetchAttemptStatus(lastFetchedStatus);
		} else {
			ConnectorSmbFolder smbFolder = convertToSmbFolderOrNull(document);

			if (smbFolder != null) {
				smbFolder.setConnector(connectorInstance)
						.setFetched(true)
						.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
						.setLastFetchedStatus(lastFetchedStatus);
			}
		}

		return document;
	}

	private void processChild(Connector connector, ConnectorDocument document, SmbFileDTO startingSmbObject, SmbFileDTO child) {
		String childUrl = child.getUrl();
		if (getSmbUtils().isAccepted(childUrl, connectorInstance)) {
			if (child.getStatus() == SmbStatus.OK || child.getStatus() == SmbStatus.PARTIAL) {
				List<ConnectorDocument<?>> documentsOrFolders = getSmbUtils().getExistingDocumentsOrFoldersWithUrl(child.getUrl(), connectorInstance);

				if (!existsDocumentOrFolderIn(documentsOrFolders)) {
					processNewChild(document, startingSmbObject, childUrl);
				} else {
					processExistingChild(document, childUrl, documentsOrFolders);
				}

			} else {
				connector.getLogger()
						.error(JOB_SKIPPED, childUrl, new LinkedHashMap<String, String>());
			}
		}
	}

	private boolean existsDocumentOrFolderIn(List<ConnectorDocument<?>> documentsOrFolders) {
		return documentsOrFolders.isEmpty() ? false : true;
	}

	private void processNewChild(ConnectorDocument document, SmbFileDTO startingSmbObject, String childUrl) {
		ConnectorDocument<?> newConnectorDocument = newUnfetchedURLDocument(childUrl);
		newConnectorDocument.setManualTokens(Record.PUBLIC_TOKEN);
		if (startingSmbObject.isDirectory()) {
			setParent(newConnectorDocument, document.getId());
		}

		eventObserver.addUpdateEvents(newConnectorDocument);
		connector.getLogger()
				.info(JOB_ADDED_CHILD, childUrl, new HashMap<String, String>());
	}

	private ConnectorDocument newUnfetchedURLDocument(String url) {
		if (StringUtils.endsWith(url, "/")) {
			return es.newConnectorSmbFolder(connectorInstance)
					.setUrl(url)
					.setFetched(false);
		} else {
			return es.newConnectorSmbDocument(connectorInstance)
					.setUrl(url)
					.setFetched(false);
		}
	}

	private void processExistingChild(ConnectorDocument document, String childUrl, List<ConnectorDocument<?>> documentsOrFolders) {
		ConnectorDocument<?> existingRecord = documentsOrFolders.get(0);
		setParent(existingRecord, document.getId());
		eventObserver.addUpdateEvents(existingRecord.setFetched(false)
				.setTraversalCode(connectorInstance.getTraversalCode()));
		connector.getLogger()
				.info(JOB_UPDATED_CHILD, childUrl, new HashMap<String, String>());
	}

	private void flushRecords() {
		es.getRecordServices()
				.flush();
	}

	private ConnectorDocument setParent(ConnectorDocument document, String parentId) {
		ConnectorSmbDocument smbDocument = convertToSmbDocumentOrNull(document);
		if (smbDocument != null) {
			smbDocument.setParent(parentId);
		} else {
			ConnectorSmbFolder smbFolder = convertToSmbFolderOrNull(document);
			if (smbFolder != null) {
				smbFolder.setParent(parentId);
			}
		}

		return document;
	}

	private ConnectorSmbDocument convertToSmbDocumentOrNull(ConnectorDocument document) {
		ConnectorSmbDocument result = null;

		String documentSchemaCode = document.getSchemaCode();
		String smbDocumentSchemaTypeCode = es.connectorSmbDocument.schemaType()
				.getCode();

		if (documentSchemaCode.contains(smbDocumentSchemaTypeCode)) {
			result = es.wrapConnectorSmbDocument(document.getWrappedRecord());
		}
		return result;
	}

	private ConnectorSmbFolder convertToSmbFolderOrNull(ConnectorDocument document) {
		ConnectorSmbFolder result = null;

		String documentSchemaCode = document.getSchemaCode();
		String smbFolderSchemaTypeCode = es.connectorSmbFolder.schemaType()
				.getCode();

		if (documentSchemaCode.contains(smbFolderSchemaTypeCode)) {
			result = es.wrapConnectorSmbFolder(document.getWrappedRecord());
		}
		return result;
	}
}
