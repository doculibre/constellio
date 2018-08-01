package com.constellio.app.modules.es.services.crawler;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

import java.util.List;

public class SimpleConnectorJobCrawler implements ConnectorJobCrawler {

	public SimpleConnectorJobCrawler() {
	}

	/* (non-Javadoc)
	 * @see connector.manager.ConnectorManager#crawl(java.util.List)
	 */
	@Override
	public <V> void crawl(List<ConnectorJob> jobs)
			throws Exception {
		for (ConnectorJob job : jobs) {
			job.run();
		}
	}
}
