package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO.SmbFileDTOStatus;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.crawler.DeleteEventOptions;

import java.util.LinkedHashMap;

import static com.constellio.model.services.records.RecordLogicalDeleteOptions.LogicallyDeleteTaxonomyRecordsBehavior.LOGICALLY_DELETE_THEM;
import static com.constellio.model.services.records.RecordPhysicalDeleteOptions.PhysicalDeleteTaxonomyRecordsBehavior.PHYSICALLY_DELETE_THEM;

public class SmbDeleteJob extends SmbConnectorJob {
	private static final String jobName = SmbDeleteJob.class.getSimpleName();
	private final JobParams jobParams;

	public SmbDeleteJob(JobParams jobParams) {
		super(jobParams.getConnector(), jobName);
		this.jobParams = jobParams;
	}

	@Override
	public void execute(Connector connector) {
		String url = jobParams.getUrl();
		if (jobParams.getSmbUtils().isAccepted(url, jobParams.getConnectorInstance())) {
			SmbFileDTO smbFileDTO = jobParams.getSmbShareService().getSmbFileDTO(url, false);
			SmbFileDTOStatus status = smbFileDTO.getStatus();
			switch (status) {
			case DELETE_DTO:
				deleteRecords();
				break;
			case FAILED_DTO:
				// Do nothing
				break;
			case FULL_DTO:
				// Do nothing
				break;
			default:
				connector.getLogger()
						.error("Unexpected DTO status when deleting : " + url, "", new LinkedHashMap<String, String>());
				break;
			}
		} else {
			deleteRecords();
		}
	}

	private void deleteRecords() {
		String url = jobParams.getUrl();
		if (jobParams.getSmbUtils().isFolder(url)) {
			ConnectorSmbFolder folderToDelete = jobParams.getSmbRecordService().getFolder(url);
			if (folderToDelete != null) {
				DeleteEventOptions options = new DeleteEventOptions();
				options.getPhysicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(PHYSICALLY_DELETE_THEM);
				options.getLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(LOGICALLY_DELETE_THEM);
				jobParams.getEventObserver().deleteEvents(options, folderToDelete);
			}
		} else {
			ConnectorSmbDocument documentToDelete = jobParams.getSmbRecordService().getDocument(url);
			if (documentToDelete != null) {
				jobParams.getEventObserver().deleteEvents(documentToDelete);
			}
		}
		jobParams.getConnector().getContext().delete(url);
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getUrl();
	}

	@Override
	public String getUrl() {
		return jobParams.getUrl();
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.DELETE_JOB;
	}
}