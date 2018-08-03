package com.constellio.app.modules.es.services.crawler;

import com.constellio.app.modules.es.connectors.spi.ConnectorJob;

import java.util.List;

public interface ConnectorJobCrawler {

	public abstract <V> void crawl(List<ConnectorJob> jobs)
			throws Exception;

}
