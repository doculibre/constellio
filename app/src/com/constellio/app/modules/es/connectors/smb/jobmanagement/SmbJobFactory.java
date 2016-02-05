package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public interface SmbJobFactory {

	// Check for thread safety
	public ConnectorJob get(SmbJobCategory jobType, String url, String parentUrl);

	// Check for thread safety
	public void reset();

	public void updateResumeUrl(String resumeUrl);
}
