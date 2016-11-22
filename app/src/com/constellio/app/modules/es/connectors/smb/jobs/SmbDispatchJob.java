package com.constellio.app.modules.es.connectors.smb.jobs;

import java.util.LinkedHashMap;
import java.util.List;

import com.constellio.app.modules.es.connectors.smb.ConnectorSmb;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.smb.utils.ConnectorSmbUtils;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbDispatchJob extends ConnectorJob implements SmbConnectorJob {
	private static final String jobName = SmbDispatchJob.class.getSimpleName();
	protected final String url;
	private final ConnectorSmbUtils smbUtils;
	private final SmbService smbService;
	private final SmbJobFactory jobFactory;
	private final String parentUrl;

	public SmbDispatchJob(Connector connector, String url, SmbService smbService, SmbJobFactory jobFactory, String parentUrl, String jobName) {
		super(connector, jobName);
		this.url = url;
		smbUtils = new ConnectorSmbUtils();
		this.smbService = smbService;
		this.jobFactory = jobFactory;
		this.parentUrl = parentUrl;
	}

	public SmbDispatchJob(Connector connector, String url, SmbService smbService, SmbJobFactory jobFactory, String parentUrl) {
		this(connector, url, smbService, jobFactory, parentUrl, jobName);
	}

	@Override
	public void execute(Connector connector) {
		ConnectorSmb connectorSmb = (ConnectorSmb) connector;

		connector.getLogger()
				.debug("Executing " + toString(), "", new LinkedHashMap<String, String>());

		ConnectorJob smbRetrievalJob = jobFactory.get(SmbJobCategory.RETRIEVAL, url, parentUrl);
		if (smbRetrievalJob != SmbNullJob.getInstance(connectorSmb)) {
			connectorSmb.queueJob(smbRetrievalJob);

			if (!(smbRetrievalJob instanceof SmbDeleteJob)) {
				if (smbUtils.isFolder(url)) {
					List<String> childrenUrls = smbService.getChildrenUrlsFor(url);
					for (String childUrl : childrenUrls) {
						if (smbUtils.isFolder(childUrl)) {
							ConnectorJob smbChildFolderRetrievalJob = jobFactory.get(SmbJobCategory.DISPATCH, childUrl, url);

							if (smbChildFolderRetrievalJob == SmbResumeIgnoreJob.getInstance(connectorSmb)) {
								// IgnoreJob. Do nothing.
								connector.getLogger()
										.debug(toString(), " Resume Job : Folder ignored", new LinkedHashMap<String, String>());
							} else if (smbChildFolderRetrievalJob == SmbNullJob.getInstance(connectorSmb)) {
								// NullJob. Do nothing.
								connector.getLogger()
										.debug(toString(), " Unexpected child folder NullJob", new LinkedHashMap<String, String>());
							} else {
								connectorSmb.queueJob(smbChildFolderRetrievalJob);
							}
						} else {
							ConnectorJob smbChildDocumentRetrievalJob = jobFactory.get(SmbJobCategory.RETRIEVAL, childUrl, url);
							if (smbChildDocumentRetrievalJob == SmbResumeIgnoreJob.getInstance(connectorSmb)) {
								// IgnoreJob. Do nothing.
								connector.getLogger()
										.debug(toString(), " Resume Job : Document ignored", new LinkedHashMap<String, String>());
							} else if (smbChildDocumentRetrievalJob == SmbNullJob.getInstance(connectorSmb)) {
								// NullJob. Do nothing.
								connector.getLogger()
										.debug(toString(), " Unexpected child document NullJob", new LinkedHashMap<String, String>());
							} else {
								connectorSmb.queueJob(smbChildDocumentRetrievalJob);
							}
						}
					}
				} else {
					// File has no children. Do nothing
					connector.getLogger()
							.info(toString(), " Ignoring. Dispatch only on Folders", new LinkedHashMap<String, String>());
				}
			} else {
				connector.getLogger()
						.info(toString(), " Changed to DeleteJob", new LinkedHashMap<String, String>());
			}
		}
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + url;
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.DISPATCH_JOB;
	}
}