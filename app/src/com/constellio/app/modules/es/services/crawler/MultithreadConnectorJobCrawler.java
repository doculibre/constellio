package com.constellio.app.modules.es.services.crawler;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public class MultithreadConnectorJobCrawler implements ConnectorJobCrawler {

	private final BlockingQueue<Runnable> queue;
	private final ThreadPoolExecutor executor;

	public MultithreadConnectorJobCrawler() {
		queue = new LinkedBlockingQueue<>();
		int cores = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(cores, cores * 2, 10, TimeUnit.SECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/* (non-Javadoc)
	 * @see connector.manager.ConnectorManager#crawl(java.util.List)
	 */
	public <V> void crawl(List<ConnectorJob> jobs)
			throws InterruptedException {
		Collection<Future<?>> futures = new LinkedList<Future<?>>();

		for (ConnectorJob job : jobs) {
			if (job != null) {
				futures.add(executor.submit(job));
			}
		}

		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public void shutdown() {
		executor.shutdown();
	}
}
