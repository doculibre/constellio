package com.constellio.data.dao.services.cache.event;

import static com.constellio.data.events.EventBusEventsExecutionStrategy.EXECUTED_LOCALLY_THEN_SENT_REMOTELY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.data.events.EventBus;
import com.constellio.data.events.EventBusManager;

public class ConstellioEventCacheManager implements ConstellioCacheManager {

	private Map<String, ConstellioCache> caches = new HashMap<>();

	private EventBusManager eventBusManager;

	public ConstellioEventCacheManager(EventBusManager eventBusManager) {
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
			EventBus eventBus = eventBusManager.createEventBus("cache-" + name, EXECUTED_LOCALLY_THEN_SENT_REMOTELY);
			cache = new ConstellioEventCache(name, eventBus);
			caches.put(name, cache);
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
