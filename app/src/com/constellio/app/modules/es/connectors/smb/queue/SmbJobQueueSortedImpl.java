package com.constellio.app.modules.es.connectors.smb.queue;

import java.util.concurrent.PriorityBlockingQueue;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbJobComparator;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbJobQueueSortedImpl implements SmbJobQueue {
	private int INITIAL_QUEUE_CAPACITY = 10_000;
	private PriorityBlockingQueue<ConnectorJob> jobsQueue;

	public SmbJobQueueSortedImpl() {
		init();
	}

	@Override
	public void init() {
		jobsQueue = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, new SmbJobComparator());
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
		// TODO Benoit. Return null job instead of null
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
