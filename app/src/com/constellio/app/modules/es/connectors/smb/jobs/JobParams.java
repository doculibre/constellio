package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbDocumentOrFolderUpdater;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;

public class JobParams {

    private final ConnectorSmb connector;
    private final ConnectorEventObserver eventObserver;
    private final ConnectorSmbUtils smbUtils;
    private final ConnectorSmbInstance connectorInstance;
    private final SmbShareService smbShareService;
    private final SmbRecordService smbRecordService;
    private final SmbDocumentOrFolderUpdater updater;
    private final SmbJobFactory jobFactory;
    private final String url;
    private final String parentUrl;

    public JobParams(ConnectorSmb connector, ConnectorEventObserver eventObserver, ConnectorSmbUtils smbUtils, ConnectorSmbInstance connectorInstance, SmbShareService smbShareService, SmbRecordService smbRecordService, SmbDocumentOrFolderUpdater updater, SmbJobFactory jobFactory, String url, String parentUrl) {
        this.connector = connector;
        this.eventObserver = eventObserver;
        this.smbUtils = smbUtils;
        this.connectorInstance = connectorInstance;
        this.smbShareService = smbShareService;
        this.smbRecordService = smbRecordService;
        this.updater = updater;
        this.jobFactory = jobFactory;
        this.url = url;
        this.parentUrl = parentUrl;
    }

    public ConnectorSmb getConnector() {
        return connector;
    }

    public ConnectorEventObserver getEventObserver() {
        return eventObserver;
    }

    public ConnectorSmbUtils getSmbUtils() {
        return smbUtils;
    }

    public ConnectorSmbInstance getConnectorInstance() {
        return connectorInstance;
    }

    public SmbShareService getSmbShareService() {
        return smbShareService;
    }

    public SmbRecordService getSmbRecordService() {
        return smbRecordService;
    }

    public SmbDocumentOrFolderUpdater getUpdater() {
        return updater;
    }

    public SmbJobFactory getJobFactory() {
        return jobFactory;
    }

    public String getUrl() {
        return url;
    }

    public String getParentUrl() {
        return parentUrl;
    }
}
