package com.constellio.data.dao.services.cache.ignite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.cache.Cache.Entry;

import org.apache.commons.lang3.time.DateUtils;
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
	
	private boolean synchronizing = false;
	
	private Date lastSynchronizationDate = new Date();

	public ConstellioIgniteCache(String name, IgniteCache<String, Object> igniteCache, Ignite igniteClient) {
		this.name = name;
		this.igniteCache = igniteCache;
		this.igniteClient = igniteClient;
		this.igniteStreamer = igniteClient.dataStreamer(igniteCache.getName()); 
		this.igniteStreamer.allowOverwrite(true);
	}

	@Override
	public final String getName() {
		return name;
	}
	
	public IgniteCache<String, Object> getIgniteCache() {
		return igniteCache;
	}

	public IgniteDataStreamer<String, Object> getIgniteStreamer() {
		return igniteStreamer;
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Serializable> void synchronizeIfNecessary() {
		Date expiryDate = DateUtils.addMinutes(lastSynchronizationDate, 5);
		Date now = new Date();
		if (now.after(expiryDate)) {
			synchronized (this) {
				if (!synchronizing) {
					synchronizing = true;
					localCache.clear();
					Iterator<Entry<String, Object>> remoteIterator = igniteCache.iterator();
					while (remoteIterator.hasNext()) {
						Entry<String, Object> remoteEntry = remoteIterator.next();
						put(remoteEntry.getKey(), (T) remoteEntry.getValue(), true);
					}
					lastSynchronizationDate = new Date();
					synchronizing = false;
				} else {
					while (synchronizing) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Serializable> T get(String key) {
		synchronizeIfNecessary();
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
		synchronizeIfNecessary();
		put(key, value, false);
	}
		
	@SuppressWarnings("unchecked")
	<T extends Serializable> void put(String key, T value, boolean locallyOnly) {	
		if (!locallyOnly) {
			synchronizeIfNecessary();
		}
		value = value == null ? (T) NULL : value;
		localCache.put(key, value);
		if (!locallyOnly) {
			igniteCache.put(key, value);
		}
	}

	@Override
	public void remove(String key) {
		synchronizeIfNecessary();
		localCache.remove(key);
		igniteCache.remove(key);
	}

	@Override
	public void removeAll(Set<String> keys) {
		synchronizeIfNecessary();
		for (String key : keys) {
			removeLocal(key);
		}
		igniteCache.removeAll(keys);
	}

	@Override
	public void clear() {
		synchronizeIfNecessary();
		clearLocal();
		igniteClient.message(igniteClient.cluster().forRemotes()).send(CLEAR_MESSAGE_TOPIC, igniteCache.getName());
	}

	public void removeLocal(String key) {
		synchronizeIfNecessary();
		localCache.remove(key);
	}

	public void clearLocal() {
		synchronizeIfNecessary();
		localCache.clear();
		igniteCache.clear();
	}

	@Override
	public Iterator<String> keySet() {
		synchronizeIfNecessary();
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
		synchronizeIfNecessary();
		return localCache.size();
	}

	@Override
	public List<Object> getAllValues() {
		synchronizeIfNecessary();
		return new ArrayList<>(localCache.values());
	}

}
