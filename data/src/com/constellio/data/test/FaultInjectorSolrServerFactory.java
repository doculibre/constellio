package com.constellio.data.test;

import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.io.stream.TupleStream;
import org.apache.solr.common.util.NamedList;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

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
	public TupleStream newTupleStream(String core, Map<String, String> props) {
		throw new UnsupportedOperationException("Unsupported");
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
		public NamedList<Object> request(SolrRequest request, String collection)
				throws SolrServerException, IOException {
			//if (random.nextInt(10) == 0) {
			//	throw new RemoteSolrException(404, "Random injected fault", new RuntimeException());
			//}
			return nestedSolrServer.request(request, collection);
		}

		@Override
		public void close() throws IOException {
			nestedSolrServer.close();
		}
	}

	@Override
	public AtomicFileSystem getConfigFileSystem(String core) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public AtomicFileSystem getConfigFileSystem() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public void reloadSolrServer(String core) {
		throw new UnsupportedOperationException("TODO");
	}

}
