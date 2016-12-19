package com.constellio.app.modules.es.connectors.smb.jobs;

import java.util.LinkedHashMap;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbNullJob extends SmbConnectorJob {
	private static final String jobName = SmbNullJob.class.getSimpleName();
	private final JobParams jobParams;

	public SmbNullJob(JobParams jobParams) {
		super(jobParams.getConnector(), jobName);
		this.jobParams = jobParams;
	}

	@Override
	public void execute(Connector connector) {
	}

	@Override
	public String getUrl() {
		return "smb://null/job/";
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.NULL_JOB;
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - ";
	}
}
