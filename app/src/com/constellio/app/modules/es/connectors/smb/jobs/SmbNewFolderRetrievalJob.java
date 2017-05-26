package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class SmbNewFolderRetrievalJob extends SmbConnectorJob {
	private static final String jobName = SmbNewFolderRetrievalJob.class.getSimpleName();
	private final JobParams jobParams;

	public SmbNewFolderRetrievalJob(JobParams jobParams) {
		super(jobParams.getConnector(), jobName);
		this.jobParams = jobParams;
	}

	@Override
	public void execute(Connector connector) {
		String url = jobParams.getUrl();

		SmbFileDTO smbFileDTO = jobParams.getSmbShareService().getSmbFileDTO(url);

		switch (smbFileDTO.getStatus()) {
		case FULL_DTO:
			try {
				ConnectorSmbFolder fullFolder = jobParams.getSmbRecordService().getFolder(url);
				if (fullFolder == null) {
					fullFolder = jobParams.getSmbRecordService().newConnectorSmbFolder(url);
				}
				String parentId = jobParams.getConnector().getContext().getParentId(url);
				if (parentId == null && jobParams.getParentUrl() != null) {
					ConnectorSmbFolder parentFolder = jobParams.getSmbRecordService().getFolder(jobParams.getParentUrl());
					parentId = SmbRecordService.getSafeId(parentFolder);
					if (parentId == null) {
						//The cache should be empty too
						jobParams.getConnector().getContext().delete(jobParams.getParentUrl());
					}
				}
				boolean seed = false;
				if (jobParams.getConnectorInstance().getSeeds().contains(url)) {
					seed = true;
				}
				jobParams.getUpdater().updateDocumentOrFolder(smbFileDTO, fullFolder, parentId, seed);
				jobParams.getEventObserver().push(Arrays.asList((ConnectorDocument) fullFolder));
				jobParams.getSmbRecordService().updateResumeUrl(url);
				jobParams.getConnector().getContext().traverseModified(url, new SmbModificationIndicator(smbFileDTO), parentId, jobParams.getConnectorInstance().getTraversalCode());
			} catch (Exception e) {
				this.connector.getLogger().errorUnexpected(e);
			}
			break;
		case FAILED_DTO:
			try {
				ConnectorSmbFolder fullFolder = jobParams.getSmbRecordService().getFolder(url);
				if (fullFolder == null) {
					fullFolder = jobParams.getSmbRecordService().newConnectorSmbFolder(url);
				}
				String parentId = jobParams.getConnector().getContext().getParentId(url);
				if (parentId == null && jobParams.getParentUrl() != null) {
					ConnectorSmbFolder parentFolder = jobParams.getSmbRecordService().getFolder(jobParams.getParentUrl());
					parentId = SmbRecordService.getSafeId(parentFolder);
				}
				jobParams.getUpdater().updateFailedDocumentOrFolder(smbFileDTO, fullFolder, parentId);
				jobParams.getEventObserver().push(Arrays.asList((ConnectorDocument) fullFolder));
				jobParams.getConnector().getContext().traverseModified(url, new SmbModificationIndicator(smbFileDTO), parentId, jobParams.getConnectorInstance().getTraversalCode());
			} catch (Exception e) {
				this.connector.getLogger().errorUnexpected(e);
			}
			break;
		case DELETE_DTO:
			try {
				ConnectorSmb connectorSmb = (ConnectorSmb) connector;
				SmbConnectorJob deleteJob = jobParams.getJobFactory().get(SmbJobCategory.DELETE, url, jobParams.getParentUrl());
				connectorSmb.queueJob(deleteJob);
			} catch (Exception e) {
				this.connector.getLogger().errorUnexpected(e);
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
		return jobParams.getUrl();
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.NEW_FOLDER_JOB;
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getUrl();
	}
}