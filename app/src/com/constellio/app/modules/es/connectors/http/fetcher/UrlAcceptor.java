package com.constellio.app.modules.es.connectors.http.fetcher;

public interface UrlAcceptor {

	boolean isAccepted(String normalizedUrl);

}
