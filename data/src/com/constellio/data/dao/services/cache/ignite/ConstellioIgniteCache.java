package com.constellio.data.dao.services.cache.ignite;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;

import com.constellio.data.dao.services.cache.ConstellioCache;

public class ConstellioIgniteCache implements ConstellioCache {
	
	public static final String CLEAR_MESSAGE_TOPIC = "clear";

	private static final Object NULL = "__NULL__";

	private String name;

	private IgniteCache<String, Object> igniteCache;
	
	private IgniteDataStreamer<String, Object> igniteStreamer;
	
	private Ignite igniteClient;

	private Map<String, Object> localCache = new ConcurrentHashMap<>();

	public ConstellioIgniteCache(String name, IgniteCache<String, Object> igniteCache, Ignite igniteClient) {
		this.name = name;
		this.igniteCache = igniteCache;
		this.igniteClient = igniteClient;
//		this.igniteStreamer = igniteClient.dataStreamer(igniteCache.getName()); 
//		this.igniteStreamer.allowOverwrite(true);
//		this.igniteStreamer.autoFlushFrequency(1);
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
			} else {
				localCache.put(key, NULL);
			}
		}
		result = NULL.equals(result) ? null : result;
		return result;
	}

	@Override
	public <T extends Serializable> void put(String key, T value) {
		put(key, value, false);
	}
		
	@SuppressWarnings("unchecked")
	<T extends Serializable> void put(String key, T value, boolean locallyOnly) {	
		value = value == null ? (T) NULL : value;
		localCache.put(key, value);
//		igniteStreamer.addData(key, value);
		if (!locallyOnly) {
			igniteCache.put(key, value);
		}
	}

	@Override
	public void remove(String key) {
		localCache.remove(key);
		igniteCache.remove(key);
	}

	@Override
	public void removeAll(Set<String> keys) {
		for (String key : keys) {
			removeLocal(key);
		}
		igniteCache.removeAll(keys);
	}

	@Override
	public void clear() {
		clearLocal();
		igniteClient.message(igniteClient.cluster().forRemotes()).send(CLEAR_MESSAGE_TOPIC, igniteCache.getName());
	}

	public void removeLocal(String key) {
		localCache.remove(key);
	}

	public void clearLocal() {
		localCache.clear();
		igniteCache.clear();
	}
	
	public void flush() {
//		igniteStreamer.flush();
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

	@Override
	public int size() {
		return localCache.size();
	}

}
