package com.constellio.app.modules.es.connectors.http;

import com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCache;
import com.constellio.app.modules.es.connectors.caches.ConnectorDocumentURLCacheEntry;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.model.connectors.http.ConnectorHttpDocument;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.cache.InsertionReason;

import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;

public class ConnectorHttpDocumentURLCache extends ConnectorDocumentURLCache {

	Map<String, String> documentUrlsClassifiedByDigests = new HashMap<>();


	public ConnectorHttpDocumentURLCache(ConnectorInstance instance,
										 AppLayerFactory appLayerFactory) {
		super(instance, appLayerFactory, asList(ConnectorHttpDocument.SCHEMA_TYPE));
		addCachedMetadata(ConnectorHttpDocument.DIGEST);
		addCachedMetadata(ConnectorHttpDocument.COPY_OF);
	}


	@Override
	protected void insertInCache(String url, ConnectorDocumentURLCacheEntry entry, InsertionReason insertionReason) {
		super.insertInCache(url, entry, insertionReason);

		if (insertionReason == InsertionReason.WAS_OBTAINED) {
			String digest = entry.getMetadata(ConnectorHttpDocument.DIGEST);
			String copyOf = entry.getMetadata(ConnectorHttpDocument.COPY_OF);

			if (digest != null && copyOf == null) {
				documentUrlsClassifiedByDigests.put(digest, url);
			}
		}
	}

	@Override
	public void invalidateAll() {
		super.invalidateAll();
		documentUrlsClassifiedByDigests.clear();
	}

	public String getDocumentUrlWithDigest(String digest) {
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

}
