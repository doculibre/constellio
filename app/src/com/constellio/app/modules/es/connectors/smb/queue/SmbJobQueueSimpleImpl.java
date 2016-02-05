package com.constellio.app.modules.es.connectors.smb.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

// TODO Benoit. Check thread safety
public class SmbJobQueueSimpleImpl implements SmbJobQueue {
	private Queue<ConnectorJob> jobsQueue;

	@Override
	public void init() {
		jobsQueue = new ConcurrentLinkedQueue<ConnectorJob>();
	}

	@Override
	public boolean isEmpty() {
		return jobsQueue.isEmpty();
	}

	@Override
	public int size() {
		return jobsQueue.size();
	}

	@Override
	public ConnectorJob poll() {
		return jobsQueue.poll();
	}

	@Override
	public boolean add(ConnectorJob job) {
		return jobsQueue.add(job);
	}

	@Override
	public void clear() {
		jobsQueue.clear();
	}
}