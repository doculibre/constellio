package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbUnmodifiedRetrievalJob extends SmbConnectorJob {
	private static final String jobName = SmbUnmodifiedRetrievalJob.class.getSimpleName();
	private final JobParams jobParams;

	public SmbUnmodifiedRetrievalJob(JobParams jobParams) {
		super(jobParams.getConnector(), jobName);
		this.jobParams = jobParams;
	}

	@Override
	public void execute(Connector connector) {
		jobParams.getConnector().getContext().traverseUnchanged(jobParams.getUrl(), jobParams.getConnectorInstance().getTraversalCode());
	}

	@Override
	public String getUrl() {
		return jobParams.getUrl();
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.UNMODIFIED_JOB;
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - ";
	}
}
