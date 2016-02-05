package com.constellio.app.modules.es.connectors.smb.queue;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public interface SmbJobQueue {
	public void init();
	public boolean isEmpty();
	public int size();
	public ConnectorJob poll();
	public boolean add(ConnectorJob job);
	public void clear();
}