package com.constellio.data.dao.services.cache.ignite;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.IgniteCache;

import com.constellio.data.dao.services.cache.ConstellioCache;

public class ConstellioIgniteCache implements ConstellioCache {

	private static final Object NULL = "__NULL__";

	private String name;

	private IgniteCache<String, Object> igniteCache;

	private Map<String, Object> localCache = new ConcurrentHashMap<>();

	public ConstellioIgniteCache(String name, IgniteCache<String, Object> igniteCache) {
		this.name = name;
		this.igniteCache = igniteCache;
	}

	@Override
	public final String getName() {
		return name;
	}
	
	public IgniteCache<String, Object> getIgniteCache() {
		return igniteCache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		T result = (T) localCache.get(key);
		if (result == null) {
			result = (T) igniteCache.get(key);
			if (result != null) {
				localCache.put(key, result);
			}
		}
		result = NULL.equals(result) ? null : result;
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> void put(String key, T value) {
		value = value == null ? (T) NULL : value;
		localCache.put(key, value);
		igniteCache.put(key, value);
	}

	@Override
	public void remove(String key) {
		localCache.remove(key);
		igniteCache.clear(key);
	}

	@Override
	public void clear() {
		localCache.clear();
		igniteCache.clear();
	}

	public void removeLocal(String key) {
		localCache.remove(key);
	}

	@Override
	public Iterator<String> keySet() {
		final Iterator<String> adaptee = localCache.keySet().iterator();

		return new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return adaptee.hasNext();
			}

			@Override
			public String next() {
				return adaptee.next();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

}
