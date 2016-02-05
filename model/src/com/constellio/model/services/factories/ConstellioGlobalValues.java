package com.constellio.model.services.factories;

/**
 * Centralize all singletons in this class
 */
public class ConstellioGlobalValues {

	//	private ForkParsers forkParsers;
	//
	//	private SolrServers solrServers;
	//
	//	private SolrServerFactory solrServerFactory;
	//
	//	public ConstellioGlobalValues(SolrServerFactory solrServerFactory) {
	//		this.solrServerFactory = solrServerFactory;
	//	}
	//
	//	public synchronized ForkParsers getForkParsers() {
	//		if (forkParsers == null) {
	//			forkParsers = newForkParsers();
	//		}
	//		return forkParsers;
	//	}
	//
	//	ForkParsers newForkParsers() {
	//		return new ForkParsers(20);
	//	}
	//
	//	public void clear() {
	//		if (forkParsers != null) {
	//			forkParsers.close();
	//			forkParsers = null;
	//		}
	//		if (solrServers != null) {
	//			solrServers.close();
	//			solrServers = null;
	//		}
	//		if (solrServerFactory != null) {
	//			solrServerFactory.clear();
	//		}
	//	}
	//
	//	public SolrServers getSolrServers() {
	//		if (solrServers == null) {
	//			solrServers = newSolrServer();
	//		}
	//		return solrServers;
	//	}
	//
	//	SolrServers newSolrServer() {
	//		return new SolrServers(solrServerFactory, configs);
	//	}

}
