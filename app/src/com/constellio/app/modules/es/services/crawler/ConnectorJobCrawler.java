package com.constellio.app.modules.es.services.crawler;

import java.util.List;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

public interface ConnectorJobCrawler {

	public abstract <V> void crawl(List<ConnectorJob> jobs)
			throws Exception;

}
