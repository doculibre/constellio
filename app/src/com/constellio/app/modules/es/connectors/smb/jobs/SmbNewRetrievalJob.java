package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;

import java.util.Arrays;
import java.util.LinkedHashMap;

public class SmbNewRetrievalJob extends SmbConnectorJob {
    private static final String jobName = SmbNewRetrievalJob.class.getSimpleName();
    private final boolean folder;
    private final JobParams jobParams;
    private final SmbModificationIndicator shareIndicator;


    public SmbNewRetrievalJob(JobParams params, SmbModificationIndicator shareIndicator, boolean folder) {
        super(params.getConnector(), jobName);
        this.jobParams = params;
        this.shareIndicator = shareIndicator;
        this.folder = folder;
    }

    @Override
    public void execute(Connector connector) {
        String url = jobParams.getUrl();

        boolean seed = jobParams.getConnectorInstance().getSeeds().contains(url);

        SmbFileDTO smbFileDTO = jobParams.getSmbShareService().getSmbFileDTO(url);

        SmbRecordService smbRecordService = jobParams.getSmbRecordService();
        switch (smbFileDTO.getStatus()) {
            case FULL_DTO:
                try {
                    ConnectorDocument connectorDocument = (ConnectorDocument) jobParams.getConnector().getCachedConnectorDocument(url);
                    if(connectorDocument == null) {
                        connectorDocument = smbRecordService.newConnectorDocument(url);
                    }
                    jobParams.getUpdater().updateDocumentOrFolder(smbFileDTO, connectorDocument, jobParams.getParentUrl(), seed);
                    jobParams.getEventObserver().push(Arrays.asList((ConnectorDocument) connectorDocument));

                } catch (Exception e) {
                    this.connector.getLogger().errorUnexpected(e);
                }
                break;
            case FAILED_DTO:
                try {
                    ConnectorDocument connectorDocument = (ConnectorDocument) jobParams.getConnector().getCachedConnectorDocument(url);
                    if(connectorDocument == null) {
                        connectorDocument = smbRecordService.newConnectorDocument(url);
                    } else if(!jobParams.getSmbUtils().isFolder(url)) {
                        break;
                    }
                    jobParams.getUpdater().updateFailedDocumentOrFolder(smbFileDTO, connectorDocument, jobParams.getParentUrl());
                    jobParams.getEventObserver().push(Arrays.asList((ConnectorDocument) connectorDocument));

                } catch (Exception e) {
                    this.connector.getLogger().errorUnexpected(e);
                }
                break;
            case DELETE_DTO:
                try {
                    SmbConnectorJob deleteJob = jobParams.getJobFactory().get(SmbJobCategory.DELETE, url, jobParams.getParentUrl());
                    ConnectorSmb connectorSmb = (ConnectorSmb) connector;
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
    public String toString() {
        return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getSmbUtils();
    }
}