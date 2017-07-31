package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.LastFetchedStatus;

import static com.constellio.app.ui.i18n.i18n.$;

import org.joda.time.LocalDateTime;

import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.schemas.Schemas;

import javax.activation.MimetypesFileTypeMap;

public class SmbDocumentOrFolderUpdater {

	private final ConnectorSmbInstance connectorInstance;
	private SmbRecordService smbRecordService;

	public SmbDocumentOrFolderUpdater(ConnectorSmbInstance connectorInstance, SmbRecordService smbRecordService) {
		this.connectorInstance = connectorInstance;
		this.smbRecordService = smbRecordService;
	}

	public void updateDocumentOrFolder(SmbFileDTO smbFileDTO, ConnectorDocument<?> documentOrFolder, String parentId, boolean seed) {
		ConnectorSmbDocument smbDocument = smbRecordService.convertToSmbDocumentOrNull(documentOrFolder);
		if (smbDocument != null) {
			updateFullDocumentDTO(smbFileDTO, smbDocument, parentId);
		} else {
			ConnectorSmbFolder smbFolder = smbRecordService.convertToSmbFolderOrNull(documentOrFolder);
			if (smbFolder != null) {
				updateFullFolderDTO(smbFileDTO, smbFolder, parentId, seed);
			}
		}
	}

	private void updateFullDocumentDTO(SmbFileDTO smbFileDTO, ConnectorSmbDocument smbDocument, String parentUrl) {

		// Utility
		smbDocument.setConnector(connectorInstance)
				.setTraversalCode(connectorInstance.getTraversalCode())
				.setFetched(true)
				.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
				.setFetchedDateTime(TimeProvider.getLocalDateTime())
				.setParentUrl(parentUrl);

		// Mandatory
		smbDocument.setUrl(smbFileDTO.getUrl())
				.setPermissionsHash(smbFileDTO.getPermissionsHash())
				.setManualTokens(smbFileDTO.getAllowTokens())
				.set(Schemas.DENY_TOKENS.getLocalCode(), smbFileDTO.getDenyTokens())
				.set(Schemas.SHARE_TOKENS.getLocalCode(), smbFileDTO.getAllowShareTokens())
				.set(Schemas.SHARE_DENY_TOKENS.getLocalCode(), smbFileDTO.getDenyShareTokens());


		String mimeType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(smbFileDTO.getUrl());

		// Optional
		smbDocument.setParsedContent(smbFileDTO.getParsedContent())
				.setSize(smbFileDTO.getLength())
				.setCreatedOn(new LocalDateTime(smbFileDTO.getCreateTime()))
				.setLastModified(new LocalDateTime(smbFileDTO.getLastModified()))
				.setPermissionsHash(smbFileDTO.getPermissionsHash())
				.setLanguage(smbFileDTO.getLanguage())
				.setExtension(smbFileDTO.getExtension())
				.setMimetype(mimeType)
				.setTitle(smbFileDTO.getName());

		// Errors
		smbDocument.setLastFetchAttemptDetails(smbFileDTO.getErrorMessage())
				.setLastFetchAttemptStatus(LastFetchedStatus.OK);

		smbDocument.setErrorCode(null);
		smbDocument.setErrorMessage(null);
		smbDocument.setErrorStackTrace(null);
		smbDocument.resetErrorsCount();

		smbDocument.addDateTimeProperty("dateCreated", new LocalDateTime(smbFileDTO.getCreateTime()))
				.withPropertyLabel("dateCreated", $("SmbDocumentOrFolderUpdater.dateCreated"));
		smbDocument.addDateTimeProperty("dateModified", new LocalDateTime(smbFileDTO.getLastModified()))
				.withPropertyLabel("dateModified", $("SmbDocumentOrFolderUpdater.dateModified"));
	}

	private void updateFullFolderDTO(SmbFileDTO smbFileDTO, ConnectorSmbFolder smbFolder, String parentUrl, boolean seed) {
		smbFolder.setTitle(smbFileDTO.getName())
				.setUrl(smbFileDTO.getUrl())
				.setTraversalCode(connectorInstance.getTraversalCode())
				.setConnector(connectorInstance)
				.setFetched(true)
				.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
				.setLastFetchedStatus(LastFetchedStatus.OK)
				.setCreatedOn(new LocalDateTime(smbFileDTO.getCreateTime()))
				.setLastModified(new LocalDateTime(smbFileDTO.getLastModified()))
				.setParentUrl(parentUrl);

		// Utility
		smbFolder.setTraversalCode(connectorInstance.getTraversalCode())
				.setConnector(connectorInstance)
				.setFetched(true)
				.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()));

		// Mandatory
		smbFolder.setUrl(smbFileDTO.getUrl())
				.setPermissionsHash(smbFileDTO.getPermissionsHash())
				.setManualTokens(smbFileDTO.getAllowTokens())
				.set(Schemas.DENY_TOKENS.getLocalCode(), smbFileDTO.getDenyTokens())
				.set(Schemas.SHARE_TOKENS.getLocalCode(), smbFileDTO.getAllowShareTokens())
				.set(Schemas.SHARE_DENY_TOKENS.getLocalCode(), smbFileDTO.getDenyShareTokens());

		// Optional

		// Errors
		if (parentUrl == null && !seed) {
			smbFolder.setLastFetchedStatus(LastFetchedStatus.PARTIAL);
		} else {
			smbFolder.setLastFetchedStatus(LastFetchedStatus.OK);
		}

		smbFolder.setErrorCode(null);
		smbFolder.setErrorMessage(null);
		smbFolder.setErrorStackTrace(null);
		smbFolder.resetErrorsCount();

		smbFolder.addDateTimeProperty("dateCreated", new LocalDateTime(smbFileDTO.getCreateTime()))
				.withPropertyLabel("dateCreated", $("SmbDocumentOrFolderUpdater.dateCreated"));
		smbFolder.addDateTimeProperty("dateModified", new LocalDateTime(smbFileDTO.getLastModified()))
				.withPropertyLabel("dateModified", $("SmbDocumentOrFolderUpdater.dateModified"));
	}

	public void updateFailedDocumentOrFolder(SmbFileDTO smbFileDTO, ConnectorDocument documentOrFolder, String parentUrl) {
		ConnectorSmbDocument smbDocument = smbRecordService.convertToSmbDocumentOrNull(documentOrFolder);
		if (smbDocument != null) {
			updateFailedDocumentDTO(smbFileDTO, smbDocument, parentUrl);
		} else {
			ConnectorSmbFolder smbFolder = smbRecordService.convertToSmbFolderOrNull(documentOrFolder);

			if (smbFolder != null) {
				updateFailedFolderDTO(smbFileDTO, smbFolder, parentUrl);
			}
		}
	}

	private void updateFailedDocumentDTO(SmbFileDTO smbFileDTO, ConnectorSmbDocument smbDocument, String parentUrl) {

		smbDocument.setConnector(connectorInstance)
				.setTraversalCode(connectorInstance.getTraversalCode())
				.setFetched(true)
				.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
				.setFetchedDateTime(TimeProvider.getLocalDateTime())
				.setUrl(smbFileDTO.getUrl())
				.setLastFetchAttemptDetails(smbFileDTO.getErrorMessage())
				.setLastFetchAttemptStatus(LastFetchedStatus.FAILED)
				.setParentUrl(parentUrl);

		smbDocument.setErrorCode("ErrorCode")
				.setErrorMessage(smbFileDTO.getErrorMessage())
				.setErrorStackTrace(smbFileDTO.getErrorMessage())
				.incrementErrorsCount();
	}

	private void updateFailedFolderDTO(SmbFileDTO smbFileDTO, ConnectorSmbFolder smbFolder, String parentUrl) {

		smbFolder.setConnector(connectorInstance)
				.setTraversalCode(connectorInstance.getTraversalCode())
				.setFetched(true)
				.setLastFetched(new LocalDateTime(smbFileDTO.getLastFetchAttempt()))
				.setFetchedDateTime(TimeProvider.getLocalDateTime())
				.setUrl(smbFileDTO.getUrl())
				.setLastFetchedStatus(LastFetchedStatus.FAILED)
				.setErrorCode("ErrorCode")
				.setErrorMessage(smbFileDTO.getErrorMessage())
				.setErrorStackTrace(smbFileDTO.getErrorMessage())
				.incrementErrorsCount()
				.setParentUrl(parentUrl);
	}
}