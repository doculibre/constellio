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

	long version = 0L;

	public boolean isNewUrl(String url) {
		return !fetchedUrls.contains(url);
	}

	public synchronized void markAsFetched(String url) {
		ConnectorHttpContextServices.dirtyContexts.add(connectorId);
		fetchedUrls.add(url);
		version++;
	}

	public synchronized void markAsNoMoreFetched(String url) {
		ConnectorHttpContextServices.dirtyContexts.add(connectorId);
		fetchedUrls.remove(url);
		version++;
	}

	public String getDocumentUrlWithDigest(String digest) {
		return documentUrlsClassifiedByDigests.get(digest);
	}

	public synchronized void removeDocumentDigest(String digest, String url) {
		if (url.equals(documentUrlsClassifiedByDigests.get(digest))) {
			ConnectorHttpContextServices.dirtyContexts.add(connectorId);
			documentUrlsClassifiedByDigests.remove(digest);
			version++;
		}
	}

	public synchronized void addDocumentDigest(String digest, String url) {
		ConnectorHttpContextServices.dirtyContexts.add(connectorId);
		documentUrlsClassifiedByDigests.put(digest, url);
		version++;
	}

	ConnectorHttpContext(String connectorId) {
		this.connectorId = connectorId;
	}

	public String getConnectorId() {
		return connectorId;
	}

	public long getVersion() { return version; }
}
