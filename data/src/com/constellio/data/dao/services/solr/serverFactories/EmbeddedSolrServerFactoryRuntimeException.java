package com.constellio.data.dao.services.solr.serverFactories;

@SuppressWarnings("serial")
public class EmbeddedSolrServerFactoryRuntimeException extends RuntimeException {

	public EmbeddedSolrServerFactoryRuntimeException(String message) {
		super(message);
	}

	public EmbeddedSolrServerFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public EmbeddedSolrServerFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public static class CannotCreateSolrServer extends EmbeddedSolrServerFactoryRuntimeException {

		public CannotCreateSolrServer(Throwable t) {
			super("Cannot create solr server", t);
		}
	}

}
