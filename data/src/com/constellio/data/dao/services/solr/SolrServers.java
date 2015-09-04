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
package com.constellio.data.dao.services.solr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.constellio.data.dao.services.bigVault.solr.BigVaultLogger;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.extensions.DataLayerExtensions;

public class SolrServers {

	private final SolrServerFactory solrServerFactory;

	private final Map<String, BigVaultServer> servers = new HashMap<>();

	private final BigVaultLogger bigVaultLogger;

	private final DataLayerExtensions extensions;

	public SolrServers(SolrServerFactory solrServerFactory, BigVaultLogger bigVaultLogger, DataLayerExtensions extensions) {
		this.solrServerFactory = solrServerFactory;
		this.bigVaultLogger = bigVaultLogger;
		this.extensions = extensions;
	}

	public synchronized BigVaultServer getSolrServer(String core) {
		BigVaultServer server = servers.get(core);
		if (server == null) {
			server = new BigVaultServer(core, solrServerFactory.newSolrServer(core),
					solrServerFactory.getConfigFileSystem(core), solrServerFactory.getAdminServer(),
					bigVaultLogger, extensions.getSystemWideExtensions());
			servers.put(core, server);
		}
		return server;
	}

	public synchronized void close() {
		for (BigVaultServer server : servers.values()) {
			server.getNestedSolrServer().shutdown();
		}
		servers.clear();
	}

	public Collection<BigVaultServer> getServers() {
		return servers.values();
	}

}
