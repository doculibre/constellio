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
import java.util.List;

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
		if (jobParams.getSmbUtils().isFolder(url)) {
			List<ConnectorSmbFolder> foldersToDelete = jobParams.getSmbRecordService().getFolders(url);
			if (!foldersToDelete.isEmpty()) {
				DeleteEventOptions options = new DeleteEventOptions();
				options.getPhysicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(PHYSICALLY_DELETE_THEM);
				options.getLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(LOGICALLY_DELETE_THEM);
				jobParams.getEventObserver().deleteEvents(options, foldersToDelete.toArray(new ConnectorSmbFolder[0]));
			}
		} else {
			List<ConnectorSmbDocument> documentsToDelete = jobParams.getSmbRecordService().getDocuments(url);
			if (!documentsToDelete.isEmpty()) {
				jobParams.getEventObserver().deleteEvents(documentsToDelete.toArray(new ConnectorSmbDocument[0]));
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