package com.constellio.app.modules.es.connectors.smb.jobmanagement;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobFactoryImpl.SmbJobType;
import com.constellio.app.modules.es.connectors.spi.Connector;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public abstract class SmbConnectorJob extends ConnectorJob {

	public SmbConnectorJob(Connector connector, String jobName) {
		super(connector, jobName);
	}

	public abstract String getUrl();
	public abstract SmbJobType getType();
}
