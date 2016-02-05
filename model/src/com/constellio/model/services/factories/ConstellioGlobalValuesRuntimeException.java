package com.constellio.model.services.factories;

@SuppressWarnings("serial")
public class ConstellioGlobalValuesRuntimeException extends RuntimeException {

	private ConstellioGlobalValuesRuntimeException(String message) {
		super(message);
	}

	public static class SolrServerFactoryNotDefined extends ConstellioGlobalValuesRuntimeException {
		public SolrServerFactoryNotDefined() {
			super("Solr server factory must be previously defined using 'setSolrServerFactory'");
		}
	}

}
