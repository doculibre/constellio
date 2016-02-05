package com.constellio.app.modules.es.connectors.http.fetcher;

public interface FetchTaskCompletedHandler {

	public void taskCompleted(FetchedDoc fetchedDoc)
			throws Exception;
}
