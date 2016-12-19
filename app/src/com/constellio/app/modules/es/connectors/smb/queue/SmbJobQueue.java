package com.constellio.app.modules.es.connectors.smb.queue;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;

public interface SmbJobQueue {
	public void init();
	public boolean isEmpty();
	public int size();
	public SmbConnectorJob poll();
	public boolean add(SmbConnectorJob job);
	public void clear();
}