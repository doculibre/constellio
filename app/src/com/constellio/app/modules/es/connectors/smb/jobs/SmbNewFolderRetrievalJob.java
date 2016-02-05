package com.constellio.app.modules.es.connectors.smb.jobs;

import java.util.Arrays;
import java.util.LinkedHashMap;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;

public class SmbNewFolderRetrievalJob extends ConnectorJob implements SmbConnectorJob {
	private static final String jobName = SmbNewFolderRetrievalJob.class.getSimpleName();
	private final ConnectorEventObserver eventObserver;
	private final SmbDocumentOrFolderUpdater updater;
	private final SmbRecordService smbRecordService;
	private final String url;
	private final SmbService smbService;
	private final String parentUrl;
	private final SmbJobFactory jobFactory;

	public SmbNewFolderRetrievalJob(Connector connector, String url, SmbService smbService, ConnectorEventObserver eventObserver,
			SmbRecordService smbRecordService, SmbDocumentOrFolderUpdater updater, String parentUrl, SmbJobFactory jobFactory) {
		super(connector, jobName);
		this.eventObserver = eventObserver;
		this.updater = updater;
		this.smbRecordService = smbRecordService;
		this.url = url;
		this.smbService = smbService;
		this.parentUrl = parentUrl;
		this.jobFactory = jobFactory;
	}

	@Override
	public void execute(Connector connector) {
		this.connector.getLogger()
				.debug("Executing " + toString(), "", new LinkedHashMap<String, String>());

		SmbFileDTO smbObject = smbService.getSmbFileDTO(url);

		switch (smbObject.getStatus()) {
		case FULL_DTO:
			ConnectorDocument fullFolder = smbRecordService.newConnectorSmbFolder(url);
			String parentId = smbRecordService.getRecordIdForFolder(parentUrl);
			updater.updateDocumentOrFolder(smbObject, fullFolder, parentId);
			eventObserver.push(Arrays.asList(fullFolder));
			smbRecordService.updateResumeUrl(url);
			break;
		case FAILED_DTO:
			ConnectorDocument failedFolder = smbRecordService.newConnectorSmbFolder(url);
			updater.updateFailedDocumentOrFolder(smbObject, failedFolder);
			eventObserver.push(Arrays.asList(failedFolder));
			break;
		case DELETE_DTO:
			try {
				ConnectorSmb connectorSmb = (ConnectorSmb) connector;
				ConnectorJob deleteJob = jobFactory.get(SmbJobCategory.DELETE, url, parentUrl);
				connectorSmb.queueJob(deleteJob);
			} catch (Exception e) {
				this.connector.getLogger()
						.errorUnexpected(e);
			}
			break;
		default:
			this.connector.getLogger()
					.error("Unexpected DTO type for : " + url, "", new LinkedHashMap<String, String>());
			break;
		}
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.NEW_FOLDER_JOB;
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + url;
	}
}