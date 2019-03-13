package com.constellio.model.services.caches;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.ConstellioCache;

import java.util.HashMap;
import java.util.Map;

public class ModelLayerCachesManager implements StatefulService {

	Map<String, UserCache> userCaches = new HashMap<>();

	Map<String, Map<String, CollectionCache>> collectionsCaches = new HashMap<>();

	public void register(UserCache userCache) {
		ConstellioCache cache = userCache.getCache();
		userCaches.put(cache.getName(), userCache);

	}

	public <T extends UserCache> T getUserCache(String cacheName) {
		return (T) userCaches.get(cacheName);
	}

	public <T extends UserCache> T getUserCache(String collection, String cacheName) {
		return getUserCache(collection + ":" + cacheName);
	}

	public void register(String collection, String cacheName, CollectionCache collectionCache) {
		Map<String, CollectionCache> collectionCaches = collectionsCaches.get(collection);
		collectionCaches.put(cacheName, collectionCache);
	}

	public CollectionCache getCollectionCache(String collection, String cacheName) {
		return collectionsCaches.get(collection).get(cacheName);
	}

	public Map<String, UserCache> getUserCaches() {
		return userCaches;
	}

	@Override
	public void initialize() {

	}

	@Override
	public void close() {
		userCaches.clear();
	}
}
