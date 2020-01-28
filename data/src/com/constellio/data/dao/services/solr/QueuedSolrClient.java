package com.constellio.data.dao.services.solr;

import com.constellio.data.utils.ThreadList;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueuedSolrClient {

	SolrClient solrClient;
	BlockingQueue<QueuedSolrClientTask> queue;
	List<SolrInputDocument> currentBatch;
	int batchSize;
	int threadsCount;
	ThreadList<Thread> threadList;

	private QueuedSolrClient(SolrClient solrClient, int batchSize, int threadsCount) {
		this.solrClient = solrClient;
		this.queue = new LinkedBlockingQueue<QueuedSolrClientTask>((int) (threadsCount * 1.5));
		this.currentBatch = new ArrayList<>(batchSize);
		this.batchSize = batchSize;
		this.threadsCount = threadsCount;

	}

	public static QueuedSolrClient createAndStart(SolrClient solrClient, int batchSize, int threadsCount) {
		QueuedSolrClient queuedSolrClient = new QueuedSolrClient(solrClient, batchSize, threadsCount);
		queuedSolrClient.start(threadsCount);
		return queuedSolrClient;
	}

	private void start(int threadsCount) {
		this.threadList = new ThreadList<>();
		for (int i = 0; i < threadsCount; i++) {
			threadList.add(new QueuedSolrClientThread());
		}
		threadList.startAll();
	}

	public void addAsync(List<SolrInputDocument> solrInputDocuments) {
		for (SolrInputDocument solrInputDocument : solrInputDocuments) {
			addAsync(solrInputDocument);
		}
	}

	public void addAsync(SolrInputDocument solrInputDocument) {
		synchronized (this) {
			currentBatch.add(solrInputDocument);
			if (currentBatch.size() >= batchSize) {
				try {
					queue.put(new QueuedSolrClientTask(currentBatch));
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				currentBatch = new ArrayList<>(batchSize);
			}
		}
	}

	private static class QueuedSolrClientTask {

		List<SolrInputDocument> documents;

		public QueuedSolrClientTask(List<SolrInputDocument> documents) {
			this.documents = documents;
		}
	}

	private class QueuedSolrClientThread extends Thread {

		@Override
		public void run() {
			boolean running = true;
			while (running) {
				try {
					QueuedSolrClientTask task = queue.take();
					if (task.documents == null) {
						running = false;
					} else {
						try {
							solrClient.add(task.documents);
						} catch (SolrServerException e) {
							throw new RuntimeException(e);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public void flush() {
		commit();
	}

	public void close() {

		try {
			if (!currentBatch.isEmpty()) {
				queue.put(new QueuedSolrClientTask(currentBatch));
			}

			for (int i = 0; i < threadsCount; i++) {
				this.queue.put(new QueuedSolrClientTask(null));
			}
			threadList.joinAll();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		commit();
	}

	private void commit() {
		try {
			solrClient.commit();

		} catch (SolrServerException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
