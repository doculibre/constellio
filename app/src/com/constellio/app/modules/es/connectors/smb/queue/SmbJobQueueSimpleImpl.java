package com.constellio.app.modules.es.connectors.smb.queue;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SmbJobQueueSimpleImpl implements SmbJobQueue {
	private Queue<SmbConnectorJob> jobsQueue;

	@Override
	public void init() {
		jobsQueue = new ConcurrentLinkedQueue<SmbConnectorJob>();
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
	public SmbConnectorJob poll() {
		return jobsQueue.poll();
	}

	@Override
	public boolean add(SmbConnectorJob job) {
		return jobsQueue.add(job);
	}

	@Override
	public void clear() {
		jobsQueue.clear();
	}
}