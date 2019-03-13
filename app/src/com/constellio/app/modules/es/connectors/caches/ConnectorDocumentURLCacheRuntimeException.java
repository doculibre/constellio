package com.constellio.app.modules.es.connectors.caches;

public class ConnectorDocumentURLCacheRuntimeException extends RuntimeException {


	public ConnectorDocumentURLCacheRuntimeException(String message) {
		super(message);
	}

	public ConnectorDocumentURLCacheRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectorDocumentURLCacheRuntimeException(Throwable cause) {
		super(cause);
	}

	public static class ConnectorDocumentURLCacheRuntimeException_CouldNotLockDocumentForFetching extends ConnectorDocumentURLCacheRuntimeException {

		public ConnectorDocumentURLCacheRuntimeException_CouldNotLockDocumentForFetching(String url, boolean leaderStatus) {
			super("Cannot lock document with url '" + url + "' for fetching : " +
				  (leaderStatus ? "It may be fetched by another process" : "Server has it's leader status"));

		}
	}
}
