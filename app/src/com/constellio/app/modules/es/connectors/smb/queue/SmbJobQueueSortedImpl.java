package com.constellio.app.modules.es.connectors.smb.queue;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

import com.constellio.app.modules.es.connectors.smb.jobmanagement.SmbConnectorJob;
import com.constellio.app.modules.es.connectors.smb.utils.SmbUrlComparator;
import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class SmbJobQueueSortedImpl implements SmbJobQueue {
	private int INITIAL_QUEUE_CAPACITY = 10_000;
	private PriorityBlockingQueue<SmbConnectorJob> jobsQueue;

	public SmbJobQueueSortedImpl() {
		init();
	}

	@Override
	public void init() {
		jobsQueue = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, new Comparator<SmbConnectorJob>() {
			public int compare(SmbConnectorJob job1, SmbConnectorJob job2) {
				String url1 = job1.getUrl();
				String url2 = job2.getUrl();
				return new SmbUrlComparator().compare(url1, url2);
			}
		});
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
