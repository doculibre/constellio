package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;

public interface SmbConnectorJob {
	public String getUrl();
	public SmbJobType getType();
}
