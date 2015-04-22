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
package com.constellio.data.dao.services.solr.serverFactories;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

public class CloudSolrServerFactory implements SolrServerFactory {

	private static Map<String, CloudSolrClient> solrServers = new HashMap<String, CloudSolrClient>();
	private final String zkHost;

	public CloudSolrServerFactory(String zkHost) {
		super();
		this.zkHost = zkHost;
	}

	@Override
	public SolrClient newSolrServer(String core) {
		if (solrServers.containsKey(core)) {
			return solrServers.get(core);
		}
		CloudSolrClient server = new CloudSolrClient(zkHost) {
			@Override
			public void shutdown() {
				//super.shutdown();
			}
		};
		server.setDefaultCollection(core);
		//server.setParser(null);
		//server.setZkClientTimeout(60000);
		//server.setZkConnectTimeout(60000);
		//server.connect();
		solrServers.put(core, server);
		return server;
	}

	@Override
	public void clear() {
	}

	@Override
	public AtomicFileSystem getConfigFileSystem(String core) {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException("Not implemented yet.");
		return null;
	}

	@Override
	public SolrClient getAdminServer() {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException("Not implemented yet.");
		return null;
	}

	@Override
	public AtomicFileSystem getConfigFileSystem() {
		throw new UnsupportedOperationException("TODO");
	}

}
