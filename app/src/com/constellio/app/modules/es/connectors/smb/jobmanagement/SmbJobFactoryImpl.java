package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDeleteJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbDispatchJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbExistingFolderRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbModifiedDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNewFolderRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbNullJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbResumeIgnoreJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbSeedJob;
import com.constellio.app.modules.es.connectors.smb.jobs.SmbUnmodifiedDocumentRetrievalJob;
import com.constellio.app.modules.es.connectors.smb.service.SmbRecordService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.service.SmbService.SmbModificationIndicator;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.smb.utils.SmbUrlComparator;
import com.constellio.app.modules.es.connectors.spi.ConnectorEventObserver;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbDocument;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbFolder;
import com.constellio.app.modules.es.model.connectors.smb.ConnectorSmbInstance;

public class SmbJobFactoryImpl implements SmbJobFactory {
	public static enum SmbJobCategory {
		SEED, DISPATCH, RETRIEVAL, DELETE
	}

	public enum SmbJobType {
		SEED_JOB(0), NEW_DOCUMENT_JOB(1), NEW_FOLDER_JOB(2), MODIFIED_DOCUMENT_JOB(3), DISPATCH_JOB(4), UNMODIFIED_DOCUMENT_JOB(5), EXISTING_FOLDER_JOB(6), DELETE_JOB(
				7), NULL_JOB(8), RESUME_IGNORE(9);

		private int priority;

		private SmbJobType(int priority) {
			this.priority = priority;
		}

		public int getPriority() {
			return priority;
		}
	}

	private final ConnectorSmb connector;
	private final ConnectorSmbInstance connectorInstance;
	private final ConnectorEventObserver eventObserver;
	private final ConnectorSmbUtils smbUtils;
	private final SmbService smbService;
	private final Set<String> dispatchJobs;
	private final Set<String> retrievalJobs;
	private final Set<String> deleteJobs;
	private final Set<String> seedJobs;
	private final SmbRecordService smbRecordService;
	private final SmbDocumentOrFolderUpdater updater;
	private final SmbUrlComparator urlComparator;
	private String resumeUrl = "";

	public SmbJobFactoryImpl(ConnectorSmb connector, ConnectorSmbInstance connectorInstance, ConnectorEventObserver eventObserver, SmbService smbService,
			ConnectorSmbUtils smbUtils, SmbRecordService smbRecordService, SmbDocumentOrFolderUpdater updater) {
		this.connector = connector;
		this.connectorInstance = connectorInstance;
		this.eventObserver = eventObserver;
		this.smbService = smbService;

		this.smbUtils = smbUtils;
		dispatchJobs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		retrievalJobs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		deleteJobs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		seedJobs = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.smbRecordService = smbRecordService;
		this.updater = updater;
		this.urlComparator = new SmbUrlComparator();
	}

	@Override
	public ConnectorJob get(SmbJobCategory jobType, String url, String parentUrl) {
		ConnectorJob job = SmbNullJob.getInstance(connector);

		if (smbUtils.isAccepted(url, connectorInstance)) {
			switch (jobType) {
			case SEED:
				if (seedJobs.add(url)) {
					job = new SmbSeedJob(connector, url, smbService, this, parentUrl);
				}
				break;
			case DISPATCH:
				if (urlComparator.compare(url, resumeUrl) > -1) {
					if (dispatchJobs.add(url)) {
						job = new SmbDispatchJob(connector, url, smbService, this, parentUrl);
					}
				} else {
					job = SmbResumeIgnoreJob.getInstance(connector);
				}
				break;
			case RETRIEVAL:
				if (urlComparator.compare(url, resumeUrl) > -1) {
					if (retrievalJobs.add(url)) {
						if (smbUtils.isFolder(url)) {
							ConnectorSmbFolder recordFolder = smbRecordService.getFolder(url);
							if (recordFolder == null) {
								job = new SmbNewFolderRetrievalJob(connector, url, smbService, eventObserver, smbRecordService, updater, parentUrl, this);
								return job;
							} else {
								job = new SmbExistingFolderRetrievalJob(connector, url, recordFolder, smbService, eventObserver, smbRecordService, updater, parentUrl, this);
								return job;
							}
						} else {
							ConnectorSmbDocument recordDocument = smbRecordService.getDocument(url);
							if (recordDocument == null) {
								job = new SmbNewDocumentRetrievalJob(connector, url, smbService, eventObserver, smbRecordService, updater, parentUrl, this);
								return job;
							} else {
								SmbModificationIndicator indicators = smbService.getModificationIndicator(url);
								if (smbRecordService.isDocumentModified(recordDocument, url, indicators)) {
									job = new SmbModifiedDocumentRetrievalJob(connector, url, smbService, eventObserver, smbRecordService, updater, parentUrl,
											this);
									return job;
								} else {
									// Do nothing.
								}
							}
						}
					}
				} else {
					job = SmbResumeIgnoreJob.getInstance(connector);
				}
				break;
			case DELETE:
				if (urlComparator.compare(url, resumeUrl) > -1) {
					if (deleteJobs.add(url)) {
						job = new SmbDeleteJob(connector, url, eventObserver, smbRecordService, connectorInstance, smbService);
					}
				} else {
					// Do nothing.
				}
				break;
			default:
				break;
			}
		} else {
			if (deleteJobs.add(url)) {
				job = new SmbDeleteJob(connector, url, eventObserver, smbRecordService, connectorInstance, smbService);
			}
		}
		return job;
	}

	@Override
	public synchronized void reset() {
		dispatchJobs.clear();
		retrievalJobs.clear();
		deleteJobs.clear();
		seedJobs.clear();
	}

	@Override
	public void updateResumeUrl(String resumeUrl) {
		if (resumeUrl == null) {
			this.resumeUrl = "";
		} else {
			this.resumeUrl = resumeUrl;
		}
	}
}