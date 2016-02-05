package com.constellio.data.dao.services.solr.serverFactories;

import org.apache.solr.client.solrj.SolrClient;

import com.constellio.data.dao.services.solr.SolrServerFactory;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.google.common.annotations.VisibleForTesting;

/**
 * This class is created only to create one general class that can be tested by {@link SolrServerFactoryTest}
 * @author Majid Laali
 *
 */
public abstract class AbstractSolrServerFactory implements SolrServerFactory{
	protected SolrClient getAdminServer() {
		return newSolrServer("");
	}
	
	
	@VisibleForTesting
	abstract SolrClient getSolrClient(String core);
	@VisibleForTesting
	abstract AtomicFileSystem getAtomicFileSystem(String core);
}
