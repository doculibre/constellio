package com.constellio.data.dao.services.solr.serverFactories;

@SuppressWarnings("serial")
public class CreateStructureUsingDefaultOneEmbeddedSolrServerFactoryRuntimeException extends RuntimeException {

	public CreateStructureUsingDefaultOneEmbeddedSolrServerFactoryRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CreateStructureUsingDefaultOneEmbeddedSolrServerFactoryRuntimeException(String message) {
		super(message);
	}

	public CreateStructureUsingDefaultOneEmbeddedSolrServerFactoryRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class CannotCreateSolrServer extends CreateStructureUsingDefaultOneEmbeddedSolrServerFactoryRuntimeException {

		public CannotCreateSolrServer(Throwable cause) {
			super("Cannot create slor server", cause);
		}
	}

}
