package com.constellio.model.services.caches;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.cache.ConstellioCache;

import java.util.HashMap;
import java.util.Map;

public class ModelLayerCachesManager implements StatefulService {

	Map<String, UserCache> userCaches = new HashMap<>();

	public void register(UserCache userCache) {
		ConstellioCache cache = userCache.getCache();
		userCaches.put(cache.getName(), userCache);

	}

	public <T extends UserCache> T getUserCache(String cacheName) {
		return (T) userCaches.get(cacheName);
	}

	public <T extends UserCache> T getUserCache(String cacheName, String collection) {
		return getUserCache(collection + ":" + cacheName);
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
