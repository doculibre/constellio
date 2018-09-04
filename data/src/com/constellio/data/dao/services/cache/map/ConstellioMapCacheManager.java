package com.constellio.data.dao.services.cache.map;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ConstellioCacheManagerRuntimeException;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstellioMapCacheManager implements ConstellioCacheManager {

	private Map<String, ConstellioCache> caches = new HashMap<>();

	public ConstellioMapCacheManager(DataLayerConfiguration dataLayerConfiguration) {
	}

	@Override
	public List<String> getCacheNames() {
		return Collections.unmodifiableList(new ArrayList<>(caches.keySet()));
	}

	@Override
	public synchronized ConstellioCache getCache(String name) {
		ConstellioCache cache = caches.get(name);
		if (cache == null) {
			cache = new ConstellioMapCache(name, new ConstellioCacheOptions());
			caches.put(name, cache);
		}
		return cache;
	}

	@Override
	public ConstellioCache createCache(String name, ConstellioCacheOptions options) {
		ConstellioCache cache = caches.get(name);
		if (cache == null) {
			cache = new ConstellioMapCache(name, options);
			caches.put(name, cache);
		} else {
			throw new ConstellioCacheManagerRuntimeException.ConstellioCacheManagerRuntimeException_CacheAlreadyExist(name);
		}
		return cache;
	}

	@Override
	public void initialize() {
	}

	@Override
	public void close() {
	}

	@Override
	public void clearAll() {
		for (ConstellioCache cache : caches.values()) {
			cache.clear();
		}
	}

}
