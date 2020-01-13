package com.constellio.data.dao.services.solr;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.io.stream.TupleStream;

import java.util.Map;

public interface SolrServerFactory {

	SolrClient newSolrServer(String core);

	TupleStream newTupleStream(String core, Map<String, String> props);

	void reloadSolrServer(String core);

	AtomicFileSystem getConfigFileSystem(String core);

	AtomicFileSystem getConfigFileSystem();

	void clear();
}
