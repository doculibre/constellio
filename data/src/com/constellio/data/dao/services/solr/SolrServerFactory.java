package com.constellio.data.dao.services.solr;

import org.apache.solr.client.solrj.SolrClient;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;

public interface SolrServerFactory {

	SolrClient newSolrServer(String core);
	void reloadSolrServer(String core);
	AtomicFileSystem getConfigFileSystem(String core);
	
	AtomicFileSystem getConfigFileSystem();
	void clear();
}
