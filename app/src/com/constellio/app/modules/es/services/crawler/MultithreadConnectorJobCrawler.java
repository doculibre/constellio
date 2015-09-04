/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
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
		executor = new ThreadPoolExecutor(1, 5, 10, TimeUnit.SECONDS, queue, new ThreadPoolExecutor.CallerRunsPolicy());
	}

	/* (non-Javadoc)
	 * @see connector.manager.ConnectorManager#crawl(java.util.List)
	 */
	public <V> void crawl(List<ConnectorJob> jobs)
			throws InterruptedException {
		Collection<Future<?>> futures = new LinkedList<Future<?>>();

		for (ConnectorJob job : jobs) {
			futures.add(executor.submit(job));
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
