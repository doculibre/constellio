package com.constellio.data.dao.services.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerCache;
import com.constellio.data.extensions.DataLayerExtensions;

public class SolrServers {
	private final SolrServerFactory solrServerFactory;
	private final Map<String, BigVaultServer> servers = new HashMap<>();
	private final BigVaultLogger bigVaultLogger;
	private final DataLayerExtensions extensions;
	private BigVaultServerCache bigVaultServerCache;

	public SolrServers(SolrServerFactory solrServerFactory, BigVaultLogger bigVaultLogger, DataLayerExtensions extensions
			, BigVaultServerCache bigVaultServerCache) {
		this.solrServerFactory = solrServerFactory;
		this.bigVaultLogger = bigVaultLogger;
		this.extensions = extensions;
		this.bigVaultServerCache = bigVaultServerCache;
	}

	public synchronized BigVaultServer getSolrServer(String core) {
		BigVaultServer server = servers.get(core);
		if (server == null) {
			server = new BigVaultServer(core, bigVaultLogger, solrServerFactory, extensions.getSystemWideExtensions(),
					bigVaultServerCache);
			servers.put(core, server);
		}
		return server;
	}

	public synchronized void close() {
		solrServerFactory.clear();
		servers.clear();
	}

	public Collection<BigVaultServer> getServers() {
		return servers.values();
	}

}
