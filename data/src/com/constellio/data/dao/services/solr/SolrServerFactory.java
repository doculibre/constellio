package com.constellio.data.dao.services.solr;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import org.apache.solr.client.solrj.SolrClient;

public interface SolrServerFactory {

	SolrClient newSolrServer(String core);

	void reloadSolrServer(String core);

	AtomicFileSystem getConfigFileSystem(String core);

	AtomicFileSystem getConfigFileSystem();

	void clear();
}
