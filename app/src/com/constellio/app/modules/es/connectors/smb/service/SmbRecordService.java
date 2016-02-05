package com.constellio.app.modules.es.connectors.smb.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;

public class SmbRecordService {
	private ESSchemasRecordsServices es;
	private ConnectorSmbInstance connectorInstance;
	private ConnectorSmbUtils smbUtils;
	private SmbRecordServiceCache cache;

	public SmbRecordService(ESSchemasRecordsServices es, ConnectorSmbInstance connectorInstance) {
		this(es, connectorInstance, new SmbRecordServiceCache());
	}

	public SmbRecordService(ESSchemasRecordsServices es, ConnectorSmbInstance connectorInstance, SmbRecordServiceCache cache) {
		this.es = es;
		this.connectorInstance = connectorInstance;
		this.smbUtils = new ConnectorSmbUtils();
		this.cache = cache;
	}

	public synchronized boolean isNew(String url) {
		if (smbUtils.isFolder(url)) {
			String recordId = getRecordIdForFolder(url);
			if (StringUtils.isBlank(recordId)) {
				return true;
			} else {
				return false;
			}
		} else {
			String recordId = getRecordIdForDocument(url);
			if (StringUtils.isBlank(recordId)) {
				return true;
			} else {
				return false;
			}
		}
	}

	private List<ConnectorSmbDocument> getDocuments(String url) {
		return es.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorSmbDocument.url())
				.isEqualTo(url));
	}

	public boolean isModified(String url, long currentLastModified, String currentPermissionHash, double currentSize) {
		List<ConnectorSmbDocument> documents = getDocuments(url);

		LocalDateTime locaDateTime = null;
		String permissionHash = "";
		double size = -2;

		if (!documents.isEmpty()) {
			ConnectorSmbDocument document = documents.get(0);
			locaDateTime = document.getLastModified();
			permissionHash = document.getPermissionsHash();
			size = document.getSize();
		}

		if (StringUtils.equals(currentPermissionHash, permissionHash)) {
			if (currentSize == size) {
				if ((Math.abs(currentLastModified - locaDateTime.toDateTime()
						.getMillis())) <= 1) {
					return false;
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	public List<ConnectorDocument<?>> getExistingDocumentsWithUrl(String url) {
		List<ConnectorSmbDocument> documents = getDocuments(url);
		List<ConnectorDocument<?>> existingDocuments = new ArrayList<>();

		for (ConnectorSmbDocument document : documents) {
			existingDocuments.add(document);
		}

		return existingDocuments;
	}

	public List<ConnectorDocument<?>> getExistingFoldersWithUrl(String url) {
		List<ConnectorSmbFolder> folders = getFolders(url);
		List<ConnectorDocument<?>> existingFolders = new ArrayList<>();

		for (ConnectorSmbFolder folder : folders) {
			existingFolders.add(folder);
		}

		return existingFolders;
	}

	private List<ConnectorSmbFolder> getFolders(String url) {
		return es.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorSmbFolder.url())
				.isEqualTo(url));
	}

	public synchronized ConnectorSmbDocument newConnectorSmbDocument(String url) {
		ConnectorSmbDocument document = es.newConnectorSmbDocument(connectorInstance);
		cache.add(url, document.getId());
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
		cache.add(url, folder.getId());
		return folder;
	}

	public List<String> getRecordsWithDifferentTraversalCode() {
		List<String> recordsToDelete = new ArrayList<>();

		for (ConnectorSmbDocument document : getDocumentsToDelete()) {
			recordsToDelete.add(document.getUrl());
		}

		for (ConnectorSmbFolder folder : getFoldersToDelete()) {
			recordsToDelete.add(folder.getUrl());
		}

		return recordsToDelete;
	}

	private List<ConnectorSmbDocument> getDocumentsToDelete() {
		return es.searchConnectorSmbDocuments(es.fromConnectorSmbDocumentWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorDocument.traversalCode())
				.isNotEqual(connectorInstance.getTraversalCode()));
	}

	private List<ConnectorSmbFolder> getFoldersToDelete() {
		return es.searchConnectorSmbFolders(es.fromConnectorSmbFolderWhereConnectorIs(connectorInstance)
				.andWhere(es.connectorDocument.traversalCode())
				.isNotEqual(connectorInstance.getTraversalCode()));
	}

	public String getRecordIdForFolder(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		} else {

			String cachedRecordId = cache.getRecordId(url);

			if (StringUtils.isBlank(cachedRecordId)) {
				List<ConnectorSmbFolder> folders = getFolders(url);

				if (folders.isEmpty()) {
					return null;
				} else {
					String recordId = folders.get(0)
							.getId();
					cache.add(url, recordId);
					return recordId;
				}

			} else {
				// Makes it slower but more accurate in case records are deleted.
				try {
					ConnectorSmbFolder correspondingFolder = es.getConnectorSmbFolder(cachedRecordId);

					if (StringUtils.equals(correspondingFolder.getUrl(), url)) {
						return cachedRecordId;
					} else {
						cache.remove(url);
						return null;
					}
				} catch (Exception e) {
					cache.remove(url);
					return null;
				}
			}
		}
	}

	public String getRecordIdForDocument(String url) {
		if (StringUtils.isBlank(url)) {
			return null;
		} else {

			String cachedRecordId = cache.getRecordId(url);

			if (StringUtils.isBlank(cachedRecordId)) {
				List<ConnectorSmbDocument> documents = getDocuments(url);

				if (documents.isEmpty()) {
					return null;
				} else {
					String recordId = documents.get(0)
							.getId();
					cache.add(url, recordId);
					return recordId;
				}

			} else {
				// Makes it slower but more accurate in case records are deleted.
				try {
					ConnectorSmbDocument correspondingDocument = es.getConnectorSmbDocument(cachedRecordId);

					if (StringUtils.equals(correspondingDocument.getUrl(), url)) {
						return cachedRecordId;
					} else {
						cache.remove(url);
						return null;
					}
				} catch (Exception e) {
					cache.remove(url);
					return null;
				}
			}
		}
	}

	public void updateResumeUrl(String url) {
		// TODO Benoit. Evaluate if it should be synchronized
		connectorInstance.setResumeUrl(url);
	}
}