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

public class SmbNewDocumentRetrievalJob extends SmbConnectorJob {
	private static final String jobName = SmbNewDocumentRetrievalJob.class.getSimpleName();
	private final JobParams jobParams;

	public SmbNewDocumentRetrievalJob(JobParams params) {
		super(params.getConnector(), jobName);
		this.jobParams = params;
	}

	@Override
	public void execute(Connector connector) {
		String url = jobParams.getUrl();

		SmbFileDTO smbFileDTO = jobParams.getSmbShareService().getSmbFileDTO(url);

		switch (smbFileDTO.getStatus()) {
		case FULL_DTO:
			try {
				ConnectorDocument fullDocument = jobParams.getSmbRecordService().getDocument(url);
				if (fullDocument == null) {
					fullDocument = jobParams.getSmbRecordService().newConnectorSmbDocument(url);
				}
				String parentId = jobParams.getConnector().getContext().getParentId(url);
				if (parentId == null) {
					ConnectorSmbFolder parentFolder = jobParams.getSmbRecordService().getFolder(jobParams.getParentUrl());
					parentId = SmbRecordService.getSafeId(parentFolder);
				}
				jobParams.getUpdater().updateDocumentOrFolder(smbFileDTO, fullDocument, parentId);
				jobParams.getEventObserver().push(Arrays.asList(fullDocument));
				jobParams.getSmbRecordService().updateResumeUrl(url);
				jobParams.getConnector().getContext().traverseModified(url, new SmbModificationIndicator(smbFileDTO), parentId, jobParams.getConnectorInstance().getTraversalCode());
			} catch (Exception e) {
				this.connector.getLogger().errorUnexpected(e);
			}
			break;
		case FAILED_DTO:
			try {
				ConnectorDocument fullDocument = jobParams.getSmbRecordService().getDocument(url);
				if (fullDocument == null) {
					fullDocument = jobParams.getSmbRecordService().newConnectorSmbDocument(url);
				}
				String parentId = jobParams.getConnector().getContext().getParentId(url);
				if (parentId == null) {
					ConnectorSmbFolder parentFolder = jobParams.getSmbRecordService().getFolder(jobParams.getParentUrl());
					parentId = SmbRecordService.getSafeId(parentFolder);
				}
				jobParams.getUpdater().updateFailedDocumentOrFolder(smbFileDTO, fullDocument, parentId);
				jobParams.getEventObserver().push(Arrays.asList(fullDocument));
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
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getSmbUtils();
	}

	@Override
	public String getUrl() {
		return jobParams.getUrl();
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.NEW_DOCUMENT_JOB;
	}
}