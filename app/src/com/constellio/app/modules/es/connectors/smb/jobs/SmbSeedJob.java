package com.constellio.app.modules.es.connectors.smb.jobs;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;

public class SmbSeedJob extends SmbDispatchJob {
	private static final String jobName = SmbSeedJob.class.getSimpleName();
	private final JobParams jobParams;

	public SmbSeedJob(JobParams jobParams) {
		super(jobParams);
		this.jobParams = jobParams;
	}

	@Override
	public String getUrl() {
		return jobParams.getUrl();
	}

	@Override
	public String toString() {
		return jobName + '@' + Integer.toHexString(hashCode()) + " - " + jobParams.getUrl();
	}

	@Override
	public SmbJobType getType() {
		return SmbJobType.SEED_JOB;
	}
}