package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactory;
import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.smb.service.SmbService;
import com.constellio.app.modules.es.connectors.spi.Connector;

public class SmbSeedJob extends SmbDispatchJob {
	private static final String jobName = SmbSeedJob.class.getSimpleName();

	public SmbSeedJob(Connector connector, String url, SmbService smbService, SmbJobFactory jobFactory, String parentUrl) {
		super(connector, url, smbService, jobFactory, parentUrl, jobName);
	}
	
	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + url;
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.SEED_JOB;
	}
}