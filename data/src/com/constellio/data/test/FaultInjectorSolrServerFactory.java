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
package com.constellio.data.test;

import java.io.IOException;
import java.util.Random;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.util.NamedList;

import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

public class FaultInjectorSolrServerFactory implements SolrServerFactory {

	SolrServerFactory nestedSolrServerFactory;

	public FaultInjectorSolrServerFactory(SolrServerFactory nestedSolrServerFactory) {
		this.nestedSolrServerFactory = nestedSolrServerFactory;
	}

	@Override
	public SolrClient newSolrServer(String core) {
		SolrClient nestedSolrServer = nestedSolrServerFactory.newSolrServer(core);
		return nestedSolrServer;
		//return new FaultInjectionSolrServer(nestedSolrServer);
	}

	@Override
	public void clear() {
		nestedSolrServerFactory.clear();
	}

	public static class FaultInjectionSolrServer extends SolrClient {

		SolrClient nestedSolrServer;
		private Random random = new Random();

		public FaultInjectionSolrServer(SolrClient nestedSolrServer) {
			this.nestedSolrServer = nestedSolrServer;
		}

		@Override
		public NamedList<Object> request(SolrRequest request)
				throws SolrServerException, IOException {
			//if (random.nextInt(10) == 0) {
			//	throw new RemoteSolrException(404, "Random injected fault", new RuntimeException());
			//}
			return nestedSolrServer.request(request);
		}

		@Override
		public void shutdown() {
			nestedSolrServer.shutdown();
		}
	}

	@Override
	public AtomicFileSystem getConfigFileSystem(String core) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public SolrClient getAdminServer() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public AtomicFileSystem getConfigFileSystem() {
		throw new UnsupportedOperationException("TODO");
	}

}
