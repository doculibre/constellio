package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.ContextUtils;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbFileDTO;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.model.connectors.ConnectorDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.model.entities.schemas.Schemas;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class SmbNewRetrievalJob extends SmbConnectorJob {
    private static final String jobName = SmbNewRetrievalJob.class.getSimpleName();
    private final boolean folder;
    private final JobParams jobParams;
    private final SmbModificationIndicator shareIndicator;

    private long getLastModified(ConnectorDocument connectorDocument) {
        long millis = -1;
        if (connectorDocument.getLastModified() != null) {
            millis = connectorDocument.getLastModified().toDateTime().getMillis();
        }
        return millis;
    }

    private SmbModificationIndicator getSmbModificationIndicatorFromDatabase(ConnectorSmbDocument fullDocument) {
        SmbModificationIndicator databaseIndicator = new SmbModificationIndicator(
                fullDocument.getPermissionsHash(),
                fullDocument.getSize(),
                getLastModified(fullDocument)
        );
        return databaseIndicator;
    }

    private SmbModificationIndicator getSmbModificationIndicatorFromDatabase(ConnectorSmbFolder fullDocument) {
        SmbModificationIndicator databaseIndicator = new SmbModificationIndicator(
                "",
                0,
                getLastModified(fullDocument)
        );
        return databaseIndicator;
    }

    private boolean updateConnectorContext(SmbModificationIndicator databaseIndicator, String parentId) {
        jobParams.getConnector().getContext().traverseModified(jobParams.getUrl(), databaseIndicator, parentId, jobParams.getConnectorInstance().getTraversalCode());
        boolean folder = jobParams.getSmbUtils().isFolder(jobParams.getUrl());
        return !ContextUtils.equals(shareIndicator, databaseIndicator, folder);
    }

    private String getParentId() {
        String parentId = jobParams.getConnector().getContext().getParentId(jobParams.getUrl());
        if (parentId == null && StringUtils.isNotEmpty(jobParams.getParentUrl())) {
            ConnectorSmbFolder parentFolder = jobParams.getSmbRecordService().getFolder(jobParams.getParentUrl());
            parentId = SmbRecordService.getSafeId(parentFolder);
            if (parentId == null && !jobParams.getUpdater().recentlyUpdated(jobParams.getParentUrl()) ||
                    parentFolder != null && parentFolder.isLogicallyDeletedStatus()) {
                //The cache should be empty too
                this.connector.getLogger().info("Invalidate cache entry", "URL : " + jobParams.getParentUrl(), Collections.EMPTY_MAP);
                jobParams.getConnector().getContext().delete(jobParams.getParentUrl());
            }
        }
        return parentId;
    }

    public SmbNewRetrievalJob(JobParams params, SmbModificationIndicator shareIndicator, boolean folder) {
        super(params.getConnector(), jobName);
        this.jobParams = params;
        this.shareIndicator = shareIndicator;
        this.folder = folder;
    }

    @Override
    public void execute(Connector connector) {
        String url = jobParams.getUrl();

        String parentId = null;
        SmbModificationIndicator databaseIndicator = null;
        ConnectorDocument connectorDocument = null;
        boolean logicallyDeletedStatus = false;
        if (folder) {
            ConnectorSmbFolder fullFolder = jobParams.getSmbRecordService().getFolder(url);
            if (fullFolder != null) {
                if (fullFolder.isLogicallyDeletedStatus()) {
                    logicallyDeletedStatus = true;
                    fullFolder.set(Schemas.LOGICALLY_DELETED_STATUS, false);
                    fullFolder.set(Schemas.LOGICALLY_DELETED_ON, null);
                }
                connectorDocument = fullFolder;
                databaseIndicator = getSmbModificationIndicatorFromDatabase(fullFolder);
                parentId = fullFolder.getParent();
            }
        } else {
            ConnectorSmbDocument fullDocument = jobParams.getSmbRecordService().getDocument(url);
            if (fullDocument != null) {
                if (fullDocument.isLogicallyDeletedStatus()) {
                    logicallyDeletedStatus = true;
                    fullDocument.set(Schemas.LOGICALLY_DELETED_STATUS, false);
                    fullDocument.set(Schemas.LOGICALLY_DELETED_ON, null);
                }
                connectorDocument = fullDocument;
                databaseIndicator = getSmbModificationIndicatorFromDatabase(fullDocument);
                parentId = fullDocument.getParent();
            }
        }
        if (databaseIndicator != null && !logicallyDeletedStatus) {
            boolean modified = updateConnectorContext(databaseIndicator, parentId);
            if (!modified && (parentId != null || jobParams.getConnectorInstance().getSeeds().contains(url))) {
                return;
            }
        }

        boolean seed = jobParams.getConnectorInstance().getSeeds().contains(url);

        SmbFileDTO smbFileDTO = jobParams.getSmbShareService().getSmbFileDTO(url);

        switch (smbFileDTO.getStatus()) {
            case FULL_DTO:
                try {
                    if (connectorDocument == null) {
                        if (folder) {
                            connectorDocument = jobParams.getSmbRecordService().newConnectorSmbFolder(url);
                        } else {
                            connectorDocument = jobParams.getSmbRecordService().newConnectorSmbDocument(url);
                        }
                    }
                    parentId = getParentId();
                    jobParams.getUpdater().updateDocumentOrFolder(smbFileDTO, connectorDocument, parentId, seed);
                    jobParams.getEventObserver().push(Arrays.asList((ConnectorDocument) connectorDocument));
                    jobParams.getSmbRecordService().updateResumeUrl(url);
                } catch (Exception e) {
                    this.connector.getLogger().errorUnexpected(e);
                }
                break;
            case FAILED_DTO:
                try {
                    if (connectorDocument == null) {
                        if (folder) {
                            connectorDocument = jobParams.getSmbRecordService().newConnectorSmbFolder(url);
                        } else {
                            connectorDocument = jobParams.getSmbRecordService().newConnectorSmbDocument(url);
                        }
                    }
                    parentId = getParentId();
                    jobParams.getUpdater().updateFailedDocumentOrFolder(smbFileDTO, connectorDocument, parentId);
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

    @Override
    public SmbJobType getType() {
        return SmbJobType.NEW_DOCUMENT_JOB;
    }
}