package com.constellio.data.dao.services.cache.event;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.ONLY_SENT_REMOTELY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.dao.services.cache.ConstellioCacheManagerRuntimeException.ConstellioCacheManagerRuntimeException_CacheAlreadyExist;
import com.constellio.data.dao.services.cache.ConstellioCacheOptions;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusManager;

public class ConstellioEventMapCacheManager implements ConstellioCacheManager {

	private Map<String, ConstellioCache> caches = new HashMap<>();

	private EventBusManager eventBusManager;

	public ConstellioEventMapCacheManager(EventBusManager eventBusManager) {
		this.eventBusManager = eventBusManager;
	}

	@Override
	public List<String> getCacheNames() {
		return Collections.unmodifiableList(new ArrayList<>(caches.keySet()));
	}

	@Override
	public synchronized ConstellioCache getCache(String name) {
		ConstellioCache cache = caches.get(name);
		if (cache == null) {
			EventBus eventBus = eventBusManager.createEventBus("cache-" + name, ONLY_SENT_REMOTELY);
			cache = new ConstellioEventMapCache(name, eventBus, new ConstellioCacheOptions());
			caches.put(name, cache);
		}
		return cache;
	}

	@Override
	public ConstellioCache createCache(String name, ConstellioCacheOptions options) {
		ConstellioCache cache = caches.get(name);
		if (cache == null) {
			EventBus eventBus = eventBusManager.createEventBus("cache-" + name, ONLY_SENT_REMOTELY);
			cache = new ConstellioEventMapCache(name, eventBus, options);
			caches.put(name, cache);
		} else {
			throw new ConstellioCacheManagerRuntimeException_CacheAlreadyExist(name);
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
