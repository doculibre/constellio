package com.constellio.app.modules.es.connectors.smb.jobs;

import java.util.LinkedHashMap;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbResumeIgnoreJob extends ConnectorJob implements SmbConnectorJob {
	private static final String jobName = SmbResumeIgnoreJob.class.getSimpleName();
	private static SmbResumeIgnoreJob instance;

	private SmbResumeIgnoreJob(Connector connector) {
		super(connector, jobName);
	}

	public static synchronized SmbResumeIgnoreJob getInstance(Connector connector) {
		if (instance == null) {
			instance = new SmbResumeIgnoreJob(connector);
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
		return "smb://resumeIgnore/job/";
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.RESUME_IGNORE;
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - ";
	}
}
