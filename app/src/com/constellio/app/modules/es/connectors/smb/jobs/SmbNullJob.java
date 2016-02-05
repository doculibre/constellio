package com.constellio.app.modules.es.connectors.smb.jobs;

import java.util.LinkedHashMap;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbNullJob extends ConnectorJob implements SmbConnectorJob {
	private static final String jobName = SmbNullJob.class.getSimpleName();
	private static SmbNullJob instance;

	private SmbNullJob(Connector connector) {
		super(connector, jobName);
	}

	public static synchronized SmbNullJob getInstance(Connector connector) {
		if (instance == null) {
			instance = new SmbNullJob(connector);
		}
		return instance;
	}

	@Override
	public void execute(Connector connector) {
		// Do nothing.
		this.connector.getLogger()
				.debug("Executed " + toString(), "", new LinkedHashMap<String, String>());
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
