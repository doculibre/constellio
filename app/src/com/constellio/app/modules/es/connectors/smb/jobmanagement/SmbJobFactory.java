package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobCategory;

public interface SmbJobFactory {

	// Check for thread safety
	public SmbConnectorJob get(SmbJobCategory jobType, String url, String parentUrl);
}
