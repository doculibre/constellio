package com.constellio.data.dao.services.solr;

import org.apache.solr.client.solrj.SolrClient;

import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.data.io.concurrent.filesystem.VersioningAtomicFileSystem;

public interface SolrServerFactory {

	SolrClient newSolrServer(String core);
	void reloadSolrServer(String core);
	VersioningAtomicFileSystem getConfigFileSystem(String core);
	
	AtomicFileSystem getConfigFileSystem();
	void clear();
	boolean isHealthy(String name);
}
