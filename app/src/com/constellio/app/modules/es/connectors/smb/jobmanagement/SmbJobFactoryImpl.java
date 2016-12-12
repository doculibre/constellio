package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.cache.SmbConnectorContext;
import com.constellio.app.modules.es.connectors.smb.jobs.*;
import com.constellio.app.modules.es.connectors.smb.service.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbShareService;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.smb.utils.SmbUrlComparator;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SmbJobFactoryImpl implements SmbJobFactory {
	public static enum SmbJobCategory {
		SEED, DISPATCH, RETRIEVAL, DELETE
	}

	public enum SmbJobType {
		SEED_JOB, NEW_DOCUMENT_JOB, NEW_FOLDER_JOB, DISPATCH_JOB, UNMODIFIED_JOB, DELETE_JOB, NULL_JOB;
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
		SmbConnectorJob job = new SmbNullJob(params);

		if (smbUtils.isAccepted(url, connectorInstance)) {
			switch (jobType) {
			case SEED:
				job = new SmbSeedJob(params);
				break;
			case DISPATCH:
				job = new SmbDispatchJob(params);
				break;
			case RETRIEVAL:
				SmbConnectorContext context = this.connector.getContext();
				SmbModificationIndicator contextIndicator = context.getModificationIndicator(url);

				if (smbUtils.isFolder(url)) {
					if (contextIndicator == null) {
						job = new SmbNewFolderRetrievalJob(params);
					} else {
						SmbModificationIndicator shareIndicator = smbShareService.getModificationIndicator(url);
						if (shareIndicator == null) {
							job = new SmbDeleteJob(params);
						} else if (contextIndicator.getParentId() == null || !contextIndicator.equals(shareIndicator)) {
							job = new SmbNewFolderRetrievalJob(params);
						}
					}
				} else {
					if (contextIndicator == null) {
						job = new SmbNewDocumentRetrievalJob(params);
					} else {
						SmbModificationIndicator shareIndicator = smbShareService.getModificationIndicator(url);
						if (shareIndicator == null) {
							job = new SmbDeleteJob(params);
						} else if (contextIndicator.getParentId() == null || !contextIndicator.equals(shareIndicator)) {
							job = new SmbNewDocumentRetrievalJob(params);
						}
					}
				}
				if (job instanceof SmbNullJob) {
					job = new SmbUnmodifiedRetrievalJob(params);
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