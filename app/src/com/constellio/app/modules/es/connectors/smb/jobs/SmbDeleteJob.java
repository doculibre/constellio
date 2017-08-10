package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.services.crawler.DeleteEventOptions;

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
        try {
            if (jobParams.getSmbUtils().isFolder(url)) {
                ConnectorSmbFolder folderToDelete = jobParams.getSmbRecordService().getFolder(url, jobParams.getConnectorInstance());
                if(folderToDelete != null) {
                    DeleteEventOptions options = new DeleteEventOptions();
                    options.getPhysicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(PHYSICALLY_DELETE_THEM);
                    options.getLogicalDeleteOptions().setBehaviorForRecordsAttachedToTaxonomy(LOGICALLY_DELETE_THEM);
                    jobParams.getEventObserver().deleteEvents(options, folderToDelete);
                }
            } else {
                ConnectorSmbDocument documentToDelete = jobParams.getSmbRecordService().getDocumentFromCache(url, jobParams.getConnectorInstance());
                if(documentToDelete != null) {
                    jobParams.getEventObserver().deleteEvents(documentToDelete);
                }
            }
        } catch (Exception e) {
            this.connector.getLogger().errorUnexpected(e);
        }
    }

    @Override
    public String toString() {
        return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getUrl();
    }

    @Override
    public String getUrl() {
        return jobParams.getUrl();
    }
}