package com.constellio.app.modules.es.connectors.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConnectorHttpContext implements Serializable {

	String connectorId;

	Set<String> fetchedUrls = new HashSet<>();

	Map<String, String> documentUrlsClassifiedByDigests = new HashMap<>();

	public boolean isNewUrl(String url) {
		return !fetchedUrls.contains(url);
	}

	public synchronized void markAsFetched(String url) {
		fetchedUrls.add(url);
	}

	public synchronized void markAsNoMoreFetched(String url) {
		fetchedUrls.remove(url);
	}

	public  String getDocumentUrlWithDigest(String digest) {
		return documentUrlsClassifiedByDigests.get(digest);
	}

	public synchronized void removeDocumentDigest(String digest, String url) {
		if (url.equals(documentUrlsClassifiedByDigests.get(digest))) {
			documentUrlsClassifiedByDigests.remove(digest);
		}
	}

	public synchronized void addDocumentDigest(String digest, String url) {
		documentUrlsClassifiedByDigests.put(digest, url);
	}

	ConnectorHttpContext(String connectorId) {
		this.connectorId = connectorId;
	}

	public String getConnectorId() {
		return connectorId;
	}
}
