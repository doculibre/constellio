package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobs.*;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.smb.utils.SmbUrlComparator;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;

public class SmbJobFactoryImpl implements SmbJobFactory {
	public enum SmbJobCategory {
		DISPATCH, RETRIEVAL, DELETE
	}

	private final ConnectorSmb connector;
	private final ConnectorSmbInstance connectorInstance;
	private final ConnectorEventObserver eventObserver;
	private final ConnectorSmbUtils smbUtils;
	private final SmbShareService smbShareService;
	private final SmbRecordService smbRecordService;
	private final SmbDocumentOrFolderUpdater updater;
	private final SmbUrlComparator urlComparator;

	public SmbJobFactoryImpl(ConnectorSmb connector, ConnectorSmbInstance connectorInstance, ConnectorEventObserver eventObserver, SmbShareService smbShareService,
			ConnectorSmbUtils smbUtils, SmbRecordService smbRecordService, SmbDocumentOrFolderUpdater updater) {
		this.connector = connector;
		this.connectorInstance = connectorInstance;
		this.eventObserver = eventObserver;
		this.smbShareService = smbShareService;

		this.smbUtils = smbUtils;
		this.smbRecordService = smbRecordService;
		this.updater = updater;
		this.urlComparator = new SmbUrlComparator();
	}

	@Override
	public SmbConnectorJob get(SmbJobCategory jobType, String url, String parentUrl) {
		JobParams params = new JobParams(connector, eventObserver, smbUtils, connectorInstance, smbShareService, smbRecordService, updater, this, url, parentUrl);
		SmbConnectorJob job = null;

		if (smbUtils.isAccepted(url, connectorInstance)) {
			switch (jobType) {
			case DISPATCH:
				job = new SmbDispatchJob(params);
				break;
			case RETRIEVAL:
				if (this.connector.getDuplicateUrls().contains(url)) {
					job = new SmbDeleteJob(params);
					break;
				}

				SmbModificationIndicator recordSmbDocument = smbRecordService.getSmbModificationIndicator(connectorInstance, url);
				SmbModificationIndicator shareIndicator = smbShareService.getModificationIndicator(url);

				if (shareIndicator == null) {
					job = new SmbDeleteJob(params);
				} else if (recordSmbDocument == null) {
					job = new SmbNewRetrievalJob(params, shareIndicator, smbUtils.isFolder(url));
				} else if (!recordSmbDocument.equals(shareIndicator)) {
					job = new SmbNewRetrievalJob(params, shareIndicator, smbUtils.isFolder(url));
				}

				break;
			case DELETE:
				job = new SmbDeleteJob(params);
				break;
			default:
				break;
			}
		} else {
			job = new SmbDeleteJob(params);
		}
		return job;
	}
}